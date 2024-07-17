package gg.meza.doobs.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Worlds {
    public int port = 3002;
    public Map<String, Location> dungeons = new ConcurrentHashMap<>();
}
