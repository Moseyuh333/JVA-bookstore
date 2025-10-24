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
}
