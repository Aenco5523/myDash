package dev.aenco.mydash.core;

public final class ServerSnapshot {
    private final String platform;
    private final String minecraftVersion;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final long uptimeMillis;

    public ServerSnapshot(String platform, String minecraftVersion, int onlinePlayers, int maxPlayers, long uptimeMillis) {
        this.platform = platform;
        this.minecraftVersion = minecraftVersion;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.uptimeMillis = uptimeMillis;
    }

    public String platform() { return platform; }
    public String minecraftVersion() { return minecraftVersion; }
    public int onlinePlayers() { return onlinePlayers; }
    public int maxPlayers() { return maxPlayers; }
    public long uptimeMillis() { return uptimeMillis; }
}
