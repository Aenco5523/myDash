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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class EmbeddedDashboardServer {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8765;

    private final ServerBridge bridge;
    private HttpServer server;
    private ExecutorService executor;

    EmbeddedDashboardServer(ServerBridge bridge) {
        this.bridge = bridge;
    }

    void start() throws IOException {
        String host = System.getProperty("mydash.host", DEFAULT_HOST);
        int port = Integer.getInteger("mydash.port", DEFAULT_PORT);

        InetAddress address = InetAddress.getByName(host);
        if (!address.isLoopbackAddress()) {
            throw new IllegalStateException(
                "Remote binding is disabled until myDash authentication is enabled. "
                    + "Use 127.0.0.1 and place an authenticated reverse proxy in front of myDash."
            );
        }

        server = HttpServer.create(new InetSocketAddress(address, port), 0);
        executor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "myDash-http");
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);

        server.createContext("/api/v1/health", exchange -> json(exchange, 200, "{\"ok\":true,\"apiVersion\":\"1\"}"));
        server.createContext("/api/v1/server", exchange -> json(exchange, 200, Json.server(bridge.snapshot())));
        server.createContext("/", new StaticHandler());

        server.start();
        System.out.println("[myDash] Dashboard listening on http://" + host + ":" + port);
    }

    void stop() {
        if (server != null) server.stop(1);
        if (executor != null) executor.shutdownNow();
    }

    private static void json(HttpExchange exchange, int status, String body) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 405, "application/json; charset=utf-8", "{\"error\":\"method_not_allowed\"}");
            return;
        }
        write(exchange, status, "application/json; charset=utf-8", body);
    }

    private static void write(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("X-Frame-Options", "DENY");
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; connect-src 'self'");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
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
            headers.set("Content-Type", contentType(path));
            headers.set("X-Content-Type-Options", "nosniff");
            headers.set("X-Frame-Options", "DENY");
            headers.set("Referrer-Policy", "no-referrer");
            headers.set("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; connect-src 'self'");
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
}
