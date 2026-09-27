package dev.aenco.mydash.core;

public final class ConsoleLine {
    private final long id;
    private final long timestamp;
    private final String level;
    private final String logger;
    private final String message;

    ConsoleLine(long id, long timestamp, String level, String logger, String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.message = message;
    }

    public long id() { return id; }
    public long timestamp() { return timestamp; }
    public String level() { return level; }
    public String logger() { return logger; }
    public String message() { return message; }
}
