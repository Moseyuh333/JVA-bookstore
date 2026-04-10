package web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * DISABLED for security. Mass user deletion endpoint removed.
 * This endpoint previously allowed deleting all users with only a
 * hardcoded secret or localhost check - a catastrophic security flaw.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/api/admin/clear-users"})
public class AdminServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_GONE);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"This endpoint has been permanently disabled for security\"}");
    }
}
