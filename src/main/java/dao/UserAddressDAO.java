package dao;

import models.UserAddress;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class UserAddressDAO {

    private UserAddressDAO() {
    }

    public static List<UserAddress> findByUser(long userId) throws SQLException {
        ensureSchema();
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
        ensureSchema();
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
        ensureSchema();
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
        ensureSchema();
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
        ensureSchema();
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
        ensureSchema();
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

    private static volatile boolean schemaReady = false;

    private static void ensureSchema() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (UserAddressDAO.class) {
            if (schemaReady) {
                return;
            }
            try (Connection conn = DBUtil.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS user_addresses (" +
                        "id SERIAL PRIMARY KEY," +
                        "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                        "label VARCHAR(50)," +
                        "recipient_name VARCHAR(255) NOT NULL," +
                        "phone VARCHAR(20) NOT NULL," +
                        "line1 VARCHAR(255) NOT NULL," +
                        "line2 VARCHAR(255)," +
                        "ward VARCHAR(100)," +
                        "district VARCHAR(100)," +
                        "city VARCHAR(100)," +
                        "province VARCHAR(100)," +
                        "postal_code VARCHAR(20)," +
                        "country VARCHAR(100) DEFAULT 'Việt Nam'," +
                        "is_default BOOLEAN DEFAULT FALSE," +
                        "note TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS label VARCHAR(50)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(255)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS phone VARCHAR(20)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS line1 VARCHAR(255)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS line2 VARCHAR(255)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS ward VARCHAR(100)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS district VARCHAR(100)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS city VARCHAR(100)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS province VARCHAR(100)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20)");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS country VARCHAR(100) DEFAULT 'Việt Nam'");
                stmt.execute("ALTER TABLE user_addresses ALTER COLUMN country SET DEFAULT 'Việt Nam'");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS is_default BOOLEAN DEFAULT FALSE");
                stmt.execute("ALTER TABLE user_addresses ALTER COLUMN is_default SET DEFAULT FALSE");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS note TEXT");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                stmt.execute("ALTER TABLE user_addresses ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP");
                stmt.execute("ALTER TABLE user_addresses ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                stmt.execute("ALTER TABLE user_addresses ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP");

                stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_addresses_user ON user_addresses(user_id)");
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_user_addresses_default_true ON user_addresses(user_id) WHERE is_default");

                schemaReady = true;
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
