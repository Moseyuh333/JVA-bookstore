package filters;

import utils.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "JwtFilter", urlPatterns = {"/api/*"})
public class JwtFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath() != null ? req.getContextPath() : "";
        String path = requestUri.substring(contextPath.length());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        System.out.println("JwtFilter: Request URI = " + requestUri + " | normalized path = " + path);

        if (isPublicEndpoint(path, req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            String token = authHeader.substring(7);
            String user = JwtUtil.validateToken(token);
            if (user != null) {
                chain.doFilter(request, response);
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"Unauthorized\"}");
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }

    private boolean isPublicEndpoint(String path, String method) {
        if (path == null) {
            return false;
        }

        // Core auth endpoints remain public
        switch (path) {
            case "/api/auth/register":
            case "/api/auth/login":
            case "/api/login":
            case "/api/auth/send-otp":
            case "/api/auth/verify-otp":
            case "/api/auth/reset-password":
            case "/api/auth/reset":
            case "/api/auth/verify":
            case "/api/admin/clear-users":
            case "/api/test-email":
            case "/api/health":
                return true;
            default:
                break;
        }

        // Allow anyone to browse catalog and category metadata.
        if ("GET".equalsIgnoreCase(method)) {
            if (path.equals("/api/books") || path.startsWith("/api/books/")) {
                return true;
            }
            if (path.equals("/api/catalog") || path.startsWith("/api/catalog/")) {
                return true;
            }
        }

        return false;
    }
}
