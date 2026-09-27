package dev.aenco.mydash.core;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ServerBridge {
    ServerSnapshot snapshot();

    Path serverDirectory();

    CompletableFuture<Void> executeCommand(String command);

    CompletableFuture<List<PlayerSnapshot>> players();

    CompletableFuture<Boolean> kickPlayer(UUID uuid, String reason);
}
