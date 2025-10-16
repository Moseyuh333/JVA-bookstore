package dao;

import models.Coupon;
import utils.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class CouponDAO {
    
    /**
     * Get coupon by code
     */
    public static Coupon getByCouponCode(String code) {
        String sql = "SELECT id, code, description, discount_type, discount_value, " +
                    "min_purchase_amount, max_usage_count, usage_count, " +
                    "valid_from, valid_until, is_active, created_at, updated_at " +
                    "FROM coupons WHERE code = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapCoupon(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Validate if coupon can be used with given amount
     */
    public static boolean validateCoupon(String code, java.math.BigDecimal amount) {
        Coupon coupon = getByCouponCode(code);
        
        if (coupon == null) {
            return false;
        }

        // Check if active and within valid dates
        if (!coupon.isValidNow()) {
            return false;
        }

        // Check usage limit
        if (coupon.getMaxUsageCount() != null && coupon.getUsageCount() >= coupon.getMaxUsageCount()) {
            return false;
        }

        // Check minimum purchase amount
        if (coupon.getMinPurchaseAmount() != null && amount.compareTo(coupon.getMinPurchaseAmount()) < 0) {
            return false;
        }

        return true;
    }

    /**
     * Get discount amount for coupon
     */
    public static java.math.BigDecimal calculateDiscount(String code, java.math.BigDecimal amount) {
        Coupon coupon = getByCouponCode(code);
        
        if (coupon == null || !validateCoupon(code, amount)) {
            return java.math.BigDecimal.ZERO;
        }

        return coupon.calculateDiscount(amount);
    }

    /**
     * Increment usage count when coupon is used
     */
    public static boolean incrementUsage(String code) {
        String sql = "UPDATE coupons SET usage_count = usage_count + 1, updated_at = NOW() WHERE code = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get coupon by ID
     */
    public static Coupon getById(int couponId) {
        String sql = "SELECT id, code, description, discount_type, discount_value, " +
                    "min_purchase_amount, max_usage_count, usage_count, " +
                    "valid_from, valid_until, is_active, created_at, updated_at " +
                    "FROM coupons WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, couponId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapCoupon(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Add new coupon (admin function)
     */
    public static boolean addCoupon(Coupon coupon) {
        String sql = "INSERT INTO coupons (code, description, discount_type, discount_value, " +
                    "min_purchase_amount, max_usage_count, valid_from, valid_until, is_active, " +
                    "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, coupon.getCode());
            pstmt.setString(2, coupon.getDescription());
            pstmt.setString(3, coupon.getDiscountType());
            pstmt.setBigDecimal(4, coupon.getDiscountValue());
            pstmt.setBigDecimal(5, coupon.getMinPurchaseAmount());
            pstmt.setObject(6, coupon.getMaxUsageCount());
            pstmt.setTimestamp(7, Timestamp.valueOf(coupon.getValidFrom()));
            pstmt.setTimestamp(8, Timestamp.valueOf(coupon.getValidUntil()));
            pstmt.setBoolean(9, coupon.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update coupon (admin function)
     */
    public static boolean updateCoupon(Coupon coupon) {
        String sql = "UPDATE coupons SET description = ?, discount_type = ?, discount_value = ?, " +
                    "min_purchase_amount = ?, max_usage_count = ?, valid_from = ?, " +
                    "valid_until = ?, is_active = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, coupon.getDescription());
            pstmt.setString(2, coupon.getDiscountType());
            pstmt.setBigDecimal(3, coupon.getDiscountValue());
            pstmt.setBigDecimal(4, coupon.getMinPurchaseAmount());
            pstmt.setObject(5, coupon.getMaxUsageCount());
            pstmt.setTimestamp(6, Timestamp.valueOf(coupon.getValidFrom()));
            pstmt.setTimestamp(7, Timestamp.valueOf(coupon.getValidUntil()));
            pstmt.setBoolean(8, coupon.isActive());
            pstmt.setInt(9, coupon.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete coupon (admin function)
     */
    public static boolean deleteCoupon(int couponId) {
        String sql = "DELETE FROM coupons WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, couponId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Map ResultSet to Coupon object
     */
    private static Coupon mapCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getInt("id"));
        coupon.setCode(rs.getString("code"));
        coupon.setDescription(rs.getString("description"));
        coupon.setDiscountType(rs.getString("discount_type"));
        coupon.setDiscountValue(rs.getBigDecimal("discount_value"));
        coupon.setMinPurchaseAmount(rs.getBigDecimal("min_purchase_amount"));
        coupon.setMaxUsageCount((Integer) rs.getObject("max_usage_count"));
        coupon.setUsageCount(rs.getInt("usage_count"));
        
        Timestamp validFromTs = rs.getTimestamp("valid_from");
        Timestamp validUntilTs = rs.getTimestamp("valid_until");
        if (validFromTs != null) coupon.setValidFrom(validFromTs.toLocalDateTime());
        if (validUntilTs != null) coupon.setValidUntil(validUntilTs.toLocalDateTime());
        
        coupon.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (createdAtTs != null) coupon.setCreatedAt(createdAtTs.toLocalDateTime());
        if (updatedAtTs != null) coupon.setUpdatedAt(updatedAtTs.toLocalDateTime());
        
        return coupon;
    }
}
