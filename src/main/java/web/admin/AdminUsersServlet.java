package web.admin;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AdminUsersServlet", urlPatterns = {"/api/admin/users"})
public class AdminUsersServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String action = req.getParameter("action");
        
        try {
            if ("list".equals(action)) {
                listUsers(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                createUser(req, out);
            } else if ("update".equals(action)) {
                updateUser(req, out);
            } else if ("delete".equals(action)) {
                deleteUser(req, out);
            } else if ("clear-tokens".equals(action)) {
                int affected = clearVerificationTokens();
                out.write("{\"message\":\"Success\", \"affected\":" + affected + "}");
            } else if ("verify-all".equals(action)) {
                int affected = verifyAllUsers();
                out.write("{\"message\":\"Success\", \"affected\":" + affected + "}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
                return;
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
    
    private void listUsers(HttpServletRequest req, PrintWriter out) throws SQLException {
        String search = req.getParameter("search");
        String sql;
        boolean hasSearch = search != null && !search.trim().isEmpty();

        if (hasSearch) {
            sql = "SELECT id, username, email, full_name, phone, role, status, email_verified, created_at, updated_at, birth_date, address FROM users WHERE LOWER(username) LIKE ? OR LOWER(email) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(phone) LIKE ? ORDER BY created_at DESC";
        } else {
            sql = "SELECT id, username, email, full_name, phone, role, status, email_verified, created_at, updated_at, birth_date, address FROM users ORDER BY created_at DESC";
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"users\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (hasSearch) {
                String likeSearch = "%" + search.trim().toLowerCase() + "%";
                pstmt.setString(1, likeSearch);
                pstmt.setString(2, likeSearch);
                pstmt.setString(3, likeSearch);
                pstmt.setString(4, likeSearch);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;

                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"username\":\"").append(escapeJson(rs.getString("username"))).append("\",")
                        .append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",")
                        .append("\"full_name\":\"").append(escapeJson(rs.getString("full_name"))).append("\",")
                        .append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",")
                        .append("\"role\":\"").append(escapeJson(rs.getString("role"))).append("\",")
                        .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                        .append("\"verified\":").append(rs.getBoolean("email_verified")).append(",")
                        .append("\"created\":\"").append(escapeJson(rs.getTimestamp("created_at").toString())).append("\",")
                        .append("\"updated\":\"").append(rs.getTimestamp("updated_at") != null ? escapeJson(rs.getTimestamp("updated_at").toString()) : "").append("\",")
                        .append("\"birth_date\":\"").append(rs.getDate("birth_date") != null ? escapeJson(rs.getDate("birth_date").toString()) : "").append("\",")
                        .append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\"")
                        .append("}");
                }
            }
        }

        json.append("]}");
        out.write(json.toString());
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
    
    private int clearVerificationTokens() throws SQLException {
        String sql = "UPDATE users SET verification_token = NULL, email_verified = true WHERE verification_token IS NOT NULL";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        }
    }
    
    private int verifyAllUsers() throws SQLException {
        String sql = "UPDATE users SET email_verified = true WHERE email_verified = false";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        }
    }

    private void createUser(HttpServletRequest req, PrintWriter out) throws SQLException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String passwordHash = req.getParameter("password_hash");
        String fullName = req.getParameter("full_name");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");
        String status = req.getParameter("status");

        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty() || passwordHash == null) {
            out.write("{\"error\":\"Username, email and password_hash are required\"}");
            return;
        }

        String sql = "INSERT INTO users (username, email, password_hash, full_name, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, fullName != null ? fullName.trim() : null);
            pstmt.setString(5, phone != null ? phone.trim() : null);
            pstmt.setString(6, role != null ? role : "user");
            pstmt.setString(7, status != null ? status : "active");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"User created successfully\"}");
            } else {
                out.write("{\"error\":\"Failed to create user\"}");
            }
        }
    }

    private void updateUser(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String fullName = req.getParameter("full_name");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");
        String status = req.getParameter("status");

        if (idStr == null || username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            out.write("{\"error\":\"ID, username and email are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "UPDATE users SET username = ?, email = ?, full_name = ?, phone = ?, role = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());
            pstmt.setString(3, fullName != null ? fullName.trim() : null);
            pstmt.setString(4, phone != null ? phone.trim() : null);
            pstmt.setString(5, role != null ? role : "user");
            pstmt.setString(6, status != null ? status : "active");
            pstmt.setInt(7, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"User updated successfully\"}");
            } else {
                out.write("{\"error\":\"User not found\"}");
            }
        }
    }

    private void deleteUser(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"User deleted successfully\"}");
            } else {
                out.write("{\"error\":\"User not found\"}");
            }
        }
    }
}
