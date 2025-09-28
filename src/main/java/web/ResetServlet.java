package web;

import utils.DBUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "ResetServlet", urlPatterns = {"/api/auth/reset"})
public class ResetServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String token = req.getParameter("token");
        String newPassword = req.getParameter("password");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"Token and new password required\"}");
            }
            return;
        }

        try (PrintWriter out = resp.getWriter()) {
            String email = DBUtil.getResetToken(token);
            if (email != null) {
                String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                if (DBUtil.updatePassword(email, hash)) {
                    out.write("{\"message\":\"Password reset successful. Please login with your new password.\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write("{\"error\":\"Failed to update password\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid or expired reset token\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        }
    }
}
