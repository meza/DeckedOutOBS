package gg.meza.doobs.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.data.CardQueue;
import net.minecraft.text.Text;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

public class BasicHttpServer {
    private HttpServer server;
    private final CardQueue queue;
    private int lastPort = 0;

    public BasicHttpServer(CardQueue queue) {
        this.queue = queue;
    }

    public void startServer(int port) {
        if ((server != null) && (port != this.lastPort)) {
            DeckedOutOBS.LOGGER.info(Text.translatable("system.changing_ports", this.lastPort, port).getString());
            server.stop(0);
        }

        if ((server != null) && (port == this.lastPort)) {
            DeckedOutOBS.LOGGER.info(Text.translatable("system.same_port", port).getString());
            return;
        }

        try {
            this.lastPort = port;
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleIndex);
            server.createContext("/assets", this::handleAssets); // Add this line
            server.createContext("/nextCard", this::handleNextCard);
            server.start();
            DeckedOutOBS.LOGGER.info(Text.translatable("message.server_started", port).getString());
        } catch (IOException var1) {
            throw new RuntimeException(var1);
        }
    }

    public void stopServer() {
        server.stop(0);
        DeckedOutOBS.LOGGER.info(Text.translatable("message.server_stopped").getString());
    }

    private void handleNextCard(HttpExchange exchange) throws IOException {
        String response;
        String nextQueueItem = queue.nextCard();
        if (nextQueueItem == null) {
            response = "{\"card\": \"\", \"hasCard\": false}";
        } else {
            response = "{\"card\": \"" + nextQueueItem + "\", \"hasCard\": true}";
            DeckedOutOBS.LOGGER.info("Sending card: {}", nextQueueItem);
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void handleIndex(HttpExchange exchange) throws IOException {
        String response = readFile("/web/index.html");
        exchange.getResponseHeaders().set("Content-Type", "text/html");
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

    private void handleAssets(HttpExchange exchange) throws IOException {
        String filePath = exchange.getRequestURI().getPath().replaceFirst("/assets", "/web/assets");
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

    public static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
