package dev.aenco.mydash.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

final class MyDashSecurity {
    private static final String SALT_KEY = "auth.adminTokenSalt";
    private static final String HASH_KEY = "auth.adminTokenHash";

    private final byte[] salt;
    private final byte[] expectedHash;
    private final String initialToken;

    private MyDashSecurity(byte[] salt, byte[] expectedHash, String initialToken) {
        this.salt = salt;
        this.expectedHash = expectedHash;
        this.initialToken = initialToken;
    }

    static MyDashSecurity loadOrCreate(MyDashConfig config) throws IOException {
        String saltValue = config.get(SALT_KEY);
        String hashValue = config.get(HASH_KEY);

        if (saltValue != null && hashValue != null) {
            try {
                return new MyDashSecurity(
                    Base64.getDecoder().decode(saltValue),
                    Base64.getDecoder().decode(hashValue),
                    null
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Invalid myDash authentication data in configuration", exception);
            }
        }

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        byte[] tokenBytes = new byte[32];
        random.nextBytes(salt);
        random.nextBytes(tokenBytes);

        String token = "mydash_" + Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        byte[] hash = hash(salt, token);

        config.set(SALT_KEY, Base64.getEncoder().encodeToString(salt));
        config.set(HASH_KEY, Base64.getEncoder().encodeToString(hash));
        config.save();

        return new MyDashSecurity(salt, hash, token);
    }

    String initialToken() {
        return initialToken;
    }

    boolean authorize(String authorizationHeader) {
        if (authorizationHeader == null) return false;
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) return false;

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) return false;

        return MessageDigest.isEqual(expectedHash, hash(salt, token));
    }

    private static byte[] hash(byte[] salt, String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            digest.update(token.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is not available", impossible);
        }
    }
}
