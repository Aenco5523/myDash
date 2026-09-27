package dev.aenco.mydash.platform.neoforge;

import dev.aenco.mydash.core.MyDashCore;
import dev.aenco.mydash.core.PlayerSnapshot;
import dev.aenco.mydash.core.ServerBridge;
import dev.aenco.mydash.core.ServerSnapshot;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mod(MyDashNeoForge.MOD_ID)
public final class MyDashNeoForge {
    public static final String MOD_ID = "mydash";\n
    private MyDashCore core;
    private long startedAt;

    public MyDashNeoForge(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onServerStarted(ServerStartedEvent event) {
        start(event.getServer());
    }

    private void start(MinecraftServer server) {
        startedAt = System.currentTimeMillis();

        ServerBridge bridge = new ServerBridge() {
            @Override
            public ServerSnapshot snapshot() {
                return new ServerSnapshot(
                    "NeoForge",
                    SharedConstants.getCurrentVersion().getName(),
                    server.getPlayerCount(),
                    server.getMaxPlayers(),
                    System.currentTimeMillis() - startedAt
                );
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
        } catch (IOException | RuntimeException exception) {
            core = null;
            throw new IllegalStateException("Failed to start myDash web server", exception);
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        stop();
    }

    private void stop() {
        if (core != null) {
            core.stop();
            core = null;
        }
    }
}
