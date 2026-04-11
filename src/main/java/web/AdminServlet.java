package web;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "AdminServlet", urlPatterns = {"/api/admin/clear-users"})
public class AdminServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        
        // Simple security check: only allow from localhost or with a secret param
        String secret = req.getParameter("secret");
        boolean isLocalhost = req.getRemoteAddr().equals("127.0.0.1") || req.getRemoteAddr().equals("::1");
        
        if (!isLocalhost && !"dev-secret-key-change-me".equals(secret)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"Forbidden\"}");
            }
            return;
        }
        
        try {
            DBUtil.deleteAllUsers();
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"message\":\"All users deleted successfully\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        }
    }
}
