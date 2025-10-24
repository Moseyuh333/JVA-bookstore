package dao;

import models.Shop;
import utils.DBUtil; // Import DBUtil của bạn
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopDAO {

    /**
     * Lấy thông tin Shop bằng ID
     */
    public static Shop getShopById(int shopId) throws SQLException {
        String sql = "SELECT id, owner_id, name, address, description, commission_rate FROM shops WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shop shop = new Shop();
                    shop.setId(rs.getInt("id"));
                    shop.setOwnerId(rs.getInt("owner_id"));
                    shop.setName(rs.getString("name"));
                    shop.setAddress(rs.getString("address"));
                    shop.setDescription(rs.getString("description"));
                    shop.setCommissionRate(rs.getDouble("commission_rate"));
                    return shop;
                }
            }
        }
        return null;
    }

    /**
     * Lấy ID của Shop bằng User ID (Owner ID)
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
        return -1; // Không tìm thấy
    }



    
    // ... (Giữ nguyên getShopById và getShopIdByUserId) ...

    /**
     * Đếm tổng số sản phẩm của một Shop
     */
    public static int countProductsByShop(int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE shop_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Đếm sản phẩm còn hàng (stock > 0) của một Shop
     */
    public static int countInStockProductsByShop(int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE shop_id = ? AND stock_quantity > 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Cập nhật thông tin Shop
     */
    public static void updateShopProfile(int shopId, String name, String address, String description) throws SQLException {
        String sql = "UPDATE shops SET name = ?, address = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, description);
            ps.setInt(4, shopId);
            ps.executeUpdate();
        }
    }

    /**
     * Tạo Shop mới
     */
    public static int createShop(int ownerId, String name, String address, String description) throws SQLException {
        String sql = "INSERT INTO shops (owner_id, name, address, description, status, commission_rate) VALUES (?, ?, ?, ?, 'active', 10.00) RETURNING id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setString(4, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        }
    }
}
