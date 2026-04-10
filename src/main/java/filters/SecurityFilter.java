package filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security Filter that adds essential security headers to all responses
 * and provides CSRF protection for state-changing requests.
 */
public class SecurityFilter implements Filter {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CSRF_TOKEN_ATTR = "_csrf_token";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final String CSRF_PARAM = "_csrf";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // === Security Headers ===
        // Prevent MIME type sniffing
        resp.setHeader("X-Content-Type-Options", "nosniff");
        // Prevent clickjacking
        resp.setHeader("X-Frame-Options", "DENY");
        // Enable XSS protection (legacy browsers)
        resp.setHeader("X-XSS-Protection", "1; mode=block");
        // Referrer policy - don't leak full URL
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // Restrict permissions
        resp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        // Content Security Policy - allow same-origin, inline scripts (for JSP), and common CDNs
        resp.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://unpkg.com; " +
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com https://unpkg.com; " +
                "font-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.gstatic.com; " +
                "img-src 'self' data: https: blob:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'");
        // Cache control for sensitive pages
        String path = req.getRequestURI();
        if (path != null && (path.contains("/api/") || path.contains("/admin"))) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            resp.setHeader("Pragma", "no-cache");
        }

        // === CSRF Protection ===
        String method = req.getMethod();
        boolean isStateChanging = "POST".equalsIgnoreCase(method) ||
                                  "PUT".equalsIgnoreCase(method) ||
                                  "DELETE".equalsIgnoreCase(method);
        boolean isApi = path != null && path.contains("/api/");

        if (isStateChanging && !isApi) {
            // For non-API form submissions, validate CSRF token
            HttpSession session = req.getSession(false);
            if (session != null) {
                String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
                String requestToken = req.getHeader(CSRF_HEADER);
                if (requestToken == null) {
                    requestToken = req.getParameter(CSRF_PARAM);
                }
                if (sessionToken != null && !sessionToken.equals(requestToken)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"error\":\"CSRF validation failed\"}");
                    return;
                }
            }
        }

        // Ensure CSRF token exists in session
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(CSRF_TOKEN_ATTR) == null) {
            session.setAttribute(CSRF_TOKEN_ATTR, generateCsrfToken());
        }

        // === Secure Cookie Flags ===
        // Add cookie security headers
        if (resp.containsHeader("Set-Cookie")) {
            String existingCookie = resp.getHeader("Set-Cookie");
            if (existingCookie != null && !existingCookie.contains("SameSite")) {
                // This is a best-effort approach; in Servlet 4.0+ we can use Cookie API directly
                resp.setHeader("Set-Cookie", existingCookie + "; SameSite=Lax");
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Generate a cryptographically strong CSRF token.
     */
    public static String generateCsrfToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Get the current CSRF token from the session, creating one if needed.
     */
    public static String getCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (token == null) {
            token = generateCsrfToken();
            session.setAttribute(CSRF_TOKEN_ATTR, token);
        }
        return token;
    }
}
