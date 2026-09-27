package dev.aenco.mydash.api;

import java.util.Arrays;

public final class DashboardAsset {
    private final String contentType;
    private final byte[] content;

    public DashboardAsset(String contentType, byte[] content) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("contentType must not be blank");
        }
        if (content == null) throw new IllegalArgumentException("content");

        this.contentType = contentType;
        this.content = Arrays.copyOf(content, content.length);
    }

    public String contentType() {
        return contentType;
    }

    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
