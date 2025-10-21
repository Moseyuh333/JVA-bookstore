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

    public static CouponValidationResult validateCouponCode(String code, long userId) throws SQLException {
        String sql = "SELECT c.id, c.code, c.description, c.coupon_type, c.value, c.max_discount, c.minimum_order, "
                + "c.usage_limit, c.per_user_limit, c.start_date, c.end_date, c.status "
                + "FROM coupon_codes c WHERE UPPER(c.code) = UPPER(?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                CouponValidationResult result = new CouponValidationResult();
                
                if (!rs.next()) {
                    result.isValid = false;
                    result.message = "Mã giảm giá không tồn tại";
                    return result;
                }
                
                result.couponId = rs.getLong("id");
                result.code = rs.getString("code");
                result.description = rs.getString("description");
                result.type = rs.getString("coupon_type");
                result.value = rs.getBigDecimal("value");
                result.maxDiscount = rs.getBigDecimal("max_discount");
                result.minimumOrder = rs.getBigDecimal("minimum_order");
                Integer usageLimit = rs.getObject("usage_limit") != null ? rs.getInt("usage_limit") : null;
                Integer perUserLimit = rs.getObject("per_user_limit") != null ? rs.getInt("per_user_limit") : null;
                result.startDate = toLocalDateTime(rs.getTimestamp("start_date"));
                result.endDate = toLocalDateTime(rs.getTimestamp("end_date"));
                result.status = rs.getString("status");
                
                // Validate status
                if (!"active".equalsIgnoreCase(result.status)) {
                    result.isValid = false;
                    result.message = "Mã giảm giá đã hết hiệu lực";
                    return result;
                }
                
                // Validate date range
                LocalDateTime now = LocalDateTime.now();
                if (result.startDate != null && now.isBefore(result.startDate)) {
                    result.isValid = false;
                    result.message = "Mã giảm giá chưa bắt đầu áp dụng";
                    return result;
                }
                if (result.endDate != null && now.isAfter(result.endDate)) {
                    result.isValid = false;
                    result.message = "Mã giảm giá đã hết hạn";
                    return result;
                }
                
                // Check total usage limit
                if (usageLimit != null) {
                    int totalUsed = countTotalUsage(conn, result.couponId);
                    if (totalUsed >= usageLimit) {
                        result.isValid = false;
                        result.message = "Mã giảm giá đã đạt số lần sử dụng tối đa";
                        return result;
                    }
                }
                
                // Check per-user usage limit
                if (perUserLimit != null) {
                    int userUsed = countUserUsage(conn, result.couponId, userId);
                    if (userUsed >= perUserLimit) {
                        result.isValid = false;
                        result.message = "Bạn đã sử dụng mã giảm giá này tối đa số lần cho phép";
                        return result;
                    }
                }
                
                result.isValid = true;
                result.message = "Mã giảm giá hợp lệ";
                return result;
            }
        }
    }
    
    private static int countTotalUsage(Connection conn, long couponId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_coupons WHERE coupon_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, couponId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }
    
    private static int countUserUsage(Connection conn, long couponId, long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_coupons oc "
                + "INNER JOIN orders o ON o.id = oc.order_id "
                + "WHERE oc.coupon_id = ? AND o.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, couponId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
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

    public static class CouponValidationResult {
        public boolean isValid;
        public String message;
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
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
