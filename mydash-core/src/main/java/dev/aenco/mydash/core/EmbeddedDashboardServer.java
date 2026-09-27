package dev.aenco.mydash.core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class EmbeddedDashboardServer {
    private static final int MAX_BODY_BYTES = 16 * 1024;

    private final ServerBridge bridge;
    private final MyDashConfig config;
    private final MyDashSecurity security;

    private HttpServer server;
    private ExecutorService executor;

    EmbeddedDashboardServer(ServerBridge bridge, MyDashConfig config, MyDashSecurity security) {
        this.bridge = bridge;
        this.config = config;
        this.security = security;
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
        executor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "myDash-http");
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);

        server.createContext("/api/v1/health", this::health);
        server.createContext("/api/v1/server", this::serverInfo);
        server.createContext("/api/v1/console", this::console);
        server.createContext("/", new StaticHandler());

        server.start();
        System.out.println("[myDash] Dashboard listening on http://" + host + ":" + port);
    }

    void stop() {
        if (server != null) server.stop(1);
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

            return new String(output.toByteArray(), StandardCharsets.UTF_8);
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

    private static void write(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        applySecurityHeaders(exchange.getResponseHeaders());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void applySecurityHeaders(Headers headers) {
        headers.set("Cache-Control", "no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("X-Frame-Options", "DENY");
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        headers.set(
            "Content-Security-Policy",
            "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; connect-src 'self'; "
                + "object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'"
        );
    }

    private static final class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                write(exchange, 405, "text/plain; charset=utf-8", "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) path = "/index.html";

            if (path.contains("..") || path.indexOf('\\') >= 0) {
                write(exchange, 400, "text/plain; charset=utf-8", "Bad Request");
                return;
            }

            String resource = "/mydash-web" + path;
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
            headers.set("Content-Type", contentType(path));
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
            return "text/html; charset=utf-8";
        }
    }

    private static final class BodyTooLargeException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
