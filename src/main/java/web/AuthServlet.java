package web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/login", "/api/auth/register", "/api/auth/send-otp", "/api/auth/verify-otp", "/api/auth/reset-password", "/api/verify"})
public class AuthServlet extends HttpServlet {

    private static final String ATTR_JSON_BODY = "AUTH_SERVLET_JSON_BODY";

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
            String subject = DBUtil.getEmailByUsername(username);
            if (subject == null || subject.trim().isEmpty()) {
                subject = username;
            }

            String token = JwtUtil.generateToken(subject);

            System.out.println("DEBUG Login - Token generated: " + (token != null));
            System.out.println("DEBUG Login - Token generated: " + (token != null));

            // Check user role for redirect
            String role = DBUtil.getUserRole(username);
            String response;
            if ("admin".equals(role)) {
                response = "{\"token\":\"" + token + "\", \"message\":\"Login successful\", \"redirect\":\"/admin-dashboard\"}";
            } else if ("shipper".equals(role)) {
                response = "{\"token\":\"" + token + "\", \"message\":\"Login successful\", \"redirect\":\"/dashboard-shipper.jsp\"}";
            } else {
                response = "{\"token\":\"" + token + "\", \"message\":\"Login successful\"}";
            }
            System.out.println("DEBUG Login - Response: " + response);
            out.write(response);
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
        String email = extractParam(req, "email");
        String username = extractParam(req, "username");
        String password = extractParam(req, "password");

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
            JsonObject payload = new JsonObject();
            payload.addProperty("message", "If the email exists, a reset link has been sent.");
            out.write(payload.toString());
            return;
        }

        String username = DBUtil.getUserByEmail(email);
        String resetToken = UUID.randomUUID().toString();
        JsonObject payload = new JsonObject();
        payload.addProperty("message", "If the email exists, a reset link has been sent.");
        boolean tokenStored = DBUtil.setResetToken(email, resetToken);

        if (tokenStored) {
            try {
                EmailUtil.sendResetEmail(email, resetToken, username);
            } catch (RuntimeException mailEx) {
                if (EmailUtil.isEmailEnabled()) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JsonObject errorPayload = new JsonObject();
                    errorPayload.addProperty("error", "Failed to send reset email. Please try again later.");
                    out.write(errorPayload.toString());
                    return;
                }
                System.err.println("DEBUG ResetPassword - Email disabled, token returned in response.");
            }
        } else {
            System.err.println("DEBUG ResetPassword - Unable to persist reset token for email: " + email);
        }

        if (tokenStored && !EmailUtil.isEmailEnabled()) {
            payload.addProperty("debugToken", resetToken);
            String requestUrl = req.getRequestURL().toString();
            String requestUri = req.getRequestURI();
            String baseUrl = requestUrl.substring(0, requestUrl.length() - requestUri.length());
            payload.addProperty("debugResetUrl", baseUrl + req.getContextPath() + "/reset-password.jsp?token=" + resetToken);
        }

        out.write(payload.toString());
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
            JsonObject payload = new JsonObject();
            try {
                EmailUtil.sendOTPEmail(email, otp);
                System.out.println("OTP sent to: " + email);
            } catch (RuntimeException mailEx) {
                if (EmailUtil.isEmailEnabled()) {
                    System.err.println("Failed to send OTP email: " + mailEx.getMessage());
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JsonObject errorPayload = new JsonObject();
                    errorPayload.addProperty("error", "Failed to send OTP email");
                    out.write(errorPayload.toString());
                    return;
                }
                System.err.println("DEBUG OTP - Email disabled, returning OTP in response.");
            }

            if (EmailUtil.isEmailEnabled()) {
                payload.addProperty("message", "OTP has been sent to your email");
            } else {
                payload.addProperty("message", "OTP generated (email delivery disabled)");
                payload.addProperty("debugOtp", otp);
            }

            out.write(payload.toString());
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to generate OTP\"}");
        }
    }
    
    private void handleVerifyOTP(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException, SQLException {
        String email = extractParam(req, "email");
        String otp = extractParam(req, "otp");
        String username = extractParam(req, "username");
        String password = extractParam(req, "password");
        
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

    private String extractParam(HttpServletRequest req, String name) throws IOException {
        String value = req.getParameter(name);
        if (value != null) {
            value = value.trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
        JsonObject json = getJsonBody(req);
        if (json != null) {
            JsonElement element = json.get(name);
            if (element != null && !element.isJsonNull()) {
                String extracted = element.getAsString();
                if (extracted != null) {
                    extracted = extracted.trim();
                    if (!extracted.isEmpty()) {
                        return extracted;
                    }
                }
            }
        }
        return null;
    }

    private JsonObject getJsonBody(HttpServletRequest req) throws IOException {
        Object cached = req.getAttribute(ATTR_JSON_BODY);
        if (cached instanceof JsonObject) {
            return (JsonObject) cached;
        }
        if (Boolean.FALSE.equals(cached)) {
            return null;
        }

        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            req.setAttribute(ATTR_JSON_BODY, Boolean.FALSE);
            return null;
        }

        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }
        }

        if (jsonPayload.length() == 0) {
            req.setAttribute(ATTR_JSON_BODY, Boolean.FALSE);
            return null;
        }

        try {
            JsonObject json = JsonParser.parseString(jsonPayload.toString()).getAsJsonObject();
            req.setAttribute(ATTR_JSON_BODY, json);
            return json;
        } catch (Exception parseEx) {
            req.setAttribute(ATTR_JSON_BODY, Boolean.FALSE);
            System.err.println("AuthServlet - Failed to parse JSON body: " + parseEx.getMessage());
            return null;
        }
    }

}
