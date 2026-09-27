package dev.aenco.mydash.platform.paper1201;

import dev.aenco.mydash.core.MyDashCore;
import dev.aenco.mydash.core.PlayerSnapshot;
import dev.aenco.mydash.core.ServerBridge;
import dev.aenco.mydash.core.ServerSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MyDashPaper1201 extends JavaPlugin {
    private MyDashCore core;
    private MyDashLogAppender logAppender;
    private long startedAt;

    @Override
    public void onEnable() {
        startedAt = System.currentTimeMillis();

        ServerBridge bridge = new ServerBridge() {
            @Override
            public ServerSnapshot snapshot() {
                return new ServerSnapshot(
                    "Paper",
                    Bukkit.getMinecraftVersion(),
                    Bukkit.getOnlinePlayers().size(),
                    Bukkit.getMaxPlayers(),
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
                Bukkit.getScheduler().runTask(MyDashPaper1201.this, () -> {
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
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
                Bukkit.getScheduler().runTask(MyDashPaper1201.this, () -> {
                    try {
                        List<PlayerSnapshot> result = new ArrayList<PlayerSnapshot>();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            result.add(new PlayerSnapshot(player.getUniqueId(), player.getName(), player.isOp()));
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
                Bukkit.getScheduler().runTask(MyDashPaper1201.this, () -> {
                    try {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) {
                            future.complete(false);
                            return;
                        }
                        player.kickPlayer(reason);
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
            core.publishConsoleLine("INFO", "myDash", "Paper 1.20.1 live console capture attached.");
        } catch (IOException | RuntimeException exception) {
            if (logAppender != null) {
                logAppender.close();
                logAppender = null;
            }
            if (core != null) {
                core.stop();
                core = null;
            }
            throw new IllegalStateException("Failed to start myDash on Paper 1.20.1", exception);
        }
    }

    @Override
    public void onDisable() {
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
