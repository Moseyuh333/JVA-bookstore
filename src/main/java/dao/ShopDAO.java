package dao;

import models.Shop;
import utils.DBUtil;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ShopDAO {
    
    /**
     * Lấy thông tin shop theo owner_id
     */
    public static Shop getShopByOwnerId(int ownerId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE owner_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShop(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Lấy shop ID theo user ID
     */
    public static int getShopIdByUserId(int userId) throws SQLException {
        String sql = "SELECT id FROM shops WHERE owner_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
    
    /**
     * Lấy thông tin shop theo shop ID
     */
    public static Shop getShopById(int shopId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShop(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Lấy tất cả shops
     */
    public static List<Shop> getAllShops() throws SQLException {
        List<Shop> shops = new ArrayList<>();
        String sql = "SELECT * FROM shops WHERE status = 'active' ORDER BY created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shops.add(mapResultSetToShop(rs));
            }
        }
        return shops;
    }
    
    /**
     * Lấy thống kê dashboard của shop
     */
    public static Map<String, Object> getDashboardStats(int shopId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        System.out.println("[ShopDAO] Đang chạy bản DAO mới nè ❤️ | shopId=" + shopId);

        try (Connection conn = DBUtil.getConnection()) {

            // Tổng sản phẩm
            String sql1 = "SELECT COUNT(*) FROM books WHERE shop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stats.put("totalProducts", rs.getInt(1));
                }
            }

            // Đơn hàng mới
            String sql2 = 
                "SELECT COUNT(DISTINCT o.id) " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN books b ON oi.book_id = b.id " +
                "WHERE b.shop_id = ? " +
                "AND LOWER(o.status) = 'new'";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stats.put("newOrders", rs.getInt(1));
                }
            }

            // Doanh thu tháng này (các đơn đã giao thành công)
            String sql3 = 
                "SELECT COALESCE(SUM(oi.total_price), 0) " +
                "FROM order_items oi " +
                "JOIN books b ON oi.book_id = b.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE b.shop_id = ? " +
                "AND EXTRACT(MONTH FROM o.created_at) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                "AND EXTRACT(YEAR FROM o.created_at) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                "AND LOWER(o.status) IN ('delivered', 'completed')";
            try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stats.put("monthlyRevenue", rs.getDouble(1));
                }
            }

            // Đánh giá trung bình
            String sql4 = 
                "SELECT COALESCE(AVG(br.rating), 0) " +
                "FROM book_reviews br " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE b.shop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql4)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("avgRating", Math.round(rs.getDouble(1) * 10.0) / 10.0);
                    }
                }
            }
        }

        return stats;
    }

    
    /**
     * Cập nhật thông tin shop
     */
    public static boolean updateShop(Shop shop) throws SQLException {
        String sql = "UPDATE shops SET name = ?, description = ?, " +
                     "logo_url = ?, status = ?, " +
                     "phone = ?, email = ?, address = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shop.getName());
            ps.setString(2, shop.getDescription());
            ps.setString(3, shop.getLogoUrl());
            ps.setString(4, shop.getStatus());
            ps.setString(5, shop.getPhone());
            ps.setString(6, shop.getEmail());
            ps.setString(7, shop.getAddress());
            ps.setInt(8, shop.getId());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Tạo shop mới
     */
    public static int createShop(Shop shop) throws SQLException {
        String sql = "INSERT INTO shops (name, owner_id, description, logo_url, status) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shop.getName());
            ps.setInt(2, shop.getOwnerId());
            ps.setString(3, shop.getDescription());
            ps.setString(4, shop.getLogoUrl());
            ps.setString(5, shop.getStatus() != null ? shop.getStatus() : "active");
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
    
    /**
     * Xóa shop (soft delete - set status = 'inactive')
     */
    public static boolean deleteShop(int shopId) throws SQLException {
        String sql = "UPDATE shops SET status = 'inactive' WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Kiểm tra shop có tồn tại không
     */
    public static boolean shopExists(int shopId) throws SQLException {
        String sql = "SELECT 1 FROM shops WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Lấy tổng số shop
     */
    public static int getTotalShops() throws SQLException {
        String sql = "SELECT COUNT(*) FROM shops WHERE status = 'active'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Map ResultSet to Shop object
     */
    private static Shop mapResultSetToShop(ResultSet rs) throws SQLException {
        Shop shop = new Shop();
        
        // Required fields
        shop.setId(rs.getInt("id"));
        shop.setName(rs.getString("name"));
        shop.setOwnerId(rs.getInt("owner_id"));
        shop.setDescription(rs.getString("description"));
        shop.setLogoUrl(rs.getString("logo_url"));
        shop.setStatus(rs.getString("status"));
        shop.setCommissionRate(rs.getDouble("commission_rate"));
        shop.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Optional fields (check if column exists)
        try {
            shop.setAvatarUrl(rs.getString("avatar_url"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setCoverUrl(rs.getString("cover_url"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setFeaturedImageUrl(rs.getString("featured_image_url"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setPhone(rs.getString("phone"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setEmail(rs.getString("email"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setAddress(rs.getString("address"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setLogoText(rs.getString("logo_text"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setSlogan(rs.getString("slogan"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setBannerColor(rs.getString("banner_color"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            shop.setThemeColor(rs.getString("theme_color"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        return shop;
    }
}