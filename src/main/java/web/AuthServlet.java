package web;

import utils.JwtUtil;
import utils.DBUtil;
import utils.EmailUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/login", "/api/auth/register", "/api/auth/reset-password"})
public class AuthServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getServletPath();

        try (PrintWriter out = resp.getWriter()) {
            if ("/api/login".equals(path)) {
                handleLogin(req, resp, out);
            } else if ("/api/auth/register".equals(path)) {
                handleRegister(req, resp, out);
            } else if ("/api/auth/reset-password".equals(path)) {
                handleResetPassword(req, resp, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Username and password required\"}");
            return;
        }

        if (!DBUtil.userExists(username)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Invalid credentials\"}");
            return;
        }

        // Check if user is verified
        boolean isVerified = DBUtil.isUserVerified(username);
        System.out.println("DEBUG Login - User verified: " + isVerified);
        if (!isVerified) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write("{\"error\":\"Account not verified. Please check your email.\"}");
            return;
        }

        String hash = DBUtil.getUserPasswordHash(username);
        System.out.println("DEBUG Login - Username: " + username + ", Hash found: " + (hash != null));
        if (hash != null && BCrypt.checkpw(password, hash)) {
            String token = JwtUtil.generateToken(username);
            System.out.println("DEBUG Login - Token generated: " + (token != null));
            String response = "{\"token\":\"" + token + "\", \"message\":\"Login successful\"}";
            System.out.println("DEBUG Login - Response: " + response);
            out.write(response);
        } else {
            System.out.println("DEBUG Login - Password check failed");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Invalid credentials\"}");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        String email = req.getParameter("email");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (email == null || email.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email, username, and password required\"}");
            return;
        }

        if (DBUtil.userExists(username)) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.write("{\"error\":\"Username already exists\"}");
            return;
        }

        if (DBUtil.emailExists(email)) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.write("{\"error\":\"Email already registered\"}");
            return;
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String verificationToken = UUID.randomUUID().toString();

        DBUtil.createUser(username, email, hash, verificationToken);
        EmailUtil.sendVerificationEmail(email, verificationToken, username);

        out.write("{\"message\":\"Registration pending. Please check your email to verify your account before logging in.\"}");
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        String email = req.getParameter("email");

        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email required\"}");
            return;
        }

        if (!DBUtil.emailExists(email)) {
            // Don't reveal if email exists for security
            out.write("{\"message\":\"If the email exists, a reset link has been sent.\"}");
            return;
        }

        String username = DBUtil.getUserByEmail(email);
        String resetToken = UUID.randomUUID().toString();

        if (DBUtil.setResetToken(email, resetToken)) {
            EmailUtil.sendResetEmail(email, resetToken, username);
        }

        out.write("{\"message\":\"If the email exists, a reset link has been sent.\"}");
    }
}
