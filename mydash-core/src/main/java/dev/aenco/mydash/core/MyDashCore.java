package dev.aenco.mydash.core;

import dev.aenco.mydash.api.DashboardExtension;
import dev.aenco.mydash.api.MyDashApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MyDashCore implements MyDashApi {
    private final ServerBridge bridge;
    private final List<DashboardExtension> extensions = new ArrayList<DashboardExtension>();
    private EmbeddedDashboardServer webServer;

    public MyDashCore(ServerBridge bridge) {
        this.bridge = bridge;
    }

    public synchronized void start() throws IOException {
        if (webServer != null) return;
        webServer = new EmbeddedDashboardServer(bridge);
        webServer.start();
    }

    public synchronized void stop() {
        if (webServer == null) return;
        webServer.stop();
        webServer = null;
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
