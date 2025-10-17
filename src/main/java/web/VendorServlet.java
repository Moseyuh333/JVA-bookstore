package web;

import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "VendorServlet", urlPatterns = {"/vendor/*"})
public class VendorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            out.write("{\"message\":\"Vendor API root. Available: /profile, /stores\"}");
            return;
        }
        if (path.startsWith("/profile")) {
            // Return simple profile info based on token
            String auth = req.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Missing token\"}");
                return;
            }
            String token = auth.substring(7);
            String username = JwtUtil.validateToken(token);
            if (username == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Invalid token\"}");
                return;
            }
            String role = JwtUtil.getRoleFromToken(token);
            out.write("{\"username\":\"" + username + "\", \"role\":\"" + (role != null ? role : "user") + "\"}");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.write("{\"error\":\"Not implemented\"}");
    }
}
