package dev.aenco.mydash.platform.fabric1182;

import dev.aenco.mydash.core.MyDashCore;
import dev.aenco.mydash.core.PlayerSnapshot;
import dev.aenco.mydash.core.ServerBridge;
import dev.aenco.mydash.core.ServerSnapshot;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MyDashFabric1182 implements ModInitializer {

    private MyDashCore core;
    private MyDashLogAppender logAppender;
    private long startedAt;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::start);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> stop());
    }

    private void start(MinecraftServer server) {
        startedAt = System.currentTimeMillis();

        ServerBridge bridge = new ServerBridge() {
            @Override
            public ServerSnapshot snapshot() {
                return new ServerSnapshot(
                    "Fabric",
                    SharedConstants.getCurrentVersion().getName(),
                    server.getPlayerCount(),
                    server.getMaxPlayers(),
                    System.currentTimeMillis() - startedAt
                );
            }

            @Override
            public Path serverDirectory() {
                return Paths.get(".").toAbsolutePath().normalize();
            }

            @Override
            public CompletableFuture<Void> executeCommand(String command) {
                CompletableFuture<Void> future = new CompletableFuture<Void>();
                server.execute(() -> {
                    try {
                        server.getCommands().performCommand(server.createCommandSourceStack(), command);
                        future.complete(null);
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });
                return future;
            }

            @Override
            public CompletableFuture<List<PlayerSnapshot>> players() {
                CompletableFuture<List<PlayerSnapshot>> future = new CompletableFuture<List<PlayerSnapshot>>();
                server.execute(() -> {
                    try {
                        List<PlayerSnapshot> result = new ArrayList<PlayerSnapshot>();
                        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                            result.add(new PlayerSnapshot(
                                player.getUUID(),
                                player.getGameProfile().getName(),
                                server.getPlayerList().isOp(player.getGameProfile())
                            ));
                        }
                        future.complete(result);
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });
                return future;
            }

            @Override
            public CompletableFuture<Boolean> kickPlayer(UUID uuid, String reason) {
                CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
                server.execute(() -> {
                    try {
                        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                        if (player == null) {
                            future.complete(false);
                            return;
                        }
                        player.connection.disconnect(new TextComponent(reason));
                        future.complete(true);
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });
                return future;
            }
        };

        core = new MyDashCore(bridge, Paths.get("config", "mydash.properties"));

        try {
            core.start();
            logAppender = MyDashLogAppender.install(core);
            core.publishConsoleLine("INFO", "myDash", "Fabric 1.18.2 live console capture attached.");
        } catch (IOException | RuntimeException exception) {
            if (logAppender != null) {
                logAppender.close();
                logAppender = null;
            }
            if (core != null) {
                core.stop();
                core = null;
            }
            throw new IllegalStateException("Failed to start myDash on Fabric 1.18.2", exception);
        }
    }

    private void stop() {
        if (logAppender != null) {
            logAppender.close();
            logAppender = null;
        }
        if (core != null) {
            core.stop();
            core = null;
        }
    }
}
