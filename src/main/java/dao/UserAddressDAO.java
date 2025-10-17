package dao;

import models.UserAddress;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class UserAddressDAO {

    private UserAddressDAO() {
    }

    public static List<UserAddress> findByUser(long userId) throws SQLException {
        String sql = "SELECT id, user_id, label, recipient_name, phone, line1, line2, ward, district, city, "
                + "province, postal_code, country, is_default, note, created_at, updated_at "
                + "FROM user_addresses WHERE user_id = ? ORDER BY is_default DESC, updated_at DESC, id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UserAddress> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    public static UserAddress findById(long userId, long addressId) throws SQLException {
        String sql = "SELECT id, user_id, label, recipient_name, phone, line1, line2, ward, district, city, "
                + "province, postal_code, country, is_default, note, created_at, updated_at "
                + "FROM user_addresses WHERE user_id = ? AND id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, addressId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        }
    }

    public static UserAddress create(UserAddress address) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean hasDefault = hasDefault(conn, address.getUserId());
                boolean shouldBeDefault = address.isDefault() || !hasDefault;
                if (shouldBeDefault) {
                    clearDefault(conn, address.getUserId());
                }
                String sql = "INSERT INTO user_addresses (user_id, label, recipient_name, phone, line1, line2, ward, district, city, province, postal_code, country, is_default, note) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, address.getUserId());
                    stmt.setString(2, trimToNull(address.getLabel()));
                    stmt.setString(3, address.getRecipientName());
                    stmt.setString(4, address.getPhone());
                    stmt.setString(5, address.getLine1());
                    stmt.setString(6, trimToNull(address.getLine2()));
                    stmt.setString(7, trimToNull(address.getWard()));
                    stmt.setString(8, trimToNull(address.getDistrict()));
                    stmt.setString(9, trimToNull(address.getCity()));
                    stmt.setString(10, trimToNull(address.getProvince()));
                    stmt.setString(11, trimToNull(address.getPostalCode()));
                    stmt.setString(12, trimToNull(address.getCountry()));
                    stmt.setBoolean(13, shouldBeDefault);
                    stmt.setString(14, trimToNull(address.getNote()));
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            address.setId(rs.getLong("id"));
                            address.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                            address.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
                            address.setDefault(shouldBeDefault);
                        }
                    }
                }
                conn.commit();
                return address;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static UserAddress update(UserAddress address) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (address.isDefault()) {
                    clearDefault(conn, address.getUserId());
                }
                String sql = "UPDATE user_addresses SET label = ?, recipient_name = ?, phone = ?, line1 = ?, line2 = ?, ward = ?, district = ?, city = ?, province = ?, postal_code = ?, country = ?, is_default = ?, note = ?, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND user_id = ? RETURNING created_at, updated_at";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, trimToNull(address.getLabel()));
                    stmt.setString(2, address.getRecipientName());
                    stmt.setString(3, address.getPhone());
                    stmt.setString(4, address.getLine1());
                    stmt.setString(5, trimToNull(address.getLine2()));
                    stmt.setString(6, trimToNull(address.getWard()));
                    stmt.setString(7, trimToNull(address.getDistrict()));
                    stmt.setString(8, trimToNull(address.getCity()));
                    stmt.setString(9, trimToNull(address.getProvince()));
                    stmt.setString(10, trimToNull(address.getPostalCode()));
                    stmt.setString(11, trimToNull(address.getCountry()));
                    stmt.setBoolean(12, address.isDefault());
                    stmt.setString(13, trimToNull(address.getNote()));
                    stmt.setLong(14, address.getId());
                    stmt.setLong(15, address.getUserId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            address.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                            address.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
                        }
                    }
                }
                if (!address.isDefault() && !hasDefault(conn, address.getUserId())) {
                    setDefault(conn, address.getUserId(), address.getId());
                    address.setDefault(true);
                }
                conn.commit();
                return address;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void delete(long userId, long addressId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean wasDefault = isDefault(conn, userId, addressId);
                String sql = "DELETE FROM user_addresses WHERE user_id = ? AND id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, addressId);
                    stmt.executeUpdate();
                }
                if (wasDefault) {
                    assignLatestAsDefault(conn, userId);
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void setDefault(long userId, long addressId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                clearDefault(conn, userId);
                setDefault(conn, userId, addressId);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void setDefault(Connection conn, long userId, long addressId) throws SQLException {
        String sql = "UPDATE user_addresses SET is_default = TRUE, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, addressId);
            stmt.executeUpdate();
        }
    }

    private static void clearDefault(Connection conn, long userId) throws SQLException {
        String sql = "UPDATE user_addresses SET is_default = FALSE WHERE user_id = ? AND is_default = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
    }

    private static boolean hasDefault(Connection conn, long userId) throws SQLException {
        String sql = "SELECT 1 FROM user_addresses WHERE user_id = ? AND is_default = TRUE LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean isDefault(Connection conn, long userId, long addressId) throws SQLException {
        String sql = "SELECT is_default FROM user_addresses WHERE user_id = ? AND id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, addressId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
                return false;
            }
        }
    }

    private static void assignLatestAsDefault(Connection conn, long userId) throws SQLException {
        String sql = "SELECT id FROM user_addresses WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    setDefault(conn, userId, rs.getLong(1));
                }
            }
        }
    }

    private static UserAddress map(ResultSet rs) throws SQLException {
        UserAddress address = new UserAddress();
        address.setId(rs.getLong("id"));
        address.setUserId(rs.getLong("user_id"));
        address.setLabel(rs.getString("label"));
        address.setRecipientName(rs.getString("recipient_name"));
        address.setPhone(rs.getString("phone"));
        address.setLine1(rs.getString("line1"));
        address.setLine2(rs.getString("line2"));
        address.setWard(rs.getString("ward"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setProvince(rs.getString("province"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCountry(rs.getString("country"));
        address.setDefault(rs.getBoolean("is_default"));
        address.setNote(rs.getString("note"));
        address.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        address.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return address;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
