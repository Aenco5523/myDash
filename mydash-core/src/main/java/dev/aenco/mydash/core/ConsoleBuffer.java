package dev.aenco.mydash.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

final class ConsoleBuffer {
    private static final int CAPACITY = 750;
    private static final int MAX_MESSAGE_LENGTH = 8192;

    private final Deque<ConsoleLine> lines = new ArrayDeque<ConsoleLine>();
    private long nextId = 1L;
    private boolean closed;

    synchronized void publish(String level, String logger, String message) {
        if (closed) return;

        ConsoleLine line = new ConsoleLine(
            nextId++,
            System.currentTimeMillis(),
            trim(level, 24, "INFO"),
            trim(logger, 256, "Minecraft"),
            trim(message, MAX_MESSAGE_LENGTH, "")
        );

        lines.addLast(line);
        while (lines.size() > CAPACITY) {
            lines.removeFirst();
        }

        notifyAll();
    }

    synchronized List<ConsoleLine> recent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, CAPACITY));
        int skip = Math.max(0, lines.size() - safeLimit);

        List<ConsoleLine> result = new ArrayList<ConsoleLine>();
        int index = 0;
        for (ConsoleLine line : lines) {
            if (index++ >= skip) result.add(line);
        }
        return result;
    }

    synchronized List<ConsoleLine> waitAfter(long afterId, int limit, long timeoutMillis)
        throws InterruptedException {

        long deadline = System.currentTimeMillis() + Math.max(1L, timeoutMillis);

        while (!closed) {
            List<ConsoleLine> result = after(afterId, limit);
            if (!result.isEmpty()) return result;

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0L) return result;

            wait(remaining);
        }

        return new ArrayList<ConsoleLine>();
    }

    synchronized void close() {
        closed = true;
        notifyAll();
    }

    private List<ConsoleLine> after(long afterId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, CAPACITY));
        List<ConsoleLine> result = new ArrayList<ConsoleLine>();

        for (ConsoleLine line : lines) {
            if (line.id() <= afterId) continue;
            result.add(line);
            if (result.size() >= safeLimit) break;
        }

        return result;
    }

    private static String trim(String value, int maxLength, String fallback) {
        String clean = value == null ? fallback : value;
        if (clean.length() <= maxLength) return clean;
        return clean.substring(0, maxLength);
    }
}
