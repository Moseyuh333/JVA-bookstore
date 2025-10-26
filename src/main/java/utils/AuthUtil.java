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
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String raw = bearerToken.length() > 7 ? bearerToken.substring(7) : "";
            String normalized = raw == null ? null : raw.trim();
            if (normalized != null && !normalized.isEmpty()
                    && !"null".equalsIgnoreCase(normalized)
                    && !"undefined".equalsIgnoreCase(normalized)) {
                return normalized;
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null) {
                        String normalizedCookie = value.trim();
                        if (!normalizedCookie.isEmpty()
                                && !"null".equalsIgnoreCase(normalizedCookie)
                                && !"undefined".equalsIgnoreCase(normalizedCookie)) {
                            return normalizedCookie;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getUserEmail(HttpServletRequest request) {
        Object cached = request.getAttribute("AUTH_USER_EMAIL");
        if (cached instanceof String) {
            return (String) cached;
        }

        String token = getTokenFromRequest(request);
        String subject = JwtUtil.validateToken(token);
        if (subject == null || subject.trim().isEmpty()) {
            return null;
        }

        String email = resolveEmail(subject);
        if (email != null) {
            request.setAttribute("AUTH_USER_EMAIL", email);
        }
        return email;
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

    private static String resolveEmail(String identity) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT email FROM users WHERE email = ? OR username = ?")) {
            stmt.setString(1, identity);
            stmt.setString(2, identity);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException ex) {
            System.err.println("AuthUtil - Unable to resolve email for identity '" + identity + "': " + ex.getMessage());
        }
        return null;
    }
}
