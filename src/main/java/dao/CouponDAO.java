package dao;

import utils.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CouponDAO {

    private CouponDAO() {
    }

    public static List<CouponRecord> listActiveCoupons(long userId) throws SQLException {
        String sql = "SELECT c.id, c.code, c.description, c.coupon_type, c.value, c.max_discount, c.minimum_order, c.start_date, c.end_date, "
                + "c.status, uc.status AS user_status, uc.usage_count, uc.redeemed_at "
                + "FROM coupon_codes c LEFT JOIN user_coupons uc ON uc.coupon_id = c.id AND uc.user_id = ? "
                + "WHERE c.status = 'active' ORDER BY c.end_date NULLS LAST, c.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<CouponRecord> list = new ArrayList<>();
                while (rs.next()) {
                    CouponRecord record = new CouponRecord();
                    record.couponId = rs.getLong("id");
                    record.code = rs.getString("code");
                    record.description = rs.getString("description");
                    record.type = rs.getString("coupon_type");
                    record.value = rs.getBigDecimal("value");
                    record.maxDiscount = rs.getBigDecimal("max_discount");
                    record.minimumOrder = rs.getBigDecimal("minimum_order");
                    record.startDate = toLocalDateTime(rs.getTimestamp("start_date"));
                    record.endDate = toLocalDateTime(rs.getTimestamp("end_date"));
                    record.status = rs.getString("status");
                    record.userStatus = rs.getString("user_status");
                    record.usageCount = rs.getObject("usage_count") == null ? 0 : rs.getInt("usage_count");
                    record.redeemedAt = toLocalDateTime(rs.getTimestamp("redeemed_at"));
                    list.add(record);
                }
                return list;
            }
        }
    }

    public static void assignCouponToUser(long userId, long couponId) throws SQLException {
        String sql = "INSERT INTO user_coupons (user_id, coupon_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, couponId);
            stmt.executeUpdate();
        }
    }

    public static class CouponRecord {
        public long couponId;
        public String code;
        public String description;
        public String type;
        public BigDecimal value;
        public BigDecimal maxDiscount;
        public BigDecimal minimumOrder;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public String status;
        public String userStatus;
        public int usageCount;
        public LocalDateTime redeemedAt;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
