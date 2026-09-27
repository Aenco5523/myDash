package dev.aenco.mydash.platform.calver.forge;

import dev.aenco.mydash.core.MyDashCore;
import dev.aenco.mydash.core.PlayerSnapshot;
import dev.aenco.mydash.core.ServerBridge;
import dev.aenco.mydash.core.ServerSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mod(MyDashForgeCalver.MOD_ID)
public final class MyDashForgeCalver {
    public static final String MOD_ID = "mydash";
    private MyDashCore core;
    private MyDashLogAppender logAppender;
    private long startedAt;

    public MyDashForgeCalver() {
        ServerStartedEvent.BUS.addListener(this::onServerStarted);
        ServerStoppingEvent.BUS.addListener(this::onServerStopping);
    }

    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        startedAt = System.currentTimeMillis();

        ServerBridge bridge = new ServerBridge() {
            @Override
            public ServerSnapshot snapshot() {
                return new ServerSnapshot(
                    "Forge",
                    server.getServerVersion(),
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
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
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
                        player.connection.disconnect(Component.literal(reason));
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
            core.publishConsoleLine("INFO", "myDash", "Forge 26.x live console capture attached.");
        } catch (IOException | RuntimeException exception) {
            cleanup();
            throw new IllegalStateException("Failed to start myDash on Forge 26.x", exception);
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        cleanup();
    }

    private void cleanup() {
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
