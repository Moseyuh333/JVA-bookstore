package dao;

import models.Store;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class StoreDAO {

    public static Store createStore(Long ownerUserId, String name, String description) throws SQLException {
        String sql = "INSERT INTO stores (owner_user_id, name, description) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ownerUserId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Store s = new Store();
                    s.setId(rs.getLong(1));
                    s.setOwnerUserId(ownerUserId);
                    s.setName(name);
                    s.setDescription(description);
                    return s;
                }
            }
        }
        return null;
    }

    public static List<Store> listStoresByOwner(Long ownerUserId) throws SQLException {
        String sql = "SELECT id, owner_user_id, name, description, avatar_url, cover_url, featured_images FROM stores WHERE owner_user_id = ?";
        List<Store> res = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ownerUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Store s = new Store();
                    s.setId(rs.getLong("id"));
                    s.setOwnerUserId(rs.getLong("owner_user_id"));
                    s.setName(rs.getString("name"));
                    s.setDescription(rs.getString("description"));
                    s.setAvatarUrl(rs.getString("avatar_url"));
                    s.setCoverUrl(rs.getString("cover_url"));
                    s.setFeaturedImagesJson(rs.getString("featured_images"));
                    res.add(s);
                }
            }
        }
        return res;
    }

    public static Store getStoreById(Long id) throws SQLException {
        String sql = "SELECT id, owner_user_id, name, description, avatar_url, cover_url, featured_images FROM stores WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Store s = new Store();
                    s.setId(rs.getLong("id"));
                    s.setOwnerUserId(rs.getLong("owner_user_id"));
                    s.setName(rs.getString("name"));
                    s.setDescription(rs.getString("description"));
                    s.setAvatarUrl(rs.getString("avatar_url"));
                    s.setCoverUrl(rs.getString("cover_url"));
                    s.setFeaturedImagesJson(rs.getString("featured_images"));
                    return s;
                }
            }
        }
        return null;
    }

    public static boolean updateStoreInfo(Long id, String name, String description, String avatarUrl, String coverUrl, String featuredImagesJson) throws SQLException {
        String sql = "UPDATE stores SET name = COALESCE(?, name), description = COALESCE(?, description), avatar_url = COALESCE(?, avatar_url), cover_url = COALESCE(?, cover_url), featured_images = COALESCE(?, featured_images), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, avatarUrl);
            stmt.setString(4, coverUrl);
            stmt.setString(5, featuredImagesJson);
            stmt.setLong(6, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean addEmployee(Long storeId, Long userId, String role) throws SQLException {
        String sql = "INSERT INTO store_employees (store_id, user_id, role) VALUES (?, ?, ?) ON CONFLICT (store_id, user_id) DO UPDATE SET role = EXCLUDED.role";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, storeId);
            stmt.setLong(2, userId);
            stmt.setString(3, role == null ? "staff" : role);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean removeEmployee(Long storeId, Long userId) throws SQLException {
        String sql = "DELETE FROM store_employees WHERE store_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, storeId);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}
