package gg.meza.doobs;

import gg.meza.doobs.cardProcessing.AudioEvent;
import gg.meza.doobs.commands.DungeonSetCommand;
import gg.meza.doobs.commands.ServerPortCommand;
import gg.meza.doobs.data.Settings;
import gg.meza.doobs.server.BasicHttpServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DeckedOutOBS implements ClientModInitializer {
    public static Settings settings;
    public static final Logger LOGGER = LoggerFactory.getLogger("decked-out-obs");
    private BasicHttpServer httpServer = new BasicHttpServer();
    private AudioEvent audioEvent;
    private MinecraftClient client;

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String currentServer;
            if (client.isConnectedToLocalServer()) {
                currentServer = Objects.requireNonNull(client.getServer()).getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString();
            } else {
                currentServer = Objects.requireNonNull(client.getCurrentServerEntry()).address;
            }
            this.client = client;
            settings = new Settings(currentServer);
            settings.initConfig();
            beginProcessing();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DeckedOutOBS.LOGGER.info(Text.translatable("system.disconnect").getString());
            httpServer.stopServer();
            settings = null;
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            DungeonSetCommand.register(dispatcher, settings, this::beginProcessing);
            ServerPortCommand.register(dispatcher, settings, this::beginProcessing);
        });
    }

    private void beginProcessing() {
        if (!settings.isDungeonSet()) {
            LOGGER.info(Text.translatable("system.processing").getString());
            return;
        }
        audioEvent = new AudioEvent(httpServer, settings.getDungeonPosition());
        httpServer.startServer(settings.getPort());

        if (this.client.player != null) {
            this.client.player.sendMessage(Text.translatable("message.server_started", settings.getPort()), false);
            this.client.player.sendMessage(Text.translatable("message.obs_source", "http://localhost:" + settings.getPort()), false);
        }

        ClientTickEvents.END_WORLD_TICK.register(audioEvent::processBlocks);
    }

}
