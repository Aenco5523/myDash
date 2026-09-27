package dev.aenco.mydash.core;

import dev.aenco.mydash.api.DashboardExtension;
import dev.aenco.mydash.api.MyDash;
import dev.aenco.mydash.api.MyDashApi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MyDashCore implements MyDashApi {
    private final ServerBridge bridge;
    private final Path configPath;
    private final List<DashboardExtension> extensions = new ArrayList<DashboardExtension>();
    private EmbeddedDashboardServer webServer;

    public MyDashCore(ServerBridge bridge, Path configPath) {
        this.bridge = bridge;
        this.configPath = configPath;
    }

    public synchronized void start() throws IOException {
        if (webServer != null) return;

        MyDashConfig config = MyDashConfig.load(configPath);
        MyDashSecurity security = MyDashSecurity.loadOrCreate(config);

        String initialToken = security.initialToken();
        if (initialToken != null) {
            System.out.println("[myDash] ============================================================");
            System.out.println("[myDash] First-run administrator token:");
            System.out.println("[myDash] " + initialToken);
            System.out.println("[myDash] Copy it now. The plaintext token is not stored on disk.");
            System.out.println("[myDash] ============================================================");
        }

        MyDash.bind(this);
        try {
            webServer = new EmbeddedDashboardServer(bridge, config, security, this::extensions);
            webServer.start();
        } catch (IOException | RuntimeException exception) {
            webServer = null;
            MyDash.unbind(this);
            throw exception;
        }
    }

    public synchronized void stop() {
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
        MyDash.unbind(this);
    }

    @Override
    public synchronized void registerExtension(DashboardExtension extension) {
        if (extension == null) throw new IllegalArgumentException("extension");
        for (DashboardExtension existing : extensions) {
            if (existing.id().equals(extension.id())) {
                throw new IllegalArgumentException("Extension already registered: " + extension.id());
            }
        }
        extensions.add(extension);
    }

    @Override
    public synchronized Collection<DashboardExtension> extensions() {
        return Collections.unmodifiableList(new ArrayList<DashboardExtension>(extensions));
    }
}
