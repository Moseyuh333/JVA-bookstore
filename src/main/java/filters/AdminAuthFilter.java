package filters;

import utils.AuthUtil;
import utils.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Enforces admin role authorization on all /api/admin/* endpoints.
 * Must be registered AFTER JwtFilter in the filter chain (web.xml).
 */
public class AdminAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Check session-based role first
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object role = session.getAttribute("role");
            if ("admin".equalsIgnoreCase(String.valueOf(role))) {
                chain.doFilter(request, response);
                return;
            }
        }

        // If no session role, try to resolve from JWT token
        try {
            String email = AuthUtil.getUserEmail(req);
            if (email != null) {
                String role = resolveRoleByEmail(email);
                if ("admin".equalsIgnoreCase(role)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        } catch (Exception ex) {
            System.err.println("AdminAuthFilter - Error resolving admin role: " + ex.getMessage());
        }

        // Not admin - reject
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"error\":\"Forbidden - Admin access required\"}");
    }

    @Override
    public void destroy() {
    }

    private String resolveRoleByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT role FROM users WHERE email = ? OR username = ?";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException ex) {
            System.err.println("AdminAuthFilter - DB error: " + ex.getMessage());
        }
        return null;
    }
}
