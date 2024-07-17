package gg.meza.doobs.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import gg.meza.doobs.DeckedOutOBS;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.SerializationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {
    private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("decked_out.json");
    private String currentWorld = "";
    private Worlds configData = new Worlds();

    public Settings(String world) {
        this.currentWorld = world;
        this.initConfig();
    }

    public void initConfig() {
        if (configPath.toFile().exists())  {
            try {
                BufferedReader reader = Files.newBufferedReader(configPath);
                Gson gson = new Gson();
                configData = gson.fromJson(reader, Worlds.class);
                reader.close();
            } catch (IOException | JsonParseException e) {
                DeckedOutOBS.LOGGER.error("Cause: " + e.getCause().getClass().getSimpleName());
                throw new SerializationException(e);
            }
        }
    }

    public void setDungeonPosition( BlockPos pos, Direction direction) {
        if (configData.dungeons.containsKey(this.currentWorld)) {
            configData.dungeons.replace(this.currentWorld, new Location(direction, pos));
        } else {
            configData.dungeons.put(this.currentWorld, new Location(direction, pos));
        }

        this.saveConfig();
    }

    public boolean isDungeonSet() {
        return configData.dungeons.containsKey(this.currentWorld);
    }

    public Location getDungeonPosition() {
        if (!isDungeonSet()) throw new RuntimeException("Dungeon not set");
        return configData.dungeons.get(this.currentWorld);
    }

    public int getPort() {
        return configData.port;
    }

    public void setPort(int port) {
        configData.port = port;
        this.saveConfig();
    }

    public void saveConfig() {
        try {
            // Save config
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedWriter writer = Files.newBufferedWriter(configPath);
            gson.toJson(configData, writer);
            writer.close();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
