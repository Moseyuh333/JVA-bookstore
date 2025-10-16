package dao;

import models.Order;
import utils.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public class OrderDAO {
    
    /**
     * Create a new order and return the order ID
     */
    public static int createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, order_date, total_amount, status, " +
                    "shipping_address, payment_method, delivery_address_id, coupon_id, " +
                    "discount_amount, final_total, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, order.getUserId());
            
            // Convert LocalDateTime to Timestamp
            if (order.getOrderDate() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            } else {
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setString(4, order.getStatus() != null ? order.getStatus() : "pending");
            stmt.setString(5, order.getShippingAddress());
            stmt.setString(6, order.getPaymentMethod());
            
            if (order.getDeliveryAddressId() > 0) {
                stmt.setInt(7, order.getDeliveryAddressId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            
            if (order.getCouponId() > 0) {
                stmt.setInt(8, order.getCouponId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            
            stmt.setBigDecimal(9, order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
            stmt.setBigDecimal(10, order.getFinalTotal());
            stmt.setString(11, order.getNotes());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Get all orders by user ID
     */
    public static List<Order> getOrdersByUserId(int userId) {
        String sql = "SELECT id, user_id, order_date, total_amount, status, shipping_address, " +
                    "payment_method, delivery_address_id, coupon_id, discount_amount, final_total, " +
                    "notes, created_at, updated_at FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        
        return queryOrders(sql, new Object[]{userId});
    }
    
    /**
     * Get orders by user ID and status
     */
    public static List<Order> getOrdersByStatus(int userId, String status) {
        String sql = "SELECT id, user_id, order_date, total_amount, status, shipping_address, " +
                    "payment_method, delivery_address_id, coupon_id, discount_amount, final_total, " +
                    "notes, created_at, updated_at FROM orders WHERE user_id = ? AND status = ? " +
                    "ORDER BY order_date DESC";
        
        return queryOrders(sql, new Object[]{userId, status});
    }
    
    /**
     * Get order by ID
     */
    public static Order getOrderById(int orderId) {
        String sql = "SELECT id, user_id, order_date, total_amount, status, shipping_address, " +
                    "payment_method, delivery_address_id, coupon_id, discount_amount, final_total, " +
                    "notes, created_at, updated_at FROM orders WHERE id = ?";
        
        List<Order> orders = queryOrders(sql, new Object[]{orderId});
        return orders.isEmpty() ? null : orders.get(0);
    }
    
    /**
     * Update order status
     */
    public static boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Cancel order (set status to 'cancelled')
     */
    public static boolean cancelOrder(int orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }
    
    /**
     * Check if user has purchased a specific book
     */
    public static boolean hasUserPurchasedBook(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "WHERE o.user_id = ? AND oi.book_id = ? AND o.status IN ('delivered', 'confirmed', 'shipping')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get total order count by user
     */
    public static int getOrderCountByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Helper method to execute queries and map results to Order objects
     */
    private static List<Order> queryOrders(String sql, Object[] params) {
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp orderDateTs = rs.getTimestamp("order_date");
                if (orderDateTs != null) {
                    order.setOrderDate(orderDateTs.toLocalDateTime());
                }
                
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setShippingAddress(rs.getString("shipping_address"));
                order.setPaymentMethod(rs.getString("payment_method"));
                
                int deliveryAddressId = rs.getInt("delivery_address_id");
                if (!rs.wasNull()) {
                    order.setDeliveryAddressId(deliveryAddressId);
                }
                
                int couponId = rs.getInt("coupon_id");
                if (!rs.wasNull()) {
                    order.setCouponId(couponId);
                }
                
                order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                order.setFinalTotal(rs.getBigDecimal("final_total"));
                order.setNotes(rs.getString("notes"));
                
                // Convert Timestamps to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    order.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    order.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }
}
