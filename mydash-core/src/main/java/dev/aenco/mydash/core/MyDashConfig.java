package dev.aenco.mydash.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

final class MyDashConfig {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8765;

    private final Path path;
    private final Properties properties;

    private MyDashConfig(Path path, Properties properties) {
        this.path = path;
        this.properties = properties;
    }

    static MyDashConfig load(Path path) throws IOException {
        Properties properties = new Properties();

        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }
        }

        boolean changed = false;
        changed |= putIfAbsent(properties, "server.bind", DEFAULT_HOST);
        changed |= putIfAbsent(properties, "server.port", Integer.toString(DEFAULT_PORT));
        changed |= putIfAbsent(properties, "server.allowRemote", "false");

        MyDashConfig config = new MyDashConfig(path, properties);
        if (changed || !Files.exists(path)) {
            config.save();
        }
        return config;
    }

    String bindAddress() {
        return properties.getProperty("server.bind", DEFAULT_HOST).trim();
    }

    int port() {
        String raw = properties.getProperty("server.port", Integer.toString(DEFAULT_PORT)).trim();
        try {
            int port = Integer.parseInt(raw);
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid myDash server.port: " + raw);
        }
    }

    boolean allowRemote() {
        return Boolean.parseBoolean(properties.getProperty("server.allowRemote", "false"));
    }

    String get(String key) {
        return properties.getProperty(key);
    }

    void set(String key, String value) {
        properties.setProperty(key, value);
    }

    synchronized void save() throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (OutputStream output = Files.newOutputStream(path)) {
            properties.store(output, "myDash configuration - keep this file private");
        }

        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            );
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException ignored) {
            // Windows and other non-POSIX file systems do not support POSIX permissions.
        }
    }

    private static boolean putIfAbsent(Properties properties, String key, String value) {
        if (properties.getProperty(key) != null) return false;
        properties.setProperty(key, value);
        return true;
    }
}
