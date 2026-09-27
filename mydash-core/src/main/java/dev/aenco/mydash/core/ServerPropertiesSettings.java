package dev.aenco.mydash.core;

final class ServerPropertiesSettings {
    final String motd;
    final int serverPort;
    final int maxPlayers;
    final boolean onlineMode;
    final boolean whiteList;
    final String difficulty;
    final String gamemode;
    final boolean hardcore;

    ServerPropertiesSettings(
        String motd,
        int serverPort,
        int maxPlayers,
        boolean onlineMode,
        boolean whiteList,
        String difficulty,
        String gamemode,
        boolean hardcore
    ) {
        this.motd = motd;
        this.serverPort = serverPort;
        this.maxPlayers = maxPlayers;
        this.onlineMode = onlineMode;
        this.whiteList = whiteList;
        this.difficulty = difficulty;
        this.gamemode = gamemode;
        this.hardcore = hardcore;
    }
}
