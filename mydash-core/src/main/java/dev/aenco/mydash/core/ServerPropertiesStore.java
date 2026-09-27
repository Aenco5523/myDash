package dev.aenco.mydash.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

final class ServerPropertiesStore {
    private final Path path;

    ServerPropertiesStore(Path serverDirectory) {
        this.path = serverDirectory.toAbsolutePath().normalize().resolve("server.properties");
    }

    synchronized ServerPropertiesSettings read() throws IOException {
        Properties properties = load();

        return new ServerPropertiesSettings(
            properties.getProperty("motd", "A Minecraft Server"),
            intValue(properties, "server-port", 25565, 1, 65535),
            intValue(properties, "max-players", 20, 1, 100000),
            booleanValue(properties, "online-mode", true),
            booleanValue(properties, "white-list", false),
            enumValue(properties, "difficulty", "easy", new String[]{"peaceful", "easy", "normal", "hard"}),
            enumValue(properties, "gamemode", "survival", new String[]{"survival", "creative", "adventure", "spectator"}),
            booleanValue(properties, "hardcore", false)
        );
    }

    synchronized void write(ServerPropertiesSettings settings) throws IOException {
        Properties properties = load();

        properties.setProperty("motd", settings.motd);
        properties.setProperty("server-port", Integer.toString(settings.serverPort));
        properties.setProperty("max-players", Integer.toString(settings.maxPlayers));
        properties.setProperty("online-mode", Boolean.toString(settings.onlineMode));
        properties.setProperty("white-list", Boolean.toString(settings.whiteList));
        properties.setProperty("difficulty", settings.difficulty);
        properties.setProperty("gamemode", settings.gamemode);
        properties.setProperty("hardcore", Boolean.toString(settings.hardcore));

        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        Path temporary = path.resolveSibling(path.getFileName().toString() + ".mydash.tmp");

        try (OutputStream output = Files.newOutputStream(temporary)) {
            properties.store(output, "Updated by myDash");
        }

        try {
            Files.move(
                temporary,
                path,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Properties load() throws IOException {
        Properties properties = new Properties();

        if (!Files.exists(path)) return properties;

        if (!Files.isRegularFile(path) || Files.isSymbolicLink(path)) {
            throw new IOException("server.properties must be a regular non-symlink file");
        }

        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }

        return properties;
    }

    private static int intValue(Properties properties, String key, int fallback, int min, int max) {
        String raw = properties.getProperty(key);
        if (raw == null) return fallback;

        try {
            int value = Integer.parseInt(raw.trim());
            if (value < min || value > max) return fallback;
            return value;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static boolean booleanValue(Properties properties, String key, boolean fallback) {
        String raw = properties.getProperty(key);
        if (raw == null) return fallback;
        if ("true".equalsIgnoreCase(raw.trim())) return true;
        if ("false".equalsIgnoreCase(raw.trim())) return false;
        return fallback;
    }

    private static String enumValue(Properties properties, String key, String fallback, String[] allowed) {
        String raw = properties.getProperty(key);
        if (raw == null) return fallback;

        String value = raw.trim().toLowerCase();
        for (String candidate : allowed) {
            if (candidate.equals(value)) return value;
        }

        return fallback;
    }
}
