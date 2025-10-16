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

        String path = req.getRequestURI();
        System.out.println("JwtFilter: Request URI = " + path);
        // Allow public endpoints without token
        if (path.equals("/api/auth/register") || 
            path.equals("/api/auth/login") || 
            path.equals("/api/login") || 
            path.equals("/api/auth/send-otp") ||
            path.equals("/api/auth/verify-otp") ||
            path.equals("/api/auth/reset-password") || 
            path.equals("/api/auth/reset") || 
            path.equals("/api/auth/verify") ||
            path.equals("/api/admin/clear-users") ||
            path.equals("/api/test-email")) {
            chain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String token = JwtUtil.getTokenFromRequest(req);
        if (token != null && JwtUtil.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"Unauthorized\"}");
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
