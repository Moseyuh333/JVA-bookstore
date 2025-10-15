package web;

import utils.EmailUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TestEmailServlet", urlPatterns = {"/api/test-email"})
public class TestEmailServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        try {
            String email = req.getParameter("email");
            
            if (email == null || email.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Email address is required\"}");
                return;
            }
            
            System.out.println("=== Test Email Request ===");
            System.out.println("Recipient: " + email);
            
            // Send test email
            EmailUtil.testEmailConnection(email);
            
            System.out.println("Test email sent successfully");
            out.write("{\"message\":\"Test email sent successfully to " + email + "\"}");
        } catch (Exception e) {
            System.err.println("Error in TestEmailServlet: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to send email: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}
