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
                listUsers(out);
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
            int affected = 0;
            
            if ("clear-tokens".equals(action)) {
                affected = clearVerificationTokens();
            } else if ("verify-all".equals(action)) {
                affected = verifyAllUsers();
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
                return;
            }
            
            out.write("{\"message\":\"Success\", \"affected\":" + affected + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
    
    private void listUsers(PrintWriter out) throws SQLException {
        String sql = "SELECT id, username, email, email_verified, verification_token, created_at FROM users ORDER BY created_at DESC";
        
        StringBuilder json = new StringBuilder();
        json.append("{\"users\":[");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                
                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"username\":\"").append(escapeJson(rs.getString("username"))).append("\",")
                    .append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",")
                    .append("\"verified\":").append(rs.getBoolean("email_verified")).append(",")
                    .append("\"hasToken\":").append(rs.getString("verification_token") != null).append(",")
                    .append("\"created\":\"").append(rs.getTimestamp("created_at").toString()).append("\"")
                    .append("}");
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
}
