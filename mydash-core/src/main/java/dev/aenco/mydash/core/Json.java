package dev.aenco.mydash.core;

final class Json {
    private Json() {}

    static String server(ServerSnapshot snapshot) {
        return "{"
            + "\"platform\":\"" + escape(snapshot.platform()) + "\","
            + "\"minecraftVersion\":\"" + escape(snapshot.minecraftVersion()) + "\","
            + "\"onlinePlayers\":" + snapshot.onlinePlayers() + ","
            + "\"maxPlayers\":" + snapshot.maxPlayers() + ","
            + "\"uptimeMillis\":" + snapshot.uptimeMillis()
            + "}";
    }

    static String escape(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}
