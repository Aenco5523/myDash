package dev.aenco.mydash.platform.neoforge;

import dev.aenco.mydash.core.MyDashCore;
import dev.aenco.mydash.core.ServerBridge;
import dev.aenco.mydash.core.ServerSnapshot;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.io.IOException;

@Mod(MyDashNeoForge.MOD_ID)
public final class MyDashNeoForge {
    public static final String MOD_ID = "mydash";

    private MyDashCore core;
    private long startedAt;

    public MyDashNeoForge(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        startedAt = System.currentTimeMillis();

        ServerBridge bridge = () -> new ServerSnapshot(
            "NeoForge",
            SharedConstants.getCurrentVersion().getName(),
            server.getPlayerCount(),
            server.getMaxPlayers(),
            System.currentTimeMillis() - startedAt
        );

        core = new MyDashCore(bridge);
        try {
            core.start();
        } catch (IOException | RuntimeException exception) {
            core = null;
            throw new IllegalStateException("Failed to start myDash web server", exception);
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        if (core != null) {
            core.stop();
            core = null;
        }
    }
}
