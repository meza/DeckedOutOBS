package gg.meza.doobs.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.data.CardQueue;
import gg.meza.doobs.data.CardQueueManager;
import net.minecraft.text.Text;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BasicHttpServer {
    private final CardQueueManager cardQueueManager;
    private HttpServer server;
    private final Map<String, Session> sessions = new HashMap<>();
    private int lastPort = 0;

    public BasicHttpServer(CardQueueManager cardQueueManager) {
        this.cardQueueManager = cardQueueManager;
        DeckedOutOBS.scheduler.scheduleWithFixedDelay(this::cleanUpSessions, 0, 15, TimeUnit.SECONDS);
    }

    public void startServer(int port) {
        if ((server != null) && (port != this.lastPort)) {
            DeckedOutOBS.LOGGER.info(Text.translatable("decked-out-obs.system.changing_ports", this.lastPort, port).getString());
            server.stop(0);
        }

        if ((server != null) && (port == this.lastPort)) {
            DeckedOutOBS.LOGGER.info(Text.translatable("decked-out-obs.system.same_port", port).getString());
            return;
        }

        try {
            this.lastPort = port;
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleIndex);
            server.createContext("/favicon.png", this::handleFavicon);
            server.createContext("/assets", this::handleAssets); // Add this line
            server.createContext("/nextCard", this::handleNextCard);
            server.start();

            DeckedOutOBS.LOGGER.info(Text.translatable("decked-out-obs.message.server_started", port).getString());
        } catch (IOException var1) {
            throw new RuntimeException(var1);
        }
    }

    public void stopServer() {
        server.stop(0);
        server = null;
        DeckedOutOBS.LOGGER.info(Text.translatable("decked-out-obs.message.server_stopped").getString());
    }

    public static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        Session currentSession = getSession(exchange); // ensure cookie exists
        String response = readFile("/assets/decked-out-obs/web/index.html").replace("X-Session", currentSession.getSessionId());
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void handleFavicon(HttpExchange exchange) throws IOException {
        String filePath = "/assets/decked-out-obs/icon.png";
        sendAsset(exchange, filePath);
    }

    private void handleAssets(HttpExchange exchange) throws IOException {
        String filePath = exchange.getRequestURI().getPath().replaceFirst("/assets", "/assets/decked-out-obs/web/assets");
        sendAsset(exchange, filePath);
    }

    private void sendAsset(HttpExchange exchange, String filePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.getResponseHeaders().set("Content-Type", getMimeType(filePath));
                exchange.sendResponseHeaders(200, 0); // 0 means length is not known
                try (OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = is.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                }
            }
        }
    }

    private void handleNextCard(HttpExchange exchange) throws IOException {
        Session session = getSession(exchange);
        String response;
        CardQueue queue = cardQueueManager.getQueueForSession(session);
        String nextQueueItem = queue.nextCard();
        if (nextQueueItem == null) {
            response = "{\"card\": \"\", \"hasCard\": false}";
        } else {
            response = "{\"card\": \"" + nextQueueItem + "\", \"hasCard\": true}";
            DeckedOutOBS.LOGGER.debug("Sending card {} to session {}", nextQueueItem, session.getSessionId());
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readFile(String path) {
        StringBuilder contentBuilder = new StringBuilder();
        try (InputStream is = getClass().getResourceAsStream(path);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String str;
            while ((str = br.readLine()) != null) {
                contentBuilder.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private String getMimeType(String path) {
        String mimeType = "application/octet-stream"; // Default binary MIME type
        if (path.endsWith(".html")) {
            mimeType = "text/html";
        } else if (path.endsWith(".js")) {
            mimeType = "application/javascript";
        } else if (path.endsWith(".css")) {
            mimeType = "text/css";
        } else if (path.endsWith(".png")) {
            mimeType = "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        }
        // Add more MIME types as needed
        return mimeType;
    }

    private Session getSession(HttpExchange exchange) {
        String sessionId = getSessionIdFromExchange(exchange);
        Session session = sessions.get(sessionId);
        if (session == null) {
            session = new Session(sessionId);
            DeckedOutOBS.LOGGER.debug("Created new session {}", sessionId);
            sessions.put(sessionId, session);
            cardQueueManager.addQueueForSession(session);
        }
        session.updateLastAccessed();

        return session;
    }

    private String getSessionIdFromExchange(HttpExchange exchange) {
        List<String> sessionIds = exchange.getRequestHeaders().get("X-Session");
        return (sessionIds != null && !sessionIds.isEmpty()) ? sessionIds.get(0) : UUID.randomUUID().toString();
    }

    private void cleanUpSessions() {
        // Iterate over sessions and remove expired ones
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (session.isExpired()) {
                cardQueueManager.removeQueueForSession(session);
                return true;
            }
            return false;
        });
    }
}
