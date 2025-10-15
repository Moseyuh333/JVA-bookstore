package web;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "VerifyServlet", urlPatterns = {"/api/auth/verify"})
public class VerifyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("token");

        if (token == null || token.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Verification token required");
            return;
        }

        try {
            if (DBUtil.verifyUser(token)) {
                resp.sendRedirect("/login.jsp?verified=true");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or expired verification token");
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
