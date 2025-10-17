package utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AuthUtil {

    public static final String ATTR_USER_ID = "AUTH_USER_ID";

    private AuthUtil() {
    }

    public static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ") && bearerToken.length() > 7) {
            return bearerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static String getUserEmail(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return JwtUtil.validateToken(token);
    }

    public static Long resolveUserId(HttpServletRequest request) throws SQLException {
        Object cached = request.getAttribute(ATTR_USER_ID);
        if (cached instanceof Long) {
            return (Long) cached;
        }
        String email = getUserEmail(request);
        if (email == null) {
            return null;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long userId = rs.getLong(1);
                    request.setAttribute(ATTR_USER_ID, userId);
                    return userId;
                }
            }
        }
        return null;
    }
}
