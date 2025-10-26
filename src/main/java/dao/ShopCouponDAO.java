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

public final class ShopCouponDAO {

    private ShopCouponDAO() {
    }

    public static List<ShopCoupon> listByShop(int shopId) throws SQLException {
        String sql = "SELECT id, shop_id, code, description, discount_type, discount_value, minimum_order, " +
                "usage_limit, used_count, status, start_date, end_date, created_at, updated_at " +
                "FROM shop_coupons WHERE shop_id = ? ORDER BY created_at DESC";

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
        coupon.setUsageLimit(rs.wasNull() ? null : usageLimit);
        coupon.setUsedCount(rs.getInt("used_count"));
        coupon.setStatus(rs.getString("status"));
        Timestamp start = rs.getTimestamp("start_date");
        Timestamp end = rs.getTimestamp("end_date");
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        coupon.setStartDate(start != null ? start.toLocalDateTime() : null);
        coupon.setEndDate(end != null ? end.toLocalDateTime() : null);
        coupon.setCreatedAt(created != null ? created.toLocalDateTime() : null);
        coupon.setUpdatedAt(updated != null ? updated.toLocalDateTime() : null);
        return coupon;
    }
}
