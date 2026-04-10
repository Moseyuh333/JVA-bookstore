package utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 86400000L; // 1 day in ms

    private static final SecretKey SECRET_KEY = initSecretKey();

    private static SecretKey initSecretKey() {
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret == null || envSecret.trim().isEmpty()) {
            System.err.println("WARNING: JWT_SECRET environment variable is not set. Using a generated fallback key. SET JWT_SECRET in production!");
            // Generate a secure random key as fallback for development only
            return Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        }
        byte[] keyBytes = envSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes (256 bits) long for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // Verify token is not expired
            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return null;
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
