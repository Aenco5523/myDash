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

    static String commandAccepted(String command) {
        return "{\"accepted\":true,\"command\":\"" + escape(command) + "\"}";
    }

    static String error(String code) {
        return "{\"error\":\"" + escape(code) + "\"}";
    }

    static String readCommand(String body) {
        if (body == null) return null;
        String trimmed = body.trim();

        if (!trimmed.startsWith("{")) {
            return cleanCommand(trimmed);
        }

        int key = trimmed.indexOf("\"command\"");
        if (key < 0) return null;

        int colon = trimmed.indexOf(':', key + 9);
        if (colon < 0) return null;

        int quote = skipWhitespace(trimmed, colon + 1);
        if (quote >= trimmed.length() || trimmed.charAt(quote) != '"') return null;

        StringBuilder value = new StringBuilder();
        boolean escaped = false;

        for (int i = quote + 1; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (escaped) {
                switch (ch) {
                    case '"': value.append('"'); break;
                    case '\\': value.append('\\'); break;
                    case '/': value.append('/'); break;
                    case 'b': value.append('\b'); break;
                    case 'f': value.append('\f'); break;
                    case 'n': value.append('\n'); break;
                    case 'r': value.append('\r'); break;
                    case 't': value.append('\t'); break;
                    default: return null;
                }
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                continue;
            }

            if (ch == '"') {
                return cleanCommand(value.toString());
            }

            value.append(ch);
        }

        return null;
    }

    static String escape(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static int skipWhitespace(String value, int start) {
        int index = start;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String cleanCommand(String command) {
        if (command == null) return null;
        String clean = command.trim();
        while (clean.startsWith("/")) clean = clean.substring(1).trim();
        if (clean.isEmpty() || clean.length() > 2048) return null;
        if (clean.indexOf('\n') >= 0 || clean.indexOf('\r') >= 0) return null;
        return clean;
    }
}
