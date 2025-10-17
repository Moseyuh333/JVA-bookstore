package utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

public class JwtUtil {
    private static final String DEFAULT_SECRET = "your_secret_key_must_be_at_least_256_bits_long_for_hs256_so_adding_more_text_here_to_make_it_longer";
    private static final String SECRET_KEY_STR = System.getenv("JWT_SECRET") != null && !System.getenv("JWT_SECRET").isEmpty()
            ? System.getenv("JWT_SECRET") : DEFAULT_SECRET;
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 86400000L; // 1 day in ms

    public static String generateToken(String username) {
        return generateToken(username, "user");
    }

    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public static String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object role = claims.get("role");
            return role != null ? role.toString() : "user";
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
