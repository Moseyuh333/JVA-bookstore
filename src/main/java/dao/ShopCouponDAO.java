package dao;

import models.ShopCoupon;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ShopCouponDAO {

    private ShopCouponDAO() {
    }

    public static List<ShopCoupon> listByShop(int shopId) throws SQLException {
        String sql = "SELECT sc.id, sc.shop_id, sc.code, sc.description, sc.discount_type, sc.discount_value, sc.minimum_order, " +
                "sc.usage_limit, sc.used_count, sc.status, sc.start_date, sc.end_date, sc.created_at, sc.updated_at, s.name AS shop_name " +
                "FROM shop_coupons sc LEFT JOIN shops s ON s.id = sc.shop_id WHERE sc.shop_id = ? ORDER BY sc.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ShopCoupon> coupons = new ArrayList<>();
                while (rs.next()) {
                    coupons.add(mapRow(rs));
                }
                return coupons;
            }
        }
    }

    public static ShopCoupon findActiveForShop(Connection conn, int shopId, String code, boolean lockForUpdate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection is required");
        }
        String sql = "SELECT sc.id, sc.shop_id, sc.code, sc.description, sc.discount_type, sc.discount_value, sc.minimum_order, " +
                "sc.usage_limit, sc.used_count, sc.status, sc.start_date, sc.end_date, sc.created_at, sc.updated_at, " +
                "(SELECT name FROM shops WHERE id = sc.shop_id) AS shop_name " +
                "FROM shop_coupons sc WHERE sc.shop_id = ? AND UPPER(sc.code) = UPPER(?)";
        if (lockForUpdate) {
            sql += " FOR UPDATE";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            stmt.setString(2, code.toUpperCase(Locale.ROOT));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public static ShopCoupon findActiveForShop(int shopId, String code) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return findActiveForShop(conn, shopId, code, false);
        }
    }

    public static List<ShopCoupon> listActiveForShop(int shopId) throws SQLException {
        String sql = "SELECT sc.id, sc.shop_id, sc.code, sc.description, sc.discount_type, sc.discount_value, sc.minimum_order, " +
                "sc.usage_limit, sc.used_count, sc.status, sc.start_date, sc.end_date, sc.created_at, sc.updated_at, s.name AS shop_name " +
                "FROM shop_coupons sc LEFT JOIN shops s ON s.id = sc.shop_id " +
                "WHERE sc.shop_id = ? AND sc.status = 'active' " +
                "AND (sc.start_date IS NULL OR sc.start_date <= CURRENT_TIMESTAMP) " +
                "AND (sc.end_date IS NULL OR sc.end_date >= CURRENT_TIMESTAMP) " +
                "AND (sc.usage_limit IS NULL OR sc.used_count < sc.usage_limit) " +
                "ORDER BY sc.end_date NULLS LAST, sc.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ShopCoupon> coupons = new ArrayList<>();
                while (rs.next()) {
                    coupons.add(mapRow(rs));
                }
                return coupons;
            }
        }
    }

    public static void incrementUsage(Connection conn, int couponId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection is required");
        }
        String sql = "UPDATE shop_coupons SET used_count = COALESCE(used_count, 0) + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            stmt.executeUpdate();
        }
    }

    public static ShopCoupon createCoupon(ShopCoupon coupon) throws SQLException {
        String sql = "INSERT INTO shop_coupons " +
                "(shop_id, code, description, discount_type, discount_value, minimum_order, usage_limit, used_count, " +
                "start_date, end_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 'active') " +
                "RETURNING id, created_at, updated_at";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, coupon.getShopId());
            stmt.setString(2, coupon.getCode());
            stmt.setString(3, coupon.getDescription());
            stmt.setString(4, coupon.getDiscountType());
            stmt.setBigDecimal(5, coupon.getDiscountValue());
            stmt.setBigDecimal(6, coupon.getMinimumOrder());
            if (coupon.getUsageLimit() != null) {
                stmt.setInt(7, coupon.getUsageLimit());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            if (coupon.getStartDate() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(coupon.getStartDate()));
            } else {
                stmt.setNull(8, java.sql.Types.TIMESTAMP);
            }
            if (coupon.getEndDate() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(coupon.getEndDate()));
            } else {
                stmt.setNull(9, java.sql.Types.TIMESTAMP);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    coupon.setId(rs.getInt("id"));
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");
                    coupon.setCreatedAt(created != null ? created.toLocalDateTime() : null);
                    coupon.setUpdatedAt(updated != null ? updated.toLocalDateTime() : null);
                    coupon.setStatus("active");
                    coupon.setUsedCount(0);
                    return coupon;
                }
            }
        }
        throw new SQLException("Không thể tạo mã giảm giá mới");
    }

    public static boolean deleteCoupon(int shopId, int couponId) throws SQLException {
        String sql = "DELETE FROM shop_coupons WHERE id = ? AND shop_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            stmt.setInt(2, shopId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    private static ShopCoupon mapRow(ResultSet rs) throws SQLException {
        ShopCoupon coupon = new ShopCoupon();
        coupon.setId(rs.getInt("id"));
        coupon.setShopId(rs.getInt("shop_id"));
        coupon.setCode(rs.getString("code"));
        coupon.setDescription(rs.getString("description"));
        coupon.setDiscountType(rs.getString("discount_type"));
        coupon.setDiscountValue(rs.getBigDecimal("discount_value"));
        coupon.setMinimumOrder(rs.getBigDecimal("minimum_order"));
        int usageLimit = rs.getInt("usage_limit");
        boolean usageLimitWasNull = rs.wasNull();
        coupon.setUsageLimit(usageLimitWasNull ? null : usageLimit);
        int usedCount = rs.getInt("used_count");
        coupon.setUsedCount(rs.wasNull() ? 0 : usedCount);
        coupon.setStatus(rs.getString("status"));
        Timestamp start = rs.getTimestamp("start_date");
        Timestamp end = rs.getTimestamp("end_date");
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        coupon.setStartDate(start != null ? start.toLocalDateTime() : null);
        coupon.setEndDate(end != null ? end.toLocalDateTime() : null);
        coupon.setCreatedAt(created != null ? created.toLocalDateTime() : null);
        coupon.setUpdatedAt(updated != null ? updated.toLocalDateTime() : null);
        if (hasColumn(rs, "shop_name")) {
            coupon.setShopName(rs.getString("shop_name"));
        }
        return coupon;
    }

    private static boolean hasColumn(ResultSet rs, String columnLabel) {
        try {
            rs.findColumn(columnLabel);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
