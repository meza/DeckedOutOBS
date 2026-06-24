package gg.meza.doobs;

import gg.meza.doobs.cardProcessing.AudioEvent;
import gg.meza.doobs.commands.DungeonSetCommand;
import gg.meza.doobs.commands.ServerPortCommand;
import gg.meza.doobs.data.CardQueueManager;
import gg.meza.doobs.data.Settings;
import gg.meza.doobs.server.BasicHttpServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DeckedOutOBS implements ClientModInitializer {
    public static Settings settings;
    public static final Logger LOGGER = LoggerFactory.getLogger("decked-out-obs");
    private final CardQueueManager queueManager = new CardQueueManager();
    private final BasicHttpServer httpServer = new BasicHttpServer(queueManager);
    private Minecraft client;
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private AudioEvent audioEvent;
    private boolean tickRegistered;

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String currentServer;
            //? >= 26.2
            if (client.hasSingleplayerServer()) {
            //? < 26.2
            //if (client.isSingleplayer()) {
                currentServer = Objects.requireNonNull(client.getSingleplayerServer()).getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
            } else {
                currentServer = Objects.requireNonNull(client.getCurrentServer()).ip;
            }
            this.client = client;
            settings = new Settings(currentServer);
            settings.initConfig();
            beginProcessing();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DeckedOutOBS.LOGGER.info(Component.translatable("decked-out-obs.system.disconnect").getString());
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
            LOGGER.info(Component.translatable("decked-out-obs.system.processing").getString());
            return;
        }

        if (audioEvent == null) {
            audioEvent = new AudioEvent(queueManager, settings.getDungeonPosition());
        }

        if (this.client.player != null) {
            this.client.player.sendSystemMessage(Component.translatable("decked-out-obs.message.server_started", settings.getPort()));
            this.client.player.sendSystemMessage(Component.translatable("decked-out-obs.message.obs_source", "http://localhost:" + settings.getPort()));
        }

        if (!tickRegistered) {
            ClientTickEvents.END_LEVEL_TICK.register(audioEvent::processBlocks);
            tickRegistered = true;
        }

        httpServer.startServer(settings.getPort());
    }

}
