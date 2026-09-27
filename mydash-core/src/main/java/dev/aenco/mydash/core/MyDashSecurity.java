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

    private byte[] salt;
    private byte[] expectedHash;
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

        GeneratedToken generated = generateToken();
        persist(config, generated);
        return new MyDashSecurity(generated.salt, generated.hash, generated.token);
    }

    String initialToken() {
        return initialToken;
    }

    synchronized boolean authorize(String authorizationHeader) {
        if (authorizationHeader == null) return false;
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) return false;

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) return false;

        return MessageDigest.isEqual(expectedHash, hash(salt, token));
    }

    synchronized String rotate(MyDashConfig config) throws IOException {
        GeneratedToken generated = generateToken();
        persist(config, generated);
        salt = generated.salt;
        expectedHash = generated.hash;
        return generated.token;
    }

    private static GeneratedToken generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        byte[] tokenBytes = new byte[32];
        random.nextBytes(salt);
        random.nextBytes(tokenBytes);

        String token = "mydash_" + Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        return new GeneratedToken(salt, hash(salt, token), token);
    }

    private static void persist(MyDashConfig config, GeneratedToken generated) throws IOException {
        config.set(SALT_KEY, Base64.getEncoder().encodeToString(generated.salt));
        config.set(HASH_KEY, Base64.getEncoder().encodeToString(generated.hash));
        config.save();
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

    private static final class GeneratedToken {
        private final byte[] salt;
        private final byte[] hash;
        private final String token;

        private GeneratedToken(byte[] salt, byte[] hash, String token) {
            this.salt = salt;
            this.hash = hash;
            this.token = token;
        }
    }
}
