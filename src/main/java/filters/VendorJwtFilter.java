package filters;

import utils.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "VendorJwtFilter", urlPatterns = {"/vendor/*"})
public class VendorJwtFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Missing token\"}");
            return;
        }
        String token = auth.substring(7);
        String username = JwtUtil.validateToken(token);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }
        String role = JwtUtil.getRoleFromToken(token);
        if (role == null || (!"vendor".equals(role) && !"staff".equals(role))) {
            // For now accept only vendor or staff (staff handling will require store check later)
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Insufficient role\"}");
            return;
        }

        // set attributes for downstream servlets
        req.setAttribute("authenticatedUsername", username);
        req.setAttribute("authenticatedRole", role);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
