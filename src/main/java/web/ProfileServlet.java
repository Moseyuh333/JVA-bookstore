package web;

import utils.DBUtil;
import utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({"/api/profile", "/api/profile/*"})
public class ProfileServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            getUserProfile(request, response);
        } else if (pathInfo.equals("/orders")) {
            getUserOrders(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            updateUserProfile(request, response);
        } else if (pathInfo.equals("/password")) {
            changePassword(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/delete")) {
            deleteUserAccount(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    private void getUserProfile(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get user from JWT token
            String token = getTokenFromRequest(request);
            if (token == null || !JwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Not authenticated");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            models.User userFromToken = JwtUtil.getSubject(token);
            if (userFromToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Invalid token data");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "SELECT id, email, full_name, phone, birth_date, address, created_at FROM users WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userFromToken.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("id", rs.getInt("id"));
                            user.put("email", rs.getString("email"));
                            user.put("fullName", rs.getString("full_name"));
                            user.put("phone", rs.getString("phone"));
                            user.put("birthDate", rs.getDate("birth_date"));
                            user.put("address", rs.getString("address"));
                            user.put("createdAt", rs.getTimestamp("created_at"));

                            responseMap.put("success", true);
                            responseMap.put("user", user);
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            responseMap.put("success", false);
                            responseMap.put("message", "User not found");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("success", false);
            responseMap.put("message", "Database error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void updateUserProfile(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get user from JWT token
            String email = getUserEmailFromRequest(request);
            if (email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Not authenticated");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            // Parse request body
            Map<String, Object> requestData = readJsonRequest(request);
            
            String fullName = (String) requestData.get("fullName");
            String phone = (String) requestData.get("phone");
            String birthDateStr = (String) requestData.get("birthDate");
            String address = (String) requestData.get("address");
            
            // Validate required fields
            if (fullName == null || fullName.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("success", false);
                responseMap.put("message", "Họ và tên không được để trống");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE users SET full_name = ?, phone = ?, birth_date = ?, address = ? WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, fullName.trim());
                    stmt.setString(2, phone != null ? phone.trim() : null);
                    
                    if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
                        stmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(birthDateStr)));
                    } else {
                        stmt.setDate(3, null);
                    }
                    
                    stmt.setString(4, address != null ? address.trim() : null);
                    stmt.setString(5, email);
                    
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        responseMap.put("success", true);
                        responseMap.put("message", "Profile updated successfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        responseMap.put("success", false);
                        responseMap.put("message", "User not found");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("success", false);
            responseMap.put("message", "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", "Invalid request data");
        }
        
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get user from JWT token
            String email = getUserEmailFromRequest(request);
            if (email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Not authenticated");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            // Parse request body
            Map<String, Object> requestData = readJsonRequest(request);
            
            String currentPassword = (String) requestData.get("currentPassword");
            String newPassword = (String) requestData.get("newPassword");
            
            // Validate input
            if (currentPassword == null || newPassword == null || 
                currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("success", false);
                responseMap.put("message", "Current password and new password are required");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            if (newPassword.length() < 6) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("success", false);
                responseMap.put("message", "Mật khẩu mới phải có ít nhất 6 ký tự");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                // First, verify current password
                String selectSql = "SELECT password FROM users WHERE email = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setString(1, email);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            String storedPassword = rs.getString("password");
                            
                            if (!BCrypt.checkpw(currentPassword, storedPassword)) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                responseMap.put("success", false);
                                responseMap.put("message", "Mật khẩu hiện tại không đúng");
                                response.getWriter().write(gson.toJson(responseMap));
                                return;
                            }
                            
                            // Hash new password and update
                            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                            
                            String updateSql = "UPDATE users SET password = ? WHERE email = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setString(1, hashedNewPassword);
                                updateStmt.setString(2, email);
                                
                                int rowsUpdated = updateStmt.executeUpdate();
                                if (rowsUpdated > 0) {
                                    responseMap.put("success", true);
                                    responseMap.put("message", "Password changed successfully");
                                } else {
                                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    responseMap.put("success", false);
                                    responseMap.put("message", "Failed to update password");
                                }
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            responseMap.put("success", false);
                            responseMap.put("message", "User not found");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("success", false);
            responseMap.put("message", "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", "Invalid request data");
        }
        
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void getUserOrders(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get user from JWT token
            String email = getUserEmailFromRequest(request);
            if (email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Not authenticated");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                // Get user ID first
                Long userId = null;
                String getUserSql = "SELECT id FROM users WHERE email = ?";
                try (PreparedStatement getUserStmt = conn.prepareStatement(getUserSql)) {
                    getUserStmt.setString(1, email);
                    try (ResultSet rs = getUserStmt.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getLong("id");
                        }
                    }
                }
                
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responseMap.put("success", false);
                    responseMap.put("message", "User not found");
                    response.getWriter().write(gson.toJson(responseMap));
                    return;
                }
                
                // Get orders for this user
                List<Map<String, Object>> orders = new ArrayList<>();
                String ordersSql = "SELECT id, order_date, total_amount, status FROM orders WHERE user_id = ? ORDER BY order_date DESC";
                try (PreparedStatement ordersStmt = conn.prepareStatement(ordersSql)) {
                    ordersStmt.setLong(1, userId);
                    try (ResultSet rs = ordersStmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> order = new HashMap<>();
                            order.put("id", rs.getLong("id"));
                            order.put("orderDate", rs.getTimestamp("order_date"));
                            order.put("totalAmount", rs.getBigDecimal("total_amount"));
                            order.put("status", rs.getString("status"));
                            orders.add(order);
                        }
                    }
                }
                
                responseMap.put("success", true);
                responseMap.put("orders", orders);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("success", false);
            responseMap.put("message", "Database error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void deleteUserAccount(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get user from JWT token
            String email = getUserEmailFromRequest(request);
            if (email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Not authenticated");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            // Parse request body
            Map<String, Object> requestData = readJsonRequest(request);
            
            String password = (String) requestData.get("password");
            
            if (password == null || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("success", false);
                responseMap.put("message", "Password is required for account deletion");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // First, verify password
                    String selectSql = "SELECT id, password FROM users WHERE email = ?";
                    Long userId = null;
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                        selectStmt.setString(1, email);
                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                userId = rs.getLong("id");
                                String storedPassword = rs.getString("password");
                                
                                if (!BCrypt.checkpw(password, storedPassword)) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    responseMap.put("success", false);
                                    responseMap.put("message", "Mật khẩu không đúng");
                                    response.getWriter().write(gson.toJson(responseMap));
                                    conn.rollback();
                                    return;
                                }
                            } else {
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                responseMap.put("success", false);
                                responseMap.put("message", "User not found");
                                response.getWriter().write(gson.toJson(responseMap));
                                conn.rollback();
                                return;
                            }
                        }
                    }
                    
                    // Delete related data (orders, etc.)
                    // Note: In a real application, you might want to keep orders for business reasons
                    // and just mark the user as deleted instead of actually deleting
                    
                    // Delete order items first (if orders table exists)
                    try {
                        String deleteOrderItemsSql = "DELETE oi FROM order_items oi INNER JOIN orders o ON oi.order_id = o.id WHERE o.user_id = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(deleteOrderItemsSql)) {
                            stmt.setLong(1, userId);
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        // Table might not exist, continue
                    }
                    
                    // Delete orders
                    try {
                        String deleteOrdersSql = "DELETE FROM orders WHERE user_id = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(deleteOrdersSql)) {
                            stmt.setLong(1, userId);
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        // Table might not exist, continue
                    }
                    
                    // Finally, delete the user
                    String deleteUserSql = "DELETE FROM users WHERE id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteUserSql)) {
                        deleteStmt.setLong(1, userId);
                        int rowsDeleted = deleteStmt.executeUpdate();
                        
                        if (rowsDeleted > 0) {
                            conn.commit();
                            responseMap.put("success", true);
                            responseMap.put("message", "Account deleted successfully");
                        } else {
                            conn.rollback();
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            responseMap.put("success", false);
                            responseMap.put("message", "Failed to delete account");
                        }
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("success", false);
            responseMap.put("message", "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", "Invalid request data");
        }
        
        response.getWriter().write(gson.toJson(responseMap));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ") && bearerToken.length() > 7) {
            return bearerToken.substring(7);
        }
        
        // Also check for token in cookies
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

    private String getUserEmailFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null && JwtUtil.validateToken(token)) {
            models.User user = JwtUtil.getSubject(token);
            if (user != null) {
                return user.getEmail();
            }
        }
        return null;
    }

    private Map<String, Object> readJsonRequest(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        return gson.fromJson(json.toString(), new TypeToken<Map<String, Object>>(){}.getType());
    }
}