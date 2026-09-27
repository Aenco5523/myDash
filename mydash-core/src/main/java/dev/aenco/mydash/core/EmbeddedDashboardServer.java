package dev.aenco.mydash.core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.aenco.mydash.api.DashboardAsset;
import dev.aenco.mydash.api.DashboardAssetProvider;
import dev.aenco.mydash.api.DashboardExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

final class EmbeddedDashboardServer {
    private static final int MAX_BODY_BYTES = 16 * 1024;
    private static final int MAX_EXTENSION_ASSET_BYTES = 8 * 1024 * 1024;
    private static final int MAX_CONSOLE_STREAMS = 4;

    private final ServerBridge bridge;
    private final MyDashConfig config;
    private final MyDashSecurity security;
    private final Supplier<Collection<DashboardExtension>> extensions;
    private final ConsoleBuffer consoleBuffer;
    private final ServerPropertiesStore serverProperties;
    private final SafeFileStore fileStore;
    private final Semaphore consoleStreams = new Semaphore(MAX_CONSOLE_STREAMS);

    private HttpServer server;
    private ExecutorService executor;

    EmbeddedDashboardServer(
        ServerBridge bridge,
        MyDashConfig config,
        MyDashSecurity security,
        Supplier<Collection<DashboardExtension>> extensions,
        ConsoleBuffer consoleBuffer
    ) {
        this.bridge = bridge;
        this.config = config;
        this.security = security;
        this.extensions = extensions;
        this.consoleBuffer = consoleBuffer;
        this.serverProperties = new ServerPropertiesStore(bridge.serverDirectory());
        this.fileStore = new SafeFileStore(bridge.serverDirectory());
    }

    void start() throws IOException {
        String host = config.bindAddress();
        int port = config.port();

        InetAddress address = InetAddress.getByName(host);
        if (!address.isLoopbackAddress() && !config.allowRemote()) {
            throw new IllegalStateException(
                "Remote binding is disabled. Set server.allowRemote=true only after securing access "
                    + "with HTTPS or a trusted reverse proxy."
            );
        }

        server = HttpServer.create(new InetSocketAddress(address, port), 0);
        executor = Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable, "myDash-http");
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);

        server.createContext("/api/v1/health", this::health);
        server.createContext("/api/v1/server", this::serverInfo);
        server.createContext("/api/v1/settings", this::settings);
        server.createContext("/api/v1/server-properties", this::serverProperties);
        server.createContext("/api/v1/files/content", this::fileContent);
        server.createContext("/api/v1/files", this::files);
        server.createContext("/api/v1/auth/rotate", this::rotateToken);
        server.createContext("/api/v1/console/stream", this::consoleStream);
        server.createContext("/api/v1/console", this::console);
        server.createContext("/api/v1/players", this::players);
        server.createContext("/api/v1/extensions", this::extensions);
        server.createContext("/extensions/", this::extensionAsset);
        server.createContext("/", new StaticHandler());

        server.start();
        System.out.println("[myDash] Dashboard listening on http://" + host + ":" + port);
    }

    void stop() {
        HttpServer activeServer = server;
        server = null;

        if (activeServer != null) activeServer.stop(1);
        if (executor != null) executor.shutdownNow();
    }

    private void health(HttpExchange exchange) throws IOException {
        if (!method(exchange, "GET")) return;
        write(exchange, 200, "application/json; charset=utf-8",
            "{\"ok\":true,\"apiVersion\":\"1\",\"authentication\":\"bearer\"}");
    }

    private void serverInfo(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;
        if (!method(exchange, "GET")) return;
        write(exchange, 200, "application/json; charset=utf-8", Json.server(bridge.snapshot()));
    }

    private void files(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;
        if (!method(exchange, "GET")) return;

        String path = queryParameter(exchange, "path");
        if (path == null) path = "";

        try {
            List<FileEntrySnapshot> entries = fileStore.list(path);
            write(
                exchange,
                200,
                "application/json; charset=utf-8",
                Json.files(path, entries)
            );
        } catch (IOException exception) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_file_path"));
        }
    }

    private void fileContent(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;

        String path = queryParameter(exchange, "path");
        if (path == null || path.trim().isEmpty()) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("missing_file_path"));
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                String content = fileStore.readText(path);
                write(exchange, 200, "text/plain; charset=utf-8", content);
            } catch (IOException exception) {
                write(exchange, 400, "application/json; charset=utf-8", Json.error("file_not_editable"));
            }
            return;
        }

        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())
            && !"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET, PUT, POST");
            write(exchange, 405, "application/json; charset=utf-8", Json.error("method_not_allowed"));
            return;
        }

        byte[] content;
        try {
            content = readBodyBytes(exchange, SafeFileStore.MAX_EDIT_BYTES);
        } catch (BodyTooLargeException exception) {
            write(exchange, 413, "application/json; charset=utf-8", Json.error("file_too_large"));
            return;
        }

        try {
            fileStore.writeText(path, content);
            write(exchange, 200, "application/json; charset=utf-8", Json.actionAccepted("file-save"));
        } catch (IOException exception) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("file_write_rejected"));
        }
    }

    private void serverProperties(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(
                exchange,
                200,
                "application/json; charset=utf-8",
                Json.serverProperties(serverProperties.read())
            );
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())
            && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET, POST, PUT");
            write(exchange, 405, "application/json; charset=utf-8", Json.error("method_not_allowed"));
            return;
        }

        String body;
        try {
            body = readBody(exchange, MAX_BODY_BYTES);
        } catch (BodyTooLargeException exception) {
            write(exchange, 413, "application/json; charset=utf-8", Json.error("payload_too_large"));
            return;
        }

        String motd = Json.readStringField(body, "motd");
        Integer serverPort = Json.readIntField(body, "serverPort");
        Integer maxPlayers = Json.readIntField(body, "maxPlayers");
        Boolean onlineMode = Json.readBooleanField(body, "onlineMode");
        Boolean whiteList = Json.readBooleanField(body, "whiteList");
        String difficulty = Json.readStringField(body, "difficulty");
        String gamemode = Json.readStringField(body, "gamemode");
        Boolean hardcore = Json.readBooleanField(body, "hardcore");

        if (motd == null || motd.length() > 512 || motd.indexOf('\n') >= 0 || motd.indexOf('\r') >= 0) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_motd"));
            return;
        }

        if (serverPort == null || serverPort.intValue() < 1 || serverPort.intValue() > 65535) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_server_port"));
            return;
        }

        if (maxPlayers == null || maxPlayers.intValue() < 1 || maxPlayers.intValue() > 100000) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_max_players"));
            return;
        }

        if (onlineMode == null || whiteList == null || hardcore == null) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_boolean_setting"));
            return;
        }

        if (!oneOf(difficulty, "peaceful", "easy", "normal", "hard")) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_difficulty"));
            return;
        }

        if (!oneOf(gamemode, "survival", "creative", "adventure", "spectator")) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_gamemode"));
            return;
        }

        serverProperties.write(new ServerPropertiesSettings(
            motd,
            serverPort.intValue(),
            maxPlayers.intValue(),
            onlineMode.booleanValue(),
            whiteList.booleanValue(),
            difficulty,
            gamemode,
            hardcore.booleanValue()
        ));

        write(exchange, 200, "application/json; charset=utf-8", Json.serverPropertiesUpdated());
    }

    private void settings(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 200, "application/json; charset=utf-8", Json.settings(config));
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())
            && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET, POST, PUT");
            write(exchange, 405, "application/json; charset=utf-8", Json.error("method_not_allowed"));
            return;
        }

        String body;
        try {
            body = readBody(exchange, MAX_BODY_BYTES);
        } catch (BodyTooLargeException exception) {
            write(exchange, 413, "application/json; charset=utf-8", Json.error("payload_too_large"));
            return;
        }

        String bindAddress = Json.readStringField(body, "bindAddress");
        Integer port = Json.readIntField(body, "port");
        Boolean allowRemote = Json.readBooleanField(body, "allowRemote");

        if (bindAddress == null || bindAddress.trim().isEmpty() || bindAddress.length() > 255) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_bind_address"));
            return;
        }

        if (port == null || port.intValue() < 1 || port.intValue() > 65535) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_port"));
            return;
        }

        if (allowRemote == null) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_allow_remote"));
            return;
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(bindAddress.trim());
        } catch (UnknownHostException exception) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_bind_address"));
            return;
        }

        if (!address.isLoopbackAddress() && !allowRemote.booleanValue()) {
            write(exchange, 400, "application/json; charset=utf-8",
                Json.error("remote_binding_requires_allow_remote"));
            return;
        }

        config.updateServer(bindAddress.trim(), port.intValue(), allowRemote.booleanValue());
        write(exchange, 200, "application/json; charset=utf-8", Json.settingsUpdated());
    }

    private void rotateToken(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;
        if (!method(exchange, "POST")) return;

        String newToken = security.rotate(config);
        write(exchange, 200, "application/json; charset=utf-8", Json.tokenRotated(newToken));
    }

    private void extensions(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;
        if (!method(exchange, "GET")) return;
        write(exchange, 200, "application/json; charset=utf-8", Json.extensions(extensions.get()));
    }

    private void extensionAsset(HttpExchange exchange) throws IOException {
        if (!method(exchange, "GET")) return;

        String requestPath = exchange.getRequestURI().getPath();
        String prefix = "/extensions/";
        String remaining = requestPath.substring(prefix.length());

        int separator = remaining.indexOf('/');
        if (separator <= 0) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("extension_not_found"));
            return;
        }

        String extensionId = remaining.substring(0, separator);
        String relativePath = remaining.substring(separator + 1);
        if (relativePath.isEmpty()) relativePath = "index.html";

        if (relativePath.contains("..") || relativePath.indexOf('\\') >= 0) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_asset_path"));
            return;
        }

        DashboardExtension matched = null;
        for (DashboardExtension extension : extensions.get()) {
            if (extension.id().equals(extensionId)) {
                matched = extension;
                break;
            }
        }

        if (matched == null) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("extension_not_found"));
            return;
        }

        DashboardAssetProvider provider = matched.assetProvider();
        if (provider == null) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("extension_has_no_assets"));
            return;
        }

        DashboardAsset asset;
        try {
            asset = provider.open(relativePath);
        } catch (IOException exception) {
            write(exchange, 500, "application/json; charset=utf-8", Json.error("extension_asset_failed"));
            return;
        }

        if (asset == null) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("asset_not_found"));
            return;
        }

        byte[] data = asset.content();
        if (data.length > MAX_EXTENSION_ASSET_BYTES) {
            write(exchange, 413, "application/json; charset=utf-8", Json.error("asset_too_large"));
            return;
        }

        writeExtensionBytes(exchange, 200, asset.contentType(), data);
    }

    private void consoleStream(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;
        if (!method(exchange, "GET")) return;

        if (!consoleStreams.tryAcquire()) {
            write(exchange, 429, "application/json; charset=utf-8", Json.error("too_many_console_streams"));
            return;
        }

        try {
            Headers headers = exchange.getResponseHeaders();
            applyCommonHeaders(headers);
            headers.set("Content-Type", "text/event-stream; charset=utf-8");
            headers.set("X-Accel-Buffering", "no");
            headers.set("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);

            try (OutputStream output = exchange.getResponseBody()) {
                long cursor = parseAfterId(exchange.getRequestURI().getRawQuery());

                if (cursor <= 0L) {
                    List<ConsoleLine> recent = consoleBuffer.recent(200);
                    for (ConsoleLine line : recent) {
                        writeConsoleEvent(output, line);
                        cursor = line.id();
                    }
                    output.flush();
                }

                while (server != null && !Thread.currentThread().isInterrupted()) {
                    List<ConsoleLine> lines;

                    try {
                        lines = consoleBuffer.waitAfter(cursor, 100, 15000L);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    if (lines.isEmpty()) {
                        output.write(": keepalive\n\n".getBytes(StandardCharsets.UTF_8));
                    } else {
                        for (ConsoleLine line : lines) {
                            writeConsoleEvent(output, line);
                            cursor = line.id();
                        }
                    }

                    output.flush();
                }
            } catch (IOException ignored) {
                // Browser disconnected. The client reconnect loop resumes from the last event id.
            }
        } finally {
            consoleStreams.release();
        }
    }

    private static void writeConsoleEvent(OutputStream output, ConsoleLine line) throws IOException {
        String event = "id: " + line.id() + "\n"
            + "event: line\n"
            + "data: " + Json.consoleLine(line) + "\n\n";
        output.write(event.getBytes(StandardCharsets.UTF_8));
    }

    private static long parseAfterId(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) return 0L;

        String[] parts = rawQuery.split("&");
        for (String part : parts) {
            if (!part.startsWith("after=")) continue;

            try {
                long value = Long.parseLong(part.substring("after=".length()));
                return Math.max(0L, value);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }

        return 0L;
    }

    private void console(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;
        if (!method(exchange, "POST")) return;

        String body;
        try {
            body = readBody(exchange, MAX_BODY_BYTES);
        } catch (BodyTooLargeException exception) {
            write(exchange, 413, "application/json; charset=utf-8", Json.error("payload_too_large"));
            return;
        }

        String command = Json.readCommand(body);
        if (command == null) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_command"));
            return;
        }

        try {
            bridge.executeCommand(command).get(5, TimeUnit.SECONDS);
            write(exchange, 202, "application/json; charset=utf-8", Json.commandAccepted(command));
        } catch (TimeoutException exception) {
            write(exchange, 504, "application/json; charset=utf-8", Json.error("command_timeout"));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            write(exchange, 503, "application/json; charset=utf-8", Json.error("interrupted"));
        } catch (ExecutionException exception) {
            write(exchange, 500, "application/json; charset=utf-8", Json.error("command_failed"));
        }
    }

    private void players(HttpExchange exchange) throws IOException {
        if (!authorize(exchange)) return;

        String requestPath = exchange.getRequestURI().getPath();
        if ("/api/v1/players".equals(requestPath) || "/api/v1/players/".equals(requestPath)) {
            if (!method(exchange, "GET")) return;

            try {
                List<PlayerSnapshot> players = bridge.players().get(5, TimeUnit.SECONDS);
                write(exchange, 200, "application/json; charset=utf-8", Json.players(players));
            } catch (TimeoutException exception) {
                write(exchange, 504, "application/json; charset=utf-8", Json.error("players_timeout"));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                write(exchange, 503, "application/json; charset=utf-8", Json.error("interrupted"));
            } catch (ExecutionException exception) {
                write(exchange, 500, "application/json; charset=utf-8", Json.error("players_failed"));
            }
            return;
        }

        if (!method(exchange, "POST")) return;

        String prefix = "/api/v1/players/";
        if (!requestPath.startsWith(prefix)) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("not_found"));
            return;
        }

        String remaining = requestPath.substring(prefix.length());
        String[] segments = remaining.split("/");
        if (segments.length != 2) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("not_found"));
            return;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(segments[0]);
        } catch (IllegalArgumentException exception) {
            write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_uuid"));
            return;
        }

        String action = segments[1];

        String body;
        try {
            body = readBody(exchange, MAX_BODY_BYTES);
        } catch (BodyTooLargeException exception) {
            write(exchange, 413, "application/json; charset=utf-8", Json.error("payload_too_large"));
            return;
        }

        if ("kick".equals(action)) {
            String reason = Json.readOptionalReason(body);
            if (reason == null) {
                write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_reason"));
                return;
            }

            try {
                boolean kicked = bridge.kickPlayer(uuid, reason).get(5, TimeUnit.SECONDS);
                if (!kicked) {
                    write(exchange, 404, "application/json; charset=utf-8", Json.error("player_not_found"));
                    return;
                }

                write(exchange, 202, "application/json; charset=utf-8", Json.actionAccepted("kick"));
            } catch (TimeoutException exception) {
                write(exchange, 504, "application/json; charset=utf-8", Json.error("kick_timeout"));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                write(exchange, 503, "application/json; charset=utf-8", Json.error("interrupted"));
            } catch (ExecutionException exception) {
                write(exchange, 500, "application/json; charset=utf-8", Json.error("kick_failed"));
            }
            return;
        }

        if (!oneOf(action, "op", "deop", "ban", "whitelist-add", "whitelist-remove")) {
            write(exchange, 404, "application/json; charset=utf-8", Json.error("unknown_player_action"));
            return;
        }

        try {
            PlayerSnapshot target = findOnlinePlayer(uuid);
            if (target == null) {
                write(exchange, 404, "application/json; charset=utf-8", Json.error("player_not_found"));
                return;
            }

            String playerName = target.name();
            if (!isSafePlayerName(playerName)) {
                write(exchange, 500, "application/json; charset=utf-8", Json.error("unsafe_player_name"));
                return;
            }

            String command;
            if ("op".equals(action)) {
                command = "op " + playerName;
            } else if ("deop".equals(action)) {
                command = "deop " + playerName;
            } else if ("whitelist-add".equals(action)) {
                command = "whitelist add " + playerName;
            } else if ("whitelist-remove".equals(action)) {
                command = "whitelist remove " + playerName;
            } else {
                String reason = Json.readOptionalReason(body);
                if (reason == null) {
                    write(exchange, 400, "application/json; charset=utf-8", Json.error("invalid_reason"));
                    return;
                }
                command = "ban " + playerName + " " + reason;
            }

            bridge.executeCommand(command).get(5, TimeUnit.SECONDS);
            write(exchange, 202, "application/json; charset=utf-8", Json.actionAccepted(action));
        } catch (TimeoutException exception) {
            write(exchange, 504, "application/json; charset=utf-8", Json.error("player_action_timeout"));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            write(exchange, 503, "application/json; charset=utf-8", Json.error("interrupted"));
        } catch (ExecutionException exception) {
            write(exchange, 500, "application/json; charset=utf-8", Json.error("player_action_failed"));
        }
    }

    private PlayerSnapshot findOnlinePlayer(UUID uuid)
        throws InterruptedException, ExecutionException, TimeoutException {

        List<PlayerSnapshot> players = bridge.players().get(5, TimeUnit.SECONDS);
        for (PlayerSnapshot player : players) {
            if (player.uuid().equals(uuid)) return player;
        }
        return null;
    }

    private static boolean isSafePlayerName(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{1,16}");
    }

    private static boolean oneOf(String value, String... allowed) {
        if (value == null) return false;
        for (String candidate : allowed) {
            if (candidate.equals(value)) return true;
        }
        return false;
    }

    private boolean authorize(HttpExchange exchange) throws IOException {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (security.authorize(authorization)) return true;

        exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer realm=\"myDash\"");
        write(exchange, 401, "application/json; charset=utf-8", Json.error("unauthorized"));
        return false;
    }

    private boolean method(HttpExchange exchange, String method) throws IOException {
        if (method.equalsIgnoreCase(exchange.getRequestMethod())) return true;

        exchange.getResponseHeaders().set("Allow", method);
        write(exchange, 405, "application/json; charset=utf-8", Json.error("method_not_allowed"));
        return false;
    }

    private static String readBody(HttpExchange exchange, int maxBytes) throws IOException, BodyTooLargeException {
        return new String(readBodyBytes(exchange, maxBytes), StandardCharsets.UTF_8);
    }

    private static byte[] readBodyBytes(HttpExchange exchange, int maxBytes)
        throws IOException, BodyTooLargeException {

        int contentLength = parseContentLength(exchange.getRequestHeaders().getFirst("Content-Length"));
        if (contentLength > maxBytes) throw new BodyTooLargeException();

        try (InputStream input = exchange.getRequestBody(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int total = 0;
            int read;

            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) throw new BodyTooLargeException();
                output.write(buffer, 0, read);
            }

            return output.toByteArray();
        }
    }

    private static String queryParameter(HttpExchange exchange, String key) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) return null;

        for (String part : rawQuery.split("&")) {
            int equals = part.indexOf('=');
            String rawKey = equals >= 0 ? part.substring(0, equals) : part;
            if (!key.equals(decodeQuery(rawKey))) continue;

            String rawValue = equals >= 0 ? part.substring(equals + 1) : "";
            return decodeQuery(rawValue);
        }

        return null;
    }

    private static String decodeQuery(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception exception) {
            return "";
        }
    }

    private static int parseContentLength(String value) {
        if (value == null) return -1;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static void writeExtensionBytes(
        HttpExchange exchange,
        int status,
        String contentType,
        byte[] bytes
    ) throws IOException {
        applyExtensionHeaders(exchange.getResponseHeaders());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void write(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        applySecurityHeaders(exchange.getResponseHeaders());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void applyCommonHeaders(Headers headers) {
        headers.set("Cache-Control", "no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
    }

    private static void applySecurityHeaders(Headers headers) {
        applyCommonHeaders(headers);
        headers.set("X-Frame-Options", "DENY");
        headers.set(
            "Content-Security-Policy",
            "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; connect-src 'self'; "
                + "object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'"
        );
    }

    private static void applyExtensionHeaders(Headers headers) {
        applyCommonHeaders(headers);
        headers.set("X-Frame-Options", "SAMEORIGIN");
        headers.set(
            "Content-Security-Policy",
            "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; connect-src 'self'; "
                + "object-src 'none'; base-uri 'none'; frame-ancestors 'self'; form-action 'self'"
        );
    }

    private static final class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                write(exchange, 405, "text/plain; charset=utf-8", "Method Not Allowed");
                return;
            }

            String requestPath = exchange.getRequestURI().getPath();
            if ("/".equals(requestPath)) requestPath = "/index.html";

            if (requestPath.contains("..") || requestPath.indexOf('\\') >= 0) {
                write(exchange, 400, "text/plain; charset=utf-8", "Bad Request");
                return;
            }

            String resource = "/mydash-web" + requestPath;
            InputStream input = EmbeddedDashboardServer.class.getResourceAsStream(resource);
            if (input == null) {
                write(exchange, 404, "text/plain; charset=utf-8", "Not Found");
                return;
            }

            byte[] data;
            try (InputStream stream = input; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[8192];
                int read;
                while ((read = stream.read(chunk)) != -1) buffer.write(chunk, 0, read);
                data = buffer.toByteArray();
            }

            Headers headers = exchange.getResponseHeaders();
            applySecurityHeaders(headers);
            headers.set("Content-Type", contentType(requestPath));
            exchange.sendResponseHeaders(200, data.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(data);
            }
        }

        private static String contentType(String path) {
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".js")) return "text/javascript; charset=utf-8";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/html; charset=utf-8";
        }
    }

    private static final class BodyTooLargeException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
