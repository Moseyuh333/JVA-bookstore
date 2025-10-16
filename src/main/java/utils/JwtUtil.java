package utils;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "your-super-secret-and-long-enough-key-for-hs256-algorithm-jva-bookstore";
    private static final long EXPIRATION_TIME_MS = 864_000_000; // 10 days

    public static String generateToken(User user) {
        Gson gson = new Gson();
        User tokenUser = new User();
        tokenUser.setId(user.getId());
        tokenUser.setEmail(user.getEmail());
        tokenUser.setFullName(user.getFullName());
        tokenUser.setRole(user.getRole());
        
        String userJson = gson.toJson(tokenUser);

        return Jwts.builder()
                .setSubject(userJson)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public static User getSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            String userJson = claims.getSubject();
            return new Gson().fromJson(userJson, User.class);
        } catch (Exception e) {
            System.err.println("JWT parsing error: " + e.getMessage());
            return null;
        }
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getTokenFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
