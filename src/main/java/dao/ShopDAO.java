package dao;

import models.Shop;
import utils.DBUtil; // Import DBUtil của bạn
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class ShopDAO {

    /**
     * Lấy thông tin Shop bằng ID
     */
    public static Shop getShopById(int shopId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shop shop = new Shop();
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    java.util.Set<String> names = new java.util.HashSet<>();
                    for (int i = 1; i <= cols; i++) names.add(md.getColumnLabel(i).toLowerCase());

                    if (names.contains("id")) shop.setId(rs.getInt("id"));
                    if (names.contains("owner_id")) shop.setOwnerId(rs.getInt("owner_id"));
                    if (names.contains("name")) shop.setName(rs.getString("name"));
                    if (names.contains("address")) shop.setAddress(rs.getString("address"));
                    if (names.contains("description")) shop.setDescription(rs.getString("description"));
                    if (names.contains("commission_rate")) shop.setCommissionRate(rs.getDouble("commission_rate"));
                    if (names.contains("phone")) shop.setPhone(rs.getString("phone"));
                    if (names.contains("email")) shop.setEmail(rs.getString("email"));
                    if (names.contains("logo_url")) shop.setLogoUrl(rs.getString("logo_url"));
                    if (names.contains("status")) shop.setStatus(rs.getString("status"));
                    if (names.contains("slogan")) shop.setSlogan(rs.getString("slogan"));

                    return shop;
                }
            }
        }
        return null;
    }

    /**
     * Lấy thông tin Shop bằng User ID (Owner ID)
     */
    public static Shop getShopByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE owner_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shop shop = new Shop();
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    java.util.Set<String> names = new java.util.HashSet<>();
                    for (int i = 1; i <= cols; i++) names.add(md.getColumnLabel(i).toLowerCase());

                    if (names.contains("id")) shop.setId(rs.getInt("id"));
                    if (names.contains("owner_id")) shop.setOwnerId(rs.getInt("owner_id"));
                    if (names.contains("name")) shop.setName(rs.getString("name"));
                    if (names.contains("address")) shop.setAddress(rs.getString("address"));
                    if (names.contains("description")) shop.setDescription(rs.getString("description"));
                    if (names.contains("commission_rate")) shop.setCommissionRate(rs.getDouble("commission_rate"));
                    if (names.contains("phone")) shop.setPhone(rs.getString("phone"));
                    if (names.contains("email")) shop.setEmail(rs.getString("email"));
                    if (names.contains("logo_url")) shop.setLogoUrl(rs.getString("logo_url"));
                    if (names.contains("status")) shop.setStatus(rs.getString("status"));
                    if (names.contains("slogan")) shop.setSlogan(rs.getString("slogan"));

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
     * Tạo Shop mới
     */
    public static int createShop(int ownerId, String name, String address, String description) throws SQLException {
        String sqlWithAddress = "INSERT INTO shops (owner_id, name, address, description, status, commission_rate) VALUES (?, ?, ?, ?, 'active', 100.00) RETURNING id";
        String sqlWithoutAddress = "INSERT INTO shops (owner_id, name, description, status, commission_rate) VALUES (?, ?, ?, 'active', 100.00) RETURNING id";
        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlWithAddress)) {
                ps.setInt(1, ownerId);
                ps.setString(2, name);
                ps.setString(3, address);
                ps.setString(4, description);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                    return -1;
                }
            } catch (SQLException e) {
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                if (msg.contains("column \"address\" does not exist") || msg.contains("column \"address\"")) {
                    try (PreparedStatement ps2 = conn.prepareStatement(sqlWithoutAddress)) {
                        ps2.setInt(1, ownerId);
                        ps2.setString(2, name);
                        ps2.setString(3, description);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) return rs2.getInt(1);
                            return -1;
                        }
                    }
                }
                throw e;
            }
        }
    }

    /**
     * Cập nhật thông tin Shop
     */
    public static void updateShopProfile(int shopId, String name, String address, String description,
                                       String phone, String email, String slogan) throws SQLException {
        String sql = "UPDATE shops SET name = ?, address = ?, description = ?, phone = ?, email = ?, slogan = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String legacySql = "UPDATE shops SET name = ?, address = ?, description = ?, phone = ?, email = ?, slogan = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, description);
                ps.setString(4, phone);
                ps.setString(5, email);
                ps.setString(6, slogan);
                ps.setInt(7, shopId);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            if (!isMissingColumn(ex, "updated_at")) {
                throw ex;
            }
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(legacySql)) {
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, description);
                ps.setString(4, phone);
                ps.setString(5, email);
                ps.setString(6, slogan);
                ps.setInt(7, shopId);
                ps.executeUpdate();
            }
        }
    }

    public static void updateCommissionRate(int shopId, double commissionRate) throws SQLException {
        String sql = "UPDATE shops SET commission_rate = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, commissionRate);
            ps.setInt(2, shopId);
            ps.executeUpdate();
        }
    }

    private static boolean isMissingColumn(SQLException ex, String columnName) {
        if (ex == null || columnName == null) {
            return false;
        }
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = columnName.toLowerCase(Locale.US);
        String marker = "column \"" + normalized + "\" does not exist";
        return message.toLowerCase(Locale.US).contains(marker);
    }
}
