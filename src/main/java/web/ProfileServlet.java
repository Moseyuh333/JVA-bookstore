package web;

import dao.CouponDAO;
import dao.FavoriteDAO;
import dao.OrderDAO;
import dao.RecentViewDAO;
import dao.ShopCouponDAO;
import dao.UserAddressDAO;
import models.Order;
import models.OrderStatusHistory;
import models.ShopCoupon;
import models.UserAddress;
import utils.AuthUtil;
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
import java.sql.Statement;
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
        
    List<String> segments = getPathSegments(request.getPathInfo());

        if (segments.isEmpty()) {
            getUserProfile(request, response);
            return;
        }

        String resource = segments.get(0);
        try {
            switch (resource) {
                case "orders":
                    handleOrdersGet(request, response, segments);
                    break;
                case "addresses":
                    if (segments.size() == 1) {
                        listUserAddresses(request, response);
                    } else {
                        sendNotFound(response);
                    }
                    break;
                case "favorites":
                    if (segments.size() == 1) {
                        listFavorites(request, response);
                    } else {
                        sendNotFound(response);
                    }
                    break;
                case "recent-views":
                    listRecentViews(request, response);
                    break;
                case "coupons":
                    listCoupons(request, response);
                    break;
                case "shop-coupons":
                    listShopCouponsForShop(request, response);
                    break;
                default:
                    sendNotFound(response);
                    break;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Database error: " + ex.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<String> segments = getPathSegments(request.getPathInfo());
        if (segments.isEmpty()) {
            sendNotFound(response);
            return;
        }

        String resource = segments.get(0);
        try {
            switch (resource) {
                case "addresses":
                    if (segments.size() == 1) {
                        createUserAddress(request, response);
                    } else if (segments.size() == 2 && "default".equalsIgnoreCase(segments.get(1))) {
                        sendNotFound(response);
                    } else if (segments.size() == 3 && "default".equalsIgnoreCase(segments.get(2))) {
                        Long addressId = parseId(segments.get(1));
                        if (addressId == null) {
                            sendNotFound(response);
                        } else {
                            setDefaultAddress(request, response, addressId);
                        }
                    } else {
                        sendNotFound(response);
                    }
                    break;
                case "favorites":
                    if (segments.size() == 2) {
                        Long bookId = parseId(segments.get(1));
                        if (bookId == null) {
                            sendNotFound(response);
                        } else {
                            addFavorite(request, response, bookId);
                        }
                    } else {
                        sendNotFound(response);
                    }
                    break;
                case "recent-views":
                    recordRecentView(request, response);
                    break;
                case "orders":
                    if (segments.size() == 3 && "cancel".equalsIgnoreCase(segments.get(2))) {
                        Long orderId = parseId(segments.get(1));
                        if (orderId == null) {
                            sendNotFound(response);
                        } else {
                            cancelUserOrder(request, response, orderId);
                        }
                    } else {
                        sendNotFound(response);
                    }
                    break;
                default:
                    sendNotFound(response);
                    break;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Database error: " + ex.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        List<String> segments = getPathSegments(request.getPathInfo());
        try {
            if (segments.isEmpty()) {
                updateUserProfile(request, response);
            } else if (segments.size() == 1 && "password".equalsIgnoreCase(segments.get(0))) {
                changePassword(request, response);
            } else if (segments.size() >= 2 && "addresses".equalsIgnoreCase(segments.get(0))) {
                Long addressId = parseId(segments.get(1));
                if (addressId == null) {
                    sendNotFound(response);
                } else {
                    updateUserAddress(request, response, addressId);
                }
            } else {
                sendNotFound(response);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Database error: " + ex.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        List<String> segments = getPathSegments(request.getPathInfo());

        try {
            if (segments.size() == 1 && "delete".equalsIgnoreCase(segments.get(0))) {
                deleteUserAccount(request, response);
                return;
            }

            if (segments.size() == 2 && "addresses".equalsIgnoreCase(segments.get(0))) {
                Long addressId = parseId(segments.get(1));
                if (addressId == null) {
                    sendNotFound(response);
                } else {
                    deleteUserAddress(request, response, addressId);
                }
                return;
            }

            if (segments.size() == 2 && "favorites".equalsIgnoreCase(segments.get(0))) {
                Long bookId = parseId(segments.get(1));
                if (bookId == null) {
                    sendNotFound(response);
                } else {
                    removeFavorite(request, response, bookId);
                }
                return;
            }

            sendNotFound(response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Database error: " + ex.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    private void getUserProfile(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get user from JWT token
            String token = getTokenFromRequest(request);
            String email = JwtUtil.validateToken(token);
            if (email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("success", false);
                responseMap.put("message", "Not authenticated");
                response.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                ensureUserProfileColumns(conn);
                String sql = "SELECT id, email, full_name, phone, birth_date, address, created_at FROM users WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, email);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("id", rs.getLong("id"));
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
                ensureUserProfileColumns(conn);
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

    private void ensureUserProfileColumns(Connection conn) throws SQLException {
        ensureUserColumnExists(conn, "birth_date", "DATE");
        ensureUserColumnExists(conn, "address", "TEXT");
    }

    private void ensureUserColumnExists(Connection conn, String columnName, String columnDefinition) throws SQLException {
        String checkSql = "SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, columnName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String alterSql = "ALTER TABLE users ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement alterStmt = conn.createStatement()) {
            alterStmt.execute(alterSql);
        }
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
                String selectSql = "SELECT password_hash FROM users WHERE email = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setString(1, email);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            String storedPassword = rs.getString("password_hash");

                            if (storedPassword == null || !BCrypt.checkpw(currentPassword, storedPassword)) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                responseMap.put("success", false);
                                responseMap.put("message", "Mật khẩu hiện tại không đúng");
                                response.getWriter().write(gson.toJson(responseMap));
                                return;
                            }
                            
                            // Hash new password and update
                            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                            
                            String updateSql = "UPDATE users SET password_hash = ? WHERE email = ?";
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
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        String status = request.getParameter("status");
        List<Order> orders = OrderDAO.findOrders(userId, status);
        responseMap.put("success", true);
        responseMap.put("orders", orders);
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
                    String selectSql = "SELECT id, password_hash FROM users WHERE email = ?";
                    Long userId = null;
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                        selectStmt.setString(1, email);
                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                userId = rs.getLong("id");
                                String storedPassword = rs.getString("password_hash");

                                if (storedPassword == null || !BCrypt.checkpw(password, storedPassword)) {
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
                    // Delete orders and order items (PostgreSQL-safe syntax)
                    String deleteOrderItemsSql = "DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(deleteOrderItemsSql)) {
                        stmt.setLong(1, userId);
                        stmt.executeUpdate();
                    }

                    String deleteOrdersSql = "DELETE FROM orders WHERE user_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(deleteOrdersSql)) {
                        stmt.setLong(1, userId);
                        stmt.executeUpdate();
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

    private void handleOrdersGet(HttpServletRequest request, HttpServletResponse response, List<String> segments) 
            throws IOException, SQLException {
        if (segments.size() == 1) {
            getUserOrders(request, response);
            return;
        }

        Long orderId = parseId(segments.get(1));
        if (orderId == null) {
            sendNotFound(response);
            return;
        }

        if (segments.size() == 2) {
            getOrderDetail(request, response, orderId);
            return;
        }

        if (segments.size() == 3 && "timeline".equalsIgnoreCase(segments.get(2))) {
            getOrderTimeline(request, response, orderId);
            return;
        }

        sendNotFound(response);
    }

    private void getOrderDetail(HttpServletRequest request, HttpServletResponse response, long orderId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        try {
            Order order = OrderDAO.fetchOrderById(orderId, userId);
            responseMap.put("success", true);
            responseMap.put("order", order);
            response.getWriter().write(gson.toJson(responseMap));
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Order not found")) {
                sendNotFound(response);
            } else {
                throw ex;
            }
        }
    }

    private void getOrderTimeline(HttpServletRequest request, HttpServletResponse response, long orderId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        try {
            OrderDAO.fetchOrderById(orderId, userId);
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Order not found")) {
                sendNotFound(response);
                return;
            }
            throw ex;
        }
        List<OrderStatusHistory> timeline = OrderDAO.findStatusTimeline(orderId, userId);
        responseMap.put("success", true);
        responseMap.put("timeline", timeline);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void cancelUserOrder(HttpServletRequest request, HttpServletResponse response, long orderId)
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        Map<String, Object> requestData = readJsonRequest(request);
        String reason = requestData != null ? stringValue(requestData.get("reason")) : null;
        try {
            Order order = OrderDAO.cancelOrder(orderId, userId, reason);
            responseMap.put("success", true);
            responseMap.put("message", "Đơn hàng đã được hủy.");
            responseMap.put("order", order);
            response.getWriter().write(gson.toJson(responseMap));
        } catch (SQLException ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "Không thể hủy đơn hàng.";
            if (message.contains("Order not found")) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", message);
            response.getWriter().write(gson.toJson(responseMap));
        }
    }

    private void listUserAddresses(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        List<UserAddress> addresses = UserAddressDAO.findByUser(userId);
        responseMap.put("success", true);
        responseMap.put("addresses", addresses);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void listFavorites(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        List<FavoriteDAO.FavoriteRecord> favorites = FavoriteDAO.findFavorites(userId);
        responseMap.put("success", true);
        responseMap.put("favorites", favorites);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void listRecentViews(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        int limit = parsePositiveInt(request.getParameter("limit"), 20, 100);
        List<RecentViewDAO.RecentViewRecord> recentViews = RecentViewDAO.findRecentViews(userId, limit);
        responseMap.put("success", true);
        responseMap.put("data", recentViews);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void listCoupons(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        List<CouponDAO.CouponRecord> coupons = CouponDAO.listActiveCoupons(userId);
        responseMap.put("success", true);
        responseMap.put("coupons", coupons);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void listShopCouponsForShop(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }

        Long shopIdRaw = parseId(request.getParameter("shopId"));
        if (shopIdRaw == null || shopIdRaw <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", "Thiếu tham số shopId hợp lệ");
            response.getWriter().write(gson.toJson(responseMap));
            return;
        }

        int shopId = shopIdRaw.intValue();
        List<ShopCoupon> coupons = ShopCouponDAO.listActiveForShop(shopId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ShopCoupon coupon : coupons) {
            Map<String, Object> node = new HashMap<>();
            node.put("code", coupon.getCode());
            node.put("description", coupon.getDescription());
            node.put("type", coupon.getDiscountType());
            node.put("value", coupon.getDiscountValue());
            node.put("minimumOrder", coupon.getMinimumOrder());
            node.put("usageLimit", coupon.getUsageLimit());
            if (coupon.getUsageLimit() != null) {
                int used = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
                node.put("remaining", Math.max(0, coupon.getUsageLimit() - used));
            }
            node.put("startDate", coupon.getStartDate());
            node.put("endDate", coupon.getEndDate());
            node.put("shopId", coupon.getShopId());
            node.put("shopName", coupon.getShopName());
            node.put("scope", "shop");
            result.add(node);
        }
        responseMap.put("success", true);
        responseMap.put("coupons", result);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void createUserAddress(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        Map<String, Object> requestData = readJsonRequest(request);
        try {
            UserAddress address = new UserAddress();
            address.setUserId(userId);
            address.setLabel(stringValue(requestData.get("label")));
            address.setRecipientName(requiredString(requestData.get("recipientName"), "Tên người nhận"));
            address.setPhone(requiredString(requestData.get("phone"), "Số điện thoại"));
            address.setLine1(requiredString(requestData.get("line1"), "Địa chỉ"));
            address.setLine2(stringValue(requestData.get("line2")));
            address.setWard(stringValue(requestData.get("ward")));
            address.setDistrict(stringValue(requestData.get("district")));
            address.setCity(stringValue(requestData.get("city")));
            address.setProvince(stringValue(requestData.get("province")));
            address.setPostalCode(stringValue(requestData.get("postalCode")));
            address.setCountry(defaultIfBlank(stringValue(requestData.get("country")), "Việt Nam"));
            address.setNote(stringValue(requestData.get("note")));
            address.setDefault(Boolean.TRUE.equals(requestData.get("isDefault")));

            address = UserAddressDAO.create(address);
            responseMap.put("success", true);
            responseMap.put("address", address);
            response.getWriter().write(gson.toJson(responseMap));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", ex.getMessage());
            response.getWriter().write(gson.toJson(responseMap));
        }
    }

    private void updateUserAddress(HttpServletRequest request, HttpServletResponse response, long addressId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        Map<String, Object> requestData = readJsonRequest(request);
        UserAddress address = UserAddressDAO.findById(userId, addressId);
        if (address == null) {
            sendNotFound(response);
            return;
        }
        try {
            address.setLabel(stringValue(requestData.get("label")));
            address.setRecipientName(requiredString(requestData.get("recipientName"), "Tên người nhận"));
            address.setPhone(requiredString(requestData.get("phone"), "Số điện thoại"));
            address.setLine1(requiredString(requestData.get("line1"), "Địa chỉ"));
            address.setLine2(stringValue(requestData.get("line2")));
            address.setWard(stringValue(requestData.get("ward")));
            address.setDistrict(stringValue(requestData.get("district")));
            address.setCity(stringValue(requestData.get("city")));
            address.setProvince(stringValue(requestData.get("province")));
            address.setPostalCode(stringValue(requestData.get("postalCode")));
            address.setCountry(defaultIfBlank(stringValue(requestData.get("country")), "Việt Nam"));
            address.setNote(stringValue(requestData.get("note")));
            Object defaultFlag = requestData.get("isDefault");
            if (defaultFlag != null) {
                address.setDefault(Boolean.TRUE.equals(defaultFlag));
            }
            address = UserAddressDAO.update(address);
            responseMap.put("success", true);
            responseMap.put("address", address);
            response.getWriter().write(gson.toJson(responseMap));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", ex.getMessage());
            response.getWriter().write(gson.toJson(responseMap));
        }
    }

    private void setDefaultAddress(HttpServletRequest request, HttpServletResponse response, long addressId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        UserAddress address = UserAddressDAO.findById(userId, addressId);
        if (address == null) {
            sendNotFound(response);
            return;
        }
        UserAddressDAO.setDefault(userId, addressId);
        responseMap.put("success", true);
        responseMap.put("message", "Đã đặt địa chỉ mặc định");
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void deleteUserAddress(HttpServletRequest request, HttpServletResponse response, long addressId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        UserAddress address = UserAddressDAO.findById(userId, addressId);
        if (address == null) {
            sendNotFound(response);
            return;
        }
        UserAddressDAO.delete(userId, addressId);
        responseMap.put("success", true);
        responseMap.put("message", "Đã xóa địa chỉ");
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void addFavorite(HttpServletRequest request, HttpServletResponse response, long bookId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        FavoriteDAO.addFavorite(userId, bookId);
        responseMap.put("success", true);
        responseMap.put("message", "Đã thêm vào yêu thích");
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void removeFavorite(HttpServletRequest request, HttpServletResponse response, long bookId) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        FavoriteDAO.removeFavorite(userId, bookId);
        responseMap.put("success", true);
        responseMap.put("message", "Đã bỏ khỏi yêu thích");
        response.getWriter().write(gson.toJson(responseMap));
    }

    private void recordRecentView(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        Map<String, Object> responseMap = new HashMap<>();
        Long userId = getRequiredUserId(request, response, responseMap);
        if (userId == null) {
            return;
        }
        Map<String, Object> requestData = readJsonRequest(request);
        Object bookIdRaw = requestData.get("bookId");
        Long bookId = null;
        if (bookIdRaw instanceof Number) {
            bookId = ((Number) bookIdRaw).longValue();
        } else if (bookIdRaw instanceof String) {
            bookId = parseId((String) bookIdRaw);
        }
        if (bookId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", "Thiếu mã sách");
            response.getWriter().write(gson.toJson(responseMap));
            return;
        }
        RecentViewDAO.recordView(userId, bookId);
        responseMap.put("success", true);
        response.getWriter().write(gson.toJson(responseMap));
    }

    private List<String> getPathSegments(String pathInfo) {
        List<String> segments = new ArrayList<>();
        if (pathInfo == null || pathInfo.isEmpty()) {
            return segments;
        }
        String[] tokens = pathInfo.split("/");
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                segments.add(token);
            }
        }
        return segments;
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Endpoint not found");
        response.getWriter().write(gson.toJson(errorResponse));
    }

    private Long parseId(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            long value = Long.parseLong(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int parsePositiveInt(String raw, int defaultValue, int max) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value <= 0) {
                return defaultValue;
            }
            return Math.min(value, max);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String stringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private String requiredString(Object raw, String fieldLabel) {
        String value = stringValue(raw);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldLabel + " không được để trống");
        }
        return value;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
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
        return JwtUtil.validateToken(token);
    }

    private Long resolveUserId(HttpServletRequest request) throws SQLException {
        return AuthUtil.resolveUserId(request);
    }

    private Long getRequiredUserId(HttpServletRequest request, HttpServletResponse response, Map<String, Object> responseMap) throws IOException, SQLException {
        Long userId = resolveUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("success", false);
            responseMap.put("message", "Not authenticated");
            response.getWriter().write(gson.toJson(responseMap));
            return null;
        }
        return userId;
    }

    private Map<String, Object> readJsonRequest(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        String raw = json.toString().trim();
        if (raw.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> data = gson.fromJson(raw, new TypeToken<Map<String, Object>>(){}.getType());
        return data != null ? data : new HashMap<>();
    }
}
