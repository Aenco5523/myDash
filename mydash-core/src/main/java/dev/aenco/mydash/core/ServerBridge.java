package dev.aenco.mydash.core;

import java.util.concurrent.CompletableFuture;

public interface ServerBridge {
    ServerSnapshot snapshot();

    CompletableFuture<Void> executeCommand(String command);
}
