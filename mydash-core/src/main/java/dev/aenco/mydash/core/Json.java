package dev.aenco.mydash.core;

import dev.aenco.mydash.api.DashboardExtension;

import java.util.Collection;
import java.util.List;

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

    static String serverProperties(ServerPropertiesSettings settings) {
        return "{"
            + "\"motd\":\"" + escape(settings.motd) + "\","
            + "\"serverPort\":" + settings.serverPort + ","
            + "\"maxPlayers\":" + settings.maxPlayers + ","
            + "\"onlineMode\":" + settings.onlineMode + ","
            + "\"whiteList\":" + settings.whiteList + ","
            + "\"difficulty\":\"" + escape(settings.difficulty) + "\","
            + "\"gamemode\":\"" + escape(settings.gamemode) + "\","
            + "\"hardcore\":" + settings.hardcore
            + "}";
    }

    static String serverPropertiesUpdated() {
        return "{\"updated\":true,\"restartRequired\":true}";
    }

    static String settings(MyDashConfig config) {
        return "{"
            + "\"bindAddress\":\"" + escape(config.bindAddress()) + "\","
            + "\"port\":" + config.port() + ","
            + "\"allowRemote\":" + config.allowRemote()
            + "}";
    }

    static String settingsUpdated() {
        return "{\"updated\":true,\"restartRequired\":true}";
    }

    static String tokenRotated(String token) {
        return "{\"rotated\":true,\"token\":\"" + escape(token) + "\"}";
    }

    static String players(List<PlayerSnapshot> players) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) json.append(',');
            PlayerSnapshot player = players.get(i);
            json.append('{')
                .append("\"uuid\":\"").append(escape(player.uuid().toString())).append("\",")
                .append("\"name\":\"").append(escape(player.name())).append("\",")
                .append("\"operator\":").append(player.operator())
                .append('}');
        }
        return json.append(']').toString();
    }

    static String extensions(Collection<DashboardExtension> extensions) {
        StringBuilder json = new StringBuilder("[");
        int index = 0;

        for (DashboardExtension extension : extensions) {
            if (index++ > 0) json.append(',');
            json.append('{')
                .append("\"id\":\"").append(escape(extension.id())).append("\",")
                .append("\"displayName\":\"").append(escape(extension.displayName())).append("\",")
                .append("\"route\":\"").append(escape(extension.route())).append("\",")
                .append("\"permissions\":[");

            int permissionIndex = 0;
            for (String permission : extension.permissions()) {
                if (permissionIndex++ > 0) json.append(',');
                json.append("\"").append(escape(permission)).append("\"");
            }

            json.append("]}");
        }

        return json.append(']').toString();
    }

    static String consoleLine(ConsoleLine line) {
        return "{"
            + "\"id\":" + line.id() + ","
            + "\"timestamp\":" + line.timestamp() + ","
            + "\"level\":\"" + escape(line.level()) + "\","
            + "\"logger\":\"" + escape(line.logger()) + "\","
            + "\"message\":\"" + escape(line.message()) + "\""
            + "}";
    }

    static String commandAccepted(String command) {
        return "{\"accepted\":true,\"command\":\"" + escape(command) + "\"}";
    }

    static String actionAccepted(String action) {
        return "{\"accepted\":true,\"action\":\"" + escape(action) + "\"}";
    }

    static String error(String code) {
        return "{\"error\":\"" + escape(code) + "\"}";
    }

    static String readCommand(String body) {
        if (body == null) return null;
        String trimmed = body.trim();

        if (!trimmed.startsWith("{")) return cleanCommand(trimmed);
        return cleanCommand(readStringField(trimmed, "command"));
    }

    static String readOptionalReason(String body) {
        if (body == null || body.trim().isEmpty()) return "Kicked by myDash";

        String reason = readStringField(body.trim(), "reason");
        if (reason == null || reason.trim().isEmpty()) return "Kicked by myDash";

        String clean = reason.trim();
        if (clean.length() > 256) return null;
        if (clean.indexOf('\n') >= 0 || clean.indexOf('\r') >= 0) return null;
        return clean;
    }

    static String readStringField(String json, String field) {
        if (json == null) return null;

        String marker = "\"" + field + "\"";
        int key = json.indexOf(marker);
        if (key < 0) return null;

        int colon = json.indexOf(':', key + marker.length());
        if (colon < 0) return null;

        int quote = skipWhitespace(json, colon + 1);
        if (quote >= json.length() || json.charAt(quote) != '"') return null;

        StringBuilder value = new StringBuilder();
        boolean escaped = false;

        for (int i = quote + 1; i < json.length(); i++) {
            char ch = json.charAt(i);

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

            if (ch == '"') return value.toString();
            value.append(ch);
        }

        return null;
    }

    static Integer readIntField(String json, String field) {
        String raw = readPrimitiveField(json, field);
        if (raw == null) return null;

        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    static Boolean readBooleanField(String json, String field) {
        String raw = readPrimitiveField(json, field);
        if ("true".equals(raw)) return Boolean.TRUE;
        if ("false".equals(raw)) return Boolean.FALSE;
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

    private static String readPrimitiveField(String json, String field) {
        if (json == null) return null;

        String marker = "\"" + field + "\"";
        int key = json.indexOf(marker);
        if (key < 0) return null;

        int colon = json.indexOf(':', key + marker.length());
        if (colon < 0) return null;

        int start = skipWhitespace(json, colon + 1);
        int end = start;

        while (end < json.length()) {
            char ch = json.charAt(end);
            if (ch == ',' || ch == '}') break;
            end++;
        }

        if (end <= start) return null;
        return json.substring(start, end).trim();
    }

    private static int skipWhitespace(String value, int start) {
        int index = start;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) index++;
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
