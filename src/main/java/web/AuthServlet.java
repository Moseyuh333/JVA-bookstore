package web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import utils.JwtUtil;
import utils.DBUtil;
import utils.EmailUtil;
import utils.OTPUtil;
import dao.ShopDAO;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet(name = "AuthServlet", urlPatterns = {
        "/api/login",
        "/api/auth/register",
        "/api/auth/send-otp",
        "/api/auth/verify-otp",
        "/api/auth/reset-password"
})
public class AuthServlet extends HttpServlet {

    private static final String ATTR_JSON_BODY = "AUTH_SERVLET_JSON_BODY";

    // Rate limiting: max 5 failed login attempts per IP, reset after 15 minutes
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes
    private static final ConcurrentHashMap<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> lockoutUntil = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
            System.err.println("AuthServlet error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"An internal error occurred\"}");
        } finally {
            out.flush();
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException, SQLException {
        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Username and password required\"}");
                return;
            }

            // Rate limiting check
            String clientIp = getClientIp(req);
            if (isLockedOut(clientIp)) {
                resp.setStatus(429); // Too Many Requests
                out.write("{\"error\":\"Too many failed login attempts. Please try again later.\"}");
                return;
            }

            if (!DBUtil.userExists(username)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Invalid credentials\"}");
                return;
            }

            // Skip email verification
            String hash = DBUtil.getUserPasswordHash(username);
            if (hash == null || hash.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Invalid credentials\"}");
                return;
            }

            if (BCrypt.checkpw(password, hash)) {
                // Check user status
                String status = DBUtil.getUserStatus(username);
                if ("inactive".equalsIgnoreCase(status)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write("{\"error\":\"Tài khoản của bạn đang bị tạm khóa\"}");
                    return;
                } else if ("banned".equalsIgnoreCase(status)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write("{\"error\":\"Tài khoản của bạn đã bị cấm\"}");
                    return;
                }

                String subject = DBUtil.getEmailByUsername(username);
                if (subject == null || subject.trim().isEmpty()) {
                    subject = username;
                }

                String token = JwtUtil.generateToken(subject);
                String role = DBUtil.getUserRole(username);
                int userId = DBUtil.getUserIdByUsername(username);

                // Fix session fixation: invalidate old session before creating new one
                HttpSession oldSession = req.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = req.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("role", role);
                session.setAttribute("user_id", userId);
                session.setAttribute("token", token);

                // Clear rate limiting on successful login
                clearLoginAttempts(clientIp);

                String sellerStatus = null;
                if ("seller".equals(role)) {
                    try {
                        sellerStatus = DBUtil.getUserStatus(username);
                        int shopId = ShopDAO.getShopIdByUserId(userId);
                        if (shopId > 0) {
                            session.setAttribute("shop_id", shopId);
                        }
                    } catch (Exception e) {
                        System.err.println("Login - Failed to get seller info: " + e.getMessage());
                    }
                }

                // ✅ Trả JSON phản hồi theo role
                String response;
                if ("admin".equals(role)) {
                    response = "{\"token\":\"" + token + "\",\"message\":\"Login successful\",\"role\":\"admin\",\"redirect\":\"/admin-dashboard\"}";
                } else if ("seller".equals(role)) {
                    if ("active".equalsIgnoreCase(sellerStatus)) {
                        response = "{\"token\":\"" + token + "\",\"message\":\"Login successful\",\"role\":\"seller\",\"redirect\":\"/seller/dashboard\"}";
                    } else {
                        response = "{\"token\":\"" + token + "\",\"message\":\"Login successful\",\"role\":\"seller\",\"redirect\":\"/seller/pending\"}";
                    }
                } else if ("shipper".equals(role)) {
                    response = "{\"token\":\"" + token + "\",\"message\":\"Login successful\",\"role\":\"shipper\",\"redirect\":\"/dashboard-shipper.jsp\"}";
                } else {
                    response = "{\"token\":\"" + token + "\",\"message\":\"Login successful\",\"role\":\"customer\"}";
                }

                out.write(response);
            } else {
                recordFailedAttempt(clientIp);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Invalid credentials\"}");
            }

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"An internal error occurred\"}");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException, SQLException {
        String email = extractParam(req, "email");
        String username = extractParam(req, "username");
        String password = extractParam(req, "password");

        if (email == null || email.isEmpty() || username == null || username.isEmpty()
                || password == null || password.isEmpty()) {
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
        DBUtil.createUserVerified(username, email, hash);

        try {
            int userId = DBUtil.getUserIdByUsername(username);
            DBUtil.updateUserRole(userId, "customer", "active");
        } catch (SQLException e) {
            System.err.println("Failed to set user role: " + e.getMessage());
        }

        try {
            EmailUtil.sendWelcomeEmail(email, username);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        out.write("{\"message\":\"Registration successful! You can now login.\"}");
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException, SQLException {
        String email = req.getParameter("email");
        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email required\"}");
            return;
        }

        if (!DBUtil.emailExists(email)) {
            JsonObject payload = new JsonObject();
            payload.addProperty("message", "If the email exists, a reset link has been sent.");
            out.write(payload.toString());
            return;
        }

        String username = DBUtil.getUserByEmail(email);
        String resetToken = UUID.randomUUID().toString();
        boolean tokenStored = DBUtil.setResetToken(email, resetToken);
        JsonObject payload = new JsonObject();
        payload.addProperty("message", "If the email exists, a reset link has been sent.");

        if (tokenStored) {
            try {
                EmailUtil.sendResetEmail(email, resetToken, username);
            } catch (RuntimeException mailEx) {
                System.err.println("Reset mail failed: " + mailEx.getMessage());
            }
        }
        out.write(payload.toString());
    }

    private void handleSendOTP(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException, SQLException {
        String email = req.getParameter("email");
        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email is required\"}");
            return;
        }

        if (!OTPUtil.canRequestNewOTP(email)) {
            long remaining = OTPUtil.getRemainingCooldownSeconds(email);
            resp.setStatus(429);
            out.write("{\"error\":\"Please wait " + remaining + " seconds before requesting new OTP\"}");
            return;
        }

        String otp = OTPUtil.generateOTP();
        if (OTPUtil.storeOTP(email, otp)) {
            try {
                EmailUtil.sendOTPEmail(email, otp);
                out.write("{\"message\":\"OTP sent successfully\"}");
            } catch (Exception e) {
                System.err.println("OTP email sending failed: " + e.getMessage());
                out.write("{\"message\":\"OTP generated (email delivery may be delayed)\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to generate OTP\"}");
        }
    }

    private void handleVerifyOTP(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException, SQLException {
        String email = extractParam(req, "email");
        String otp = extractParam(req, "otp");
        String username = extractParam(req, "username");
        String password = extractParam(req, "password");

        if (email == null || otp == null || username == null || password == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Email, OTP, username, and password are required\"}");
            return;
        }

        if (OTPUtil.verifyOTP(email, otp)) {
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
            DBUtil.createUserVerified(username, email, hash);

            try {
                int userId = DBUtil.getUserIdByUsername(username);
                DBUtil.updateUserRole(userId, "customer", "active");
            } catch (SQLException e) {
                System.err.println("Failed to set user status: " + e.getMessage());
            }

            try {
                EmailUtil.sendWelcomeEmail(email, username);
            } catch (Exception e) {
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }

            out.write("{\"message\":\"Registration successful! You can now login.\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Invalid or expired OTP.\"}");
        }
    }

    private String extractParam(HttpServletRequest req, String name) throws IOException {
        String value = req.getParameter(name);
        if (value != null && !value.trim().isEmpty()) return value.trim();

        JsonObject json = getJsonBody(req);
        if (json != null && json.has(name)) {
            String val = json.get(name).getAsString();
            if (val != null && !val.trim().isEmpty()) return val.trim();
        }
        return null;
    }

    private JsonObject getJsonBody(HttpServletRequest req) throws IOException {
        Object cached = req.getAttribute(ATTR_JSON_BODY);
        if (cached instanceof JsonObject) return (JsonObject) cached;
        if (Boolean.FALSE.equals(cached)) return null;

        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            req.setAttribute(ATTR_JSON_BODY, Boolean.FALSE);
            return null;
        }

        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) jsonPayload.append(line);
        }

        if (jsonPayload.length() == 0) {
            req.setAttribute(ATTR_JSON_BODY, Boolean.FALSE);
            return null;
        }

        try {
            JsonObject json = JsonParser.parseString(jsonPayload.toString()).getAsJsonObject();
            req.setAttribute(ATTR_JSON_BODY, json);
            return json;
        } catch (Exception e) {
            req.setAttribute(ATTR_JSON_BODY, Boolean.FALSE);
            System.err.println("AuthServlet - Failed to parse JSON body: " + e.getMessage());
            return null;
        }
    }

    // === Rate Limiting Helpers ===

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private boolean isLockedOut(String ip) {
        AtomicLong until = lockoutUntil.get(ip);
        if (until != null && System.currentTimeMillis() < until.get()) {
            return true;
        }
        if (until != null && System.currentTimeMillis() >= until.get()) {
            // Lockout expired, reset
            lockoutUntil.remove(ip);
            loginAttempts.remove(ip);
        }
        return false;
    }

    private void recordFailedAttempt(String ip) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        int count = attempts.incrementAndGet();
        if (count >= MAX_LOGIN_ATTEMPTS) {
            lockoutUntil.put(ip, new AtomicLong(System.currentTimeMillis() + LOCKOUT_DURATION_MS));
        }
    }

    private void clearLoginAttempts(String ip) {
        loginAttempts.remove(ip);
        lockoutUntil.remove(ip);
    }
}
