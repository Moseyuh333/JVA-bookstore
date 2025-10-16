package web;

import models.User;
import utils.JwtUtil;
import utils.DBUtil;
import utils.EmailUtil;
import utils.OTPUtil;
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

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/login", "/api/auth/register", "/api/auth/send-otp", "/api/auth/verify-otp", "/api/auth/reset-password"})
public class AuthServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("DEBUG AuthServlet - doPost called, path: " + req.getServletPath());
        resp.setContentType("application/json");
        String path = req.getServletPath();
        PrintWriter out = resp.getWriter();
        try {
            if ("/api/login".equals(path)) {
                handleLogin(req, resp, out);
            } else if ("/api/auth/send-otp".equals(path)) {
                handleSendOTP(req, resp, out);
            } else if ("/api/auth/verify-otp".equals(path)) {
                handleVerifyOTP(req, resp, out);
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
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        System.out.println("DEBUG AuthServlet - handleLogin called");
        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            System.out.println("DEBUG AuthServlet - username: " + username + ", password provided: " + (password != null && !password.isEmpty()));

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

        // Skip email verification - allow all users to login
        System.out.println("DEBUG Login - Email verification skipped");

        String hash = DBUtil.getUserPasswordHash(username);
        System.out.println("DEBUG Login - Username: " + username + ", Hash found: " + (hash != null) + ", Hash length: " + (hash != null ? hash.length() : 0));
        System.out.println("DEBUG Login - Password input: '" + password + "', Password length: " + password.length());
        System.out.println("DEBUG Login - Hash: " + hash);
        
        // Validate hash before BCrypt check
        if (hash == null || hash.trim().isEmpty()) {
            System.out.println("DEBUG Login - Empty or null password hash");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Invalid credentials\"}");
            return;
        }
        
        if (BCrypt.checkpw(password, hash)) {
            User user = DBUtil.getUserDetailsByUsername(username); // Fetch the full user object
            if (user != null) {
                String token = JwtUtil.generateToken(user);
                System.out.println("DEBUG Login - Token generated: " + (token != null));
                String response = "{\"token\":\"" + token + "\", \"message\":\"Login successful\"}";
                System.out.println("DEBUG Login - Response: " + response);
                out.write(response);
            } else {
                // This case should ideally not happen if userExists and password check passed
                System.out.println("DEBUG Login - User not found after password check");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"User details not found after login\"}");
            }
        } else {
            System.out.println("DEBUG Login - Password check failed");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Invalid credentials\"}");
        }
        } catch (Exception e) {
            System.out.println("DEBUG Login - Exception: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Login error: " + e.getMessage() + "\"}");
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

        // Create user without verification - set as verified immediately
        DBUtil.createUserVerified(username, email, hash);
        
        // Send welcome email (optional)
        try {
            EmailUtil.sendWelcomeEmail(email, username);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            // Don't block registration if email fails
        }

        out.write("{\"message\":\"Registration successful! You can now login with your credentials.\"}");
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
    
    private void handleSendOTP(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        String email = req.getParameter("email");
        
        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email is required\"}");
            return;
        }
        
        // Check if can request new OTP (2 minutes cooldown)
        if (!OTPUtil.canRequestNewOTP(email)) {
            long remainingSeconds = OTPUtil.getRemainingCooldownSeconds(email);
            resp.setStatus(429); // Too Many Requests
            out.write("{\"error\":\"Please wait " + remainingSeconds + " seconds before requesting a new OTP\", \"remaining\":" + remainingSeconds + "}");
            return;
        }
        
        // Generate and store OTP
        String otp = OTPUtil.generateOTP();
        if (OTPUtil.storeOTP(email, otp)) {
            // Send OTP via email
            try {
                EmailUtil.sendOTPEmail(email, otp);
                System.out.println("OTP sent to: " + email);
                out.write("{\"message\":\"OTP has been sent to your email\"}");
            } catch (Exception e) {
                System.err.println("Failed to send OTP email: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\":\"Failed to send OTP email\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to generate OTP\"}");
        }
    }
    
    private void handleVerifyOTP(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        String email = req.getParameter("email");
        String otp = req.getParameter("otp");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        if (email == null || otp == null || username == null || password == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email, OTP, username, and password are required\"}");
            return;
        }
        
        // Verify OTP
        if (OTPUtil.verifyOTP(email, otp)) {
            // Check if username already exists
            if (DBUtil.userExists(username)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write("{\"error\":\"Username already exists\"}");
                return;
            }
            
            // Check if email already registered
            if (DBUtil.emailExists(email)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write("{\"error\":\"Email already registered\"}");
                return;
            }
            
            // Create user account
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            DBUtil.createUserVerified(username, email, hash);
            
            // Send welcome email
            try {
                EmailUtil.sendWelcomeEmail(email, username);
            } catch (Exception e) {
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }
            
            System.out.println("User registered successfully: " + username);
            out.write("{\"message\":\"Registration successful! You can now login.\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Invalid or expired OTP. Please try again.\"}");
        }
    }
}
