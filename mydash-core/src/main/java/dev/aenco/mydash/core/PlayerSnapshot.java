package dev.aenco.mydash.core;

import java.util.UUID;

public final class PlayerSnapshot {
    private final UUID uuid;
    private final String name;
    private final boolean operator;

    public PlayerSnapshot(UUID uuid, String name, boolean operator) {
        this.uuid = uuid;
        this.name = name;
        this.operator = operator;
    }

    public UUID uuid() { return uuid; }
    public String name() { return name; }
    public boolean operator() { return operator; }
}
