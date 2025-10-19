package dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Order;
import models.OrderItem;
import models.OrderStatusHistory;
import utils.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class OrderDAO {

    private static final Gson GSON = new Gson();
    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(Arrays.asList(
        "new",
        "confirmed",
        "shipping",
        "delivered",
        "cancelled",
        "returned"
    ));
    private static final Set<String> USER_CANCELLABLE_STATUSES = new HashSet<>(Arrays.asList(
        "new",
        "confirmed",
        "shipping"
    ));

    private OrderDAO() {
    }

    public static Order checkout(long userId, long addressId, String paymentMethod, String couponCode, String notes,
                                 String sessionId, List<ItemSelection> selections, String modeRaw, BigDecimal shippingFee) throws SQLException {
        if (selections == null || selections.isEmpty()) {
            throw new SQLException("Không có sản phẩm để thanh toán");
        }
        String normalizedMethod = normalizePaymentMethod(paymentMethod);
        CheckoutMode mode = CheckoutMode.from(modeRaw);
        BigDecimal effectiveShipping = shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0 ? shippingFee : BigDecimal.ZERO;
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                CartData cartData = mode == CheckoutMode.BUY_NOW
                        ? buildBuyNowCartData(conn, selections)
                        : loadCartForCheckout(conn, userId, sessionId, selections);
                if (cartData.items.isEmpty()) {
                    throw new SQLException("Không có sản phẩm hợp lệ để thanh toán");
                }
                AddressSnapshot address = loadAddress(conn, userId, addressId);
                CouponResult couponResult = CouponResult.empty();
                if (couponCode != null && !couponCode.trim().isEmpty()) {
                    couponResult = applyCoupon(conn, userId, couponCode.trim(), cartData.subtotal);
                }
                BigDecimal shipping = cartData.items.isEmpty() ? BigDecimal.ZERO : effectiveShipping;
                BigDecimal total = cartData.subtotal.add(shipping).subtract(couponResult.discount);
                if (total.compareTo(BigDecimal.ZERO) < 0) {
                    total = BigDecimal.ZERO;
                }
                String orderCode = generateOrderCode();
                long orderId = insertOrder(conn, userId, normalizedMethod, notes, address, cartData, shipping, total, couponResult, orderCode);
                insertOrderItems(conn, orderId, cartData);
                updateInventory(conn, cartData);
                recordStatus(conn, orderId, "new", "Đặt hàng thành công", String.valueOf(userId));
                createPaymentRecord(conn, orderId, normalizedMethod, total);
                if (couponResult.couponId != null) {
                    recordCouponUsage(conn, orderId, couponResult);
                }
                if (mode == CheckoutMode.CART) {
                    clearCartAfterCheckout(conn, cartData, selections);
                }
                Order order = fetchOrderByIdInternal(conn, orderId, userId);
                conn.commit();
                return order;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static List<Order> findOrders(long userId, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, code, user_id, order_date, status, payment_status, payment_method, payment_provider, items_subtotal, discount_amount, shipping_fee, total_amount, currency, coupon_code, notes, created_at, updated_at "
                + "FROM orders WHERE user_id = ?");
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append(" AND status = ?");
        }
        sql.append(" ORDER BY order_date DESC, id DESC");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, userId);
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                stmt.setString(2, statusFilter.trim());
            }
            List<Order> orders = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(findOrderItems(conn, order.getId(), order.getUserId()));
                    orders.add(order);
                }
            }
            return orders;
        }
    }

    public static Order fetchOrderById(long orderId, long userId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return fetchOrderByIdInternal(conn, orderId, userId);
        }
    }

    public static Order fetchOrderForAdmin(long orderId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return fetchOrderByIdInternal(conn, orderId, null);
        }
    }

    public static List<OrderStatusHistory> findStatusTimeline(long orderId, long userId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            fetchOrderByIdInternal(conn, orderId, userId);
            return loadStatusTimeline(conn, orderId);
        }
    }

    public static List<OrderStatusHistory> findStatusTimelineForAdmin(long orderId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            fetchOrderByIdInternal(conn, orderId, null);
            return loadStatusTimeline(conn, orderId);
        }
    }

    public static Order cancelOrder(long orderId, long userId, String reason) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Order existing = fetchOrderByIdInternal(conn, orderId, userId);
                String normalizedStatus = normalizeStatusValue(existing.getStatus());
                if (!isUserCancellableStatus(normalizedStatus)) {
                    throw new SQLException("Đơn hàng không thể hủy ở trạng thái hiện tại");
                }
                String trimmedReason = reason == null ? "" : reason.trim();
                if (trimmedReason.length() > 255) {
                    trimmedReason = trimmedReason.substring(0, 255);
                }
                int updated;
                String updateSql = "UPDATE orders SET status = 'cancelled', updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setLong(1, orderId);
                    stmt.setString(2, existing.getStatus());
                    updated = stmt.executeUpdate();
                }
                if (updated == 0) {
                    throw new SQLException("Đơn hàng không thể hủy ở trạng thái hiện tại hoặc đã được cập nhật");
                }
                String note = trimmedReason.isEmpty() ? "Khách hủy đơn hàng" : "Khách hủy: " + trimmedReason;
                recordStatus(conn, orderId, "cancelled", note, "user:" + userId);
                restoreInventory(conn, orderId);
                releaseCouponUsage(conn, orderId, userId);
                Order updatedOrder = fetchOrderByIdInternal(conn, orderId, userId);
                conn.commit();
                return updatedOrder;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static Order fetchOrderByIdInternal(Connection conn, long orderId, Long userId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT o.id, o.code, o.user_id, o.order_date, o.status, o.payment_status, o.payment_method, o.payment_provider, o.items_subtotal, o.discount_amount, o.shipping_fee, o.total_amount, o.currency, o.coupon_code, o.notes, o.created_at, o.updated_at, "
                + "u.email AS customer_email, COALESCE(NULLIF(u.full_name, ''), NULLIF(u.username, ''), u.email) AS customer_name "
                + "FROM orders o LEFT JOIN users u ON u.id = o.user_id WHERE o.id = ?");
        if (userId != null) {
            sql.append(" AND o.user_id = ?");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, orderId);
            if (userId != null) {
                stmt.setLong(2, userId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Order not found: " + orderId);
                }
                Order order = mapOrder(rs);
                order.setCustomerEmail(rs.getString("customer_email"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setItems(findOrderItems(conn, orderId, order.getUserId()));
                return order;
            }
        }
    }

    private static List<OrderStatusHistory> loadStatusTimeline(Connection conn, long orderId) throws SQLException {
        String sql = "SELECT id, order_id, status, note, created_at, created_by FROM order_status_history WHERE order_id = ? ORDER BY created_at";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderStatusHistory> timeline = new ArrayList<>();
                while (rs.next()) {
                    OrderStatusHistory history = new OrderStatusHistory();
                    history.setId(rs.getLong("id"));
                    history.setOrderId(rs.getLong("order_id"));
                    history.setStatus(rs.getString("status"));
                    history.setNote(rs.getString("note"));
                    history.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                    history.setCreatedBy(rs.getString("created_by"));
                    timeline.add(history);
                }
                return timeline;
            }
        }
    }

    public static List<AdminOrderSummary> listOrdersForAdmin(String statusFilter, String keyword, int limit) throws SQLException {
        String normalizedStatus = normalizeStatusValue(statusFilter);
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.id, o.code, o.status, o.payment_status, o.payment_method, o.total_amount, o.shipping_fee, o.order_date, o.updated_at, ")
                .append("u.email, COALESCE(NULLIF(u.full_name, ''), NULLIF(u.username, ''), u.email) AS customer_name ")
                .append("FROM orders o LEFT JOIN users u ON u.id = o.user_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (normalizedStatus != null && !"all".equals(normalizedStatus)) {
            sql.append(" AND LOWER(o.status) = ?");
            params.add(normalizedStatus);
        }
        if (keyword != null) {
            String trimmed = keyword.trim();
            if (!trimmed.isEmpty()) {
                String pattern = "%" + trimmed.toLowerCase(Locale.US) + "%";
                sql.append(" AND (LOWER(o.code) LIKE ? OR LOWER(COALESCE(u.email, '')) LIKE ? OR LOWER(COALESCE(u.full_name, '')) LIKE ?)");
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
        }
        sql.append(" ORDER BY o.order_date DESC NULLS LAST, o.id DESC LIMIT ?");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object param : params) {
                stmt.setObject(index++, param);
            }
            stmt.setInt(index, safeLimit);
            try (ResultSet rs = stmt.executeQuery()) {
                List<AdminOrderSummary> orders = new ArrayList<>();
                while (rs.next()) {
                    AdminOrderSummary summary = new AdminOrderSummary();
                    summary.id = rs.getLong("id");
                    summary.code = rs.getString("code");
                    summary.status = rs.getString("status");
                    summary.paymentStatus = rs.getString("payment_status");
                    summary.paymentMethod = rs.getString("payment_method");
                    summary.totalAmount = rs.getBigDecimal("total_amount");
                    summary.shippingFee = rs.getBigDecimal("shipping_fee");
                    summary.orderDate = toLocalDateTime(rs.getTimestamp("order_date"));
                    summary.updatedAt = toLocalDateTime(rs.getTimestamp("updated_at"));
                    summary.customerEmail = rs.getString("email");
                    summary.customerName = rs.getString("customer_name");
                    orders.add(summary);
                }
                return orders;
            }
        }
    }

    public static void updateOrderStatus(long orderId, String newStatus, String note, String actor) throws SQLException {
        String normalizedStatus = normalizeStatusValue(newStatus);
        if (normalizedStatus == null || !ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new SQLException("Trạng thái đơn hàng không hợp lệ");
        }
        String effectiveNote = note != null && !note.trim().isEmpty() ? note.trim() : defaultNoteForStatus(normalizedStatus);
        String createdBy = actor != null && !actor.trim().isEmpty() ? actor.trim() : "admin";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                    stmt.setString(1, normalizedStatus);
                    stmt.setLong(2, orderId);
                    updated = stmt.executeUpdate();
                }
                if (updated == 0) {
                    throw new SQLException("Order not found: " + orderId);
                }
                recordStatus(conn, orderId, normalizedStatus, effectiveNote, createdBy);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static String normalizeStatusValue(String status) {
        if (status == null) {
            return null;
        }
        String normalized = status.trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized;
    }

    private static String defaultNoteForStatus(String status) {
        switch (status) {
            case "confirmed":
                return "Đơn hàng đã được xác nhận";
            case "shipping":
                return "Đơn hàng đang được giao đến bạn";
            case "delivered":
            case "completed":
                return "Đơn hàng đã giao thành công";
            case "cancelled":
                return "Đơn hàng đã bị hủy";
            case "returned":
                return "Đơn hàng đã được hoàn trả";
            case "processing":
            case "pending":
                return "Đơn hàng đang được xử lý";
            case "failed":
                return "Đơn hàng gặp sự cố trong quá trình xử lý";
            default:
                return "Cập nhật trạng thái đơn hàng";
        }
    }

    private static Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setCode(rs.getString("code"));
        order.setUserId(rs.getLong("user_id"));
        order.setOrderDate(toLocalDateTime(rs.getTimestamp("order_date")));
        order.setStatus(rs.getString("status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentProvider(rs.getString("payment_provider"));
        order.setItemsSubtotal(rs.getBigDecimal("items_subtotal"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setShippingFee(rs.getBigDecimal("shipping_fee"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCurrency(rs.getString("currency"));
        order.setCouponCode(rs.getString("coupon_code"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        order.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return order;
    }

    private static List<OrderItem> findOrderItems(Connection conn, long orderId, Long userId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT oi.id, oi.order_id, oi.book_id, oi.quantity, oi.unit_price, oi.total_price, b.title, b.author, b.image_url");
        if (userId != null) {
            sql.append(", r.id AS review_id");
        }
        sql.append(" FROM order_items oi INNER JOIN books b ON b.id = oi.book_id");
        if (userId != null) {
            sql.append(" LEFT JOIN book_reviews r ON r.book_id = oi.book_id AND r.user_id = ?");
        }
        sql.append(" WHERE oi.order_id = ?");
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (userId != null) {
                stmt.setLong(index++, userId);
            }
            stmt.setLong(index, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setBookId(rs.getLong("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setTitle(rs.getString("title"));
                    item.setAuthor(rs.getString("author"));
                    item.setImageUrl(rs.getString("image_url"));
                    if (userId != null) {
                        long reviewId = rs.getLong("review_id");
                        if (rs.wasNull()) {
                            item.setReviewId(null);
                            item.setHasReview(false);
                        } else {
                            item.setReviewId(reviewId);
                            item.setHasReview(true);
                        }
                    } else {
                        item.setReviewId(null);
                        item.setHasReview(false);
                    }
                    items.add(item);
                }
                return items;
            }
        }
    }

    private static void insertOrderItems(Connection conn, long orderId, CartData cartData) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, book_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartLine item : cartData.items) {
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.bookId);
                stmt.setInt(3, item.quantity);
                stmt.setBigDecimal(4, item.unitPrice);
                stmt.setBigDecimal(5, item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void updateInventory(Connection conn, CartData cartData) throws SQLException {
        String sql = "UPDATE books SET stock_quantity = stock_quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND stock_quantity >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartLine item : cartData.items) {
                stmt.setInt(1, item.quantity);
                stmt.setLong(2, item.bookId);
                stmt.setInt(3, item.quantity);
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            for (int result : results) {
                if (result == 0) {
                    throw new SQLException("Insufficient stock for one of the books");
                }
            }
        }
    }

    private static void recordStatus(Connection conn, long orderId, String status, String note, String createdBy) throws SQLException {
        String sql = "INSERT INTO order_status_history (order_id, status, note, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, status);
            stmt.setString(3, note);
            stmt.setString(4, createdBy);
            stmt.executeUpdate();
        }
    }

    private static void restoreInventory(Connection conn, long orderId) throws SQLException {
        if (!columnExists(conn, "books", "stock_quantity")) {
            return;
        }
        Map<Long, Integer> restock = new HashMap<>();
        String sql = "SELECT book_id, quantity FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long bookId = rs.getLong("book_id");
                    int quantity = rs.getInt("quantity");
                    restock.merge(bookId, quantity, Integer::sum);
                }
            }
        }
        if (restock.isEmpty()) {
            return;
        }
        String updateSql = "UPDATE books SET stock_quantity = stock_quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            for (Map.Entry<Long, Integer> entry : restock.entrySet()) {
                stmt.setInt(1, entry.getValue());
                stmt.setLong(2, entry.getKey());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void releaseCouponUsage(Connection conn, long orderId, long userId) throws SQLException {
        String sql = "SELECT coupon_id FROM order_coupons WHERE order_id = ? AND coupon_id IS NOT NULL";
        List<Long> couponIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long couponId = rs.getLong("coupon_id");
                    if (!rs.wasNull()) {
                        couponIds.add(couponId);
                    }
                }
            }
        }
        if (couponIds.isEmpty()) {
            return;
        }
        String updateSql = "UPDATE user_coupons SET usage_count = GREATEST(COALESCE(usage_count, 0) - 1, 0), status = 'available', redeemed_at = NULL WHERE user_id = ? AND coupon_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            for (Long couponId : couponIds) {
                stmt.setLong(1, userId);
                stmt.setLong(2, couponId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void createPaymentRecord(Connection conn, long orderId, String method, BigDecimal total) throws SQLException {
        String status = "cod".equals(method) ? "pending" : "processing";
        String sql = "INSERT INTO order_payments (order_id, method, provider, status, amount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, method);
            stmt.setString(3, providerFor(method));
            stmt.setString(4, status);
            stmt.setBigDecimal(5, total);
            stmt.executeUpdate();
        }
    }

    private static void recordCouponUsage(Connection conn, long orderId, CouponResult coupon) throws SQLException {
        String sql = "INSERT INTO order_coupons (order_id, coupon_id, code, discount_amount, snapshot) VALUES (?, ?, ?, ?, ?::jsonb)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            if (coupon.couponId == null) {
                stmt.setNull(2, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(2, coupon.couponId);
            }
            stmt.setString(3, coupon.code);
            stmt.setBigDecimal(4, coupon.discount);
            stmt.setString(5, coupon.snapshotJson);
            stmt.executeUpdate();
        }
        if (coupon.couponId != null) {
            String sqlUsage = "UPDATE user_coupons SET usage_count = usage_count + 1, redeemed_at = CURRENT_TIMESTAMP, status = 'used' WHERE coupon_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsage)) {
                stmt.setLong(1, coupon.couponId);
                stmt.setLong(2, coupon.userId);
                stmt.executeUpdate();
            }
        }
    }

    private static long insertOrder(Connection conn, long userId, String paymentMethod, String notes, AddressSnapshot address, CartData cartData,
                                    BigDecimal shippingFee, BigDecimal total, CouponResult coupon, String orderCode) throws SQLException {
        String sql = "INSERT INTO orders (user_id, code, status, payment_status, payment_method, payment_provider, shipping_address_id, shipping_snapshot, cart_snapshot, items_subtotal, discount_amount, shipping_fee, total_amount, currency, coupon_code, coupon_snapshot, notes) "
                + "VALUES (?, ?, 'new', 'unpaid', ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, 'VND', ?, ?::jsonb, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, orderCode);
            stmt.setString(3, paymentMethod);
            stmt.setString(4, providerFor(paymentMethod));
            if (address.addressId == null) {
                stmt.setNull(5, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(5, address.addressId);
            }
            stmt.setString(6, address.jsonSnapshot);
            stmt.setString(7, cartData.snapshotJson);
            stmt.setBigDecimal(8, cartData.subtotal);
            stmt.setBigDecimal(9, coupon.discount);
            stmt.setBigDecimal(10, shippingFee);
            stmt.setBigDecimal(11, total);
            stmt.setString(12, coupon.code);
            stmt.setString(13, coupon.snapshotJson);
            stmt.setString(14, notes);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to insert order");
            }
        }
    }

    private static void clearCartAfterCheckout(Connection conn, CartData cartData, List<ItemSelection> selections) throws SQLException {
        if (cartData.cartId == null) {
            return;
        }
        if (selections == null || selections.isEmpty()) {
            String deleteItems = "DELETE FROM cart_items WHERE cart_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteItems)) {
                stmt.setLong(1, cartData.cartId);
                stmt.executeUpdate();
            }
            String updateCart = "UPDATE carts SET status = 'checked_out', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateCart)) {
                stmt.setLong(1, cartData.cartId);
                stmt.executeUpdate();
            }
            return;
        }

        String deleteSql = "DELETE FROM cart_items WHERE cart_id = ? AND book_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            for (ItemSelection selection : selections) {
                stmt.setLong(1, cartData.cartId);
                stmt.setLong(2, selection.getBookId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            stmt.setLong(1, cartData.cartId);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM cart_items WHERE cart_id = ?")) {
            stmt.setLong(1, cartData.cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement update = conn.prepareStatement("UPDATE carts SET status = 'checked_out', updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                        update.setLong(1, cartData.cartId);
                        update.executeUpdate();
                    }
                }
            }
        }
    }

    private static CartData buildBuyNowCartData(Connection conn, List<ItemSelection> selections) throws SQLException {
        CartData cartData = new CartData();
        boolean hasOriginalPrice = columnExists(conn, "books", "original_price");
        boolean hasStockQuantity = columnExists(conn, "books", "stock_quantity");
        StringBuilder sql = new StringBuilder("SELECT id, title, author, image_url, price");
        if (hasOriginalPrice) {
            sql.append(", original_price");
        }
        if (hasStockQuantity) {
            sql.append(", stock_quantity");
        }
        sql.append(" FROM books WHERE id = ?");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (ItemSelection selection : selections) {
                stmt.setLong(1, selection.getBookId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy sách với mã " + selection.getBookId());
                    }
                    CartLine item = new CartLine();
                    item.bookId = rs.getLong("id");
                    item.quantity = selection.getQuantity();
                    item.unitPrice = rs.getBigDecimal("price");
                    if ((item.unitPrice == null || item.unitPrice.compareTo(BigDecimal.ZERO) <= 0) && hasOriginalPrice) {
                        BigDecimal fallback = rs.getBigDecimal("original_price");
                        if (fallback != null && fallback.compareTo(BigDecimal.ZERO) > 0) {
                            item.unitPrice = fallback;
                        }
                    }
                    if (item.unitPrice == null) {
                        item.unitPrice = BigDecimal.ZERO;
                    }
                    item.title = rs.getString("title");
                    item.author = rs.getString("author");
                    item.imageUrl = rs.getString("image_url");
                    if (hasStockQuantity) {
                        item.stockQuantity = rs.getInt("stock_quantity");
                        if (!rs.wasNull() && item.quantity > item.stockQuantity) {
                            throw new SQLException("Số lượng sách \"" + item.title + "\" vượt quá tồn kho");
                        }
                    }
                    cartData.items.add(item);
                    cartData.subtotal = cartData.subtotal.add(item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)));
                }
                stmt.clearParameters();
            }
        }
        cartData.snapshotJson = buildCartSnapshot(cartData);
        return cartData;
    }

    private static CartData loadCartForCheckout(Connection conn, long userId, String sessionId, List<ItemSelection> selections) throws SQLException {
        if (selections == null || selections.isEmpty()) {
            throw new SQLException("Không có sản phẩm trong giỏ để thanh toán");
        }
        CartData cartData = new CartData();
        Map<Long, ItemSelection> selectionMap = new HashMap<>();
        for (ItemSelection selection : selections) {
            selectionMap.put(selection.getBookId(), selection);
        }
        StringBuilder sql = new StringBuilder("SELECT c.id AS cart_id, ci.book_id, ci.quantity, ci.unit_price, b.title, b.author, b.image_url, b.stock_quantity "
                + "FROM carts c "
                + "INNER JOIN cart_items ci ON ci.cart_id = c.id "
                + "INNER JOIN books b ON b.id = ci.book_id "
                + "WHERE c.status = 'active' AND (c.user_id = ? OR (c.user_id IS NULL AND c.session_id = ?)) "
                + "AND ci.book_id IN (");
        for (int i = 0; i < selections.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(')');

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            stmt.setLong(index++, userId);
            stmt.setString(index++, sessionId);
            for (ItemSelection selection : selections) {
                stmt.setLong(index++, selection.getBookId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cartData.cartId = rs.getLong("cart_id");
                    CartLine item = new CartLine();
                    item.bookId = rs.getLong("book_id");
                    ItemSelection requested = selectionMap.get(item.bookId);
                    if (requested == null) {
                        continue;
                    }
                    int storedQuantity = rs.getInt("quantity");
                    item.quantity = requested.getQuantity();
                    if (item.quantity > storedQuantity) {
                        throw new SQLException("Số lượng sách \"" + rs.getString("title") + "\" vượt quá số lượng trong giỏ hàng");
                    }
                    item.unitPrice = rs.getBigDecimal("unit_price");
                    if (item.unitPrice == null) {
                        item.unitPrice = BigDecimal.ZERO;
                    }
                    item.title = rs.getString("title");
                    item.author = rs.getString("author");
                    item.imageUrl = rs.getString("image_url");
                    item.stockQuantity = rs.getInt("stock_quantity");
                    if (!rs.wasNull() && item.quantity > item.stockQuantity) {
                        throw new SQLException("Số lượng sách \"" + item.title + "\" vượt quá tồn kho");
                    }
                    cartData.items.add(item);
                    cartData.subtotal = cartData.subtotal.add(item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)));
                }
            }
        }
        if (cartData.cartId == null) {
            throw new SQLException("Không tìm thấy giỏ hàng để thanh toán");
        }
        if (cartData.items.size() != selectionMap.size()) {
            throw new SQLException("Một số sản phẩm đã bị xoá khỏi giỏ hàng, vui lòng tải lại trang");
        }
        cartData.snapshotJson = buildCartSnapshot(cartData);
        return cartData;
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT))) {
            return rs.next();
        }
    }

    private static boolean isUserCancellableStatus(String status) {
        if (status == null) {
            return false;
        }
        return USER_CANCELLABLE_STATUSES.contains(status);
    }

    private static AddressSnapshot loadAddress(Connection conn, long userId, long addressId) throws SQLException {
        String sql = "SELECT id, recipient_name, phone, line1, line2, ward, district, city, province, postal_code, country, note FROM user_addresses WHERE user_id = ? AND id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, addressId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Địa chỉ không tồn tại");
                }
                JsonObject json = new JsonObject();
                json.addProperty("recipientName", rs.getString("recipient_name"));
                json.addProperty("phone", rs.getString("phone"));
                json.addProperty("line1", rs.getString("line1"));
                json.addProperty("line2", rs.getString("line2"));
                json.addProperty("ward", rs.getString("ward"));
                json.addProperty("district", rs.getString("district"));
                json.addProperty("city", rs.getString("city"));
                json.addProperty("province", rs.getString("province"));
                json.addProperty("postalCode", rs.getString("postal_code"));
                json.addProperty("country", rs.getString("country"));
                json.addProperty("note", rs.getString("note"));
                AddressSnapshot snapshot = new AddressSnapshot();
                snapshot.addressId = rs.getLong("id");
                snapshot.jsonSnapshot = GSON.toJson(json);
                return snapshot;
            }
        }
    }

    private static CouponResult applyCoupon(Connection conn, long userId, String code, BigDecimal subtotal) throws SQLException {
        String sql = "SELECT id, coupon_type, value, max_discount, minimum_order, usage_limit, per_user_limit, start_date, end_date, status, description FROM coupon_codes WHERE code = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Mã giảm giá không hợp lệ");
                }
                CouponResult result = new CouponResult();
                result.code = code;
                result.couponId = rs.getLong("id");
                result.userId = userId;
                result.type = rs.getString("coupon_type");
                result.value = rs.getBigDecimal("value");
                result.maxDiscount = rs.getBigDecimal("max_discount");
                result.minimumOrder = rs.getBigDecimal("minimum_order");
                result.usageLimit = rs.getObject("usage_limit") != null ? rs.getInt("usage_limit") : null;
                result.perUserLimit = rs.getObject("per_user_limit") != null ? rs.getInt("per_user_limit") : null;
                result.startDate = toLocalDateTime(rs.getTimestamp("start_date"));
                result.endDate = toLocalDateTime(rs.getTimestamp("end_date"));
                result.status = rs.getString("status");
                validateCoupon(conn, result, subtotal);
                result.discount = calculateDiscount(result, subtotal);
                JsonObject snapshot = new JsonObject();
                snapshot.addProperty("type", result.type);
                snapshot.addProperty("value", result.value);
                snapshot.addProperty("discount", result.discount);
                snapshot.addProperty("description", rs.getString("description"));
                result.snapshotJson = GSON.toJson(snapshot);
                return result;
            }
        }
    }

    private static void validateCoupon(Connection conn, CouponResult coupon, BigDecimal subtotal) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        if (!"active".equalsIgnoreCase(coupon.status)) {
            throw new SQLException("Mã giảm giá đã hết hiệu lực");
        }
        if (coupon.startDate != null && now.isBefore(coupon.startDate)) {
            throw new SQLException("Mã giảm giá chưa bắt đầu áp dụng");
        }
        if (coupon.endDate != null && now.isAfter(coupon.endDate)) {
            throw new SQLException("Mã giảm giá đã hết hạn");
        }
        if (coupon.minimumOrder != null && subtotal.compareTo(coupon.minimumOrder) < 0) {
            throw new SQLException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá");
        }
        if (coupon.usageLimit != null) {
            int used = countUsage(conn, coupon.couponId, null);
            if (used >= coupon.usageLimit) {
                throw new SQLException("Mã giảm giá đã đạt số lần sử dụng tối đa");
            }
        }
        if (coupon.perUserLimit != null) {
            int usedByUser = countUsage(conn, coupon.couponId, coupon.userId);
            if (usedByUser >= coupon.perUserLimit) {
                throw new SQLException("Bạn đã sử dụng mã giảm giá này tối đa số lần cho phép");
            }
        }
    }

    private static int countUsage(Connection conn, long couponId, Long userId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM order_coupons WHERE coupon_id = ?");
        if (userId != null) {
            sql.append(" AND order_id IN (SELECT id FROM orders WHERE user_id = ?)");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, couponId);
            if (userId != null) {
                stmt.setLong(2, userId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    private static BigDecimal calculateDiscount(CouponResult coupon, BigDecimal subtotal) throws SQLException {
        BigDecimal discount;
        if ("percentage".equalsIgnoreCase(coupon.type)) {
            discount = subtotal.multiply(coupon.value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.maxDiscount != null && discount.compareTo(coupon.maxDiscount) > 0) {
                discount = coupon.maxDiscount;
            }
        } else if ("fixed".equalsIgnoreCase(coupon.type)) {
            discount = coupon.value;
        } else {
            throw new SQLException("Loại mã giảm giá không được hỗ trợ");
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return discount;
    }

    private static String buildCartSnapshot(CartData cartData) {
        JsonArray itemsJson = new JsonArray();
        for (CartLine item : cartData.items) {
            JsonObject node = new JsonObject();
            node.addProperty("bookId", item.bookId);
            node.addProperty("quantity", item.quantity);
            node.addProperty("unitPrice", item.unitPrice);
            node.addProperty("title", item.title);
            node.addProperty("author", item.author);
            node.addProperty("imageUrl", item.imageUrl);
            itemsJson.add(node);
        }
        JsonObject snapshot = new JsonObject();
        snapshot.add("items", itemsJson);
        snapshot.addProperty("subtotal", cartData.subtotal);
        return GSON.toJson(snapshot);
    }

    private static String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return "cod";
        }
        String normalized = paymentMethod.trim().toLowerCase(Locale.US);
        switch (normalized) {
            case "cod":
            case "vnpay":
            case "momo":
                return normalized;
            default:
                return "cod";
        }
    }

    private static String providerFor(String method) {
        switch (method) {
            case "vnpay":
                return "VNPAY";
            case "momo":
                return "MOMO";
            default:
                return "COD";
        }
    }

    private static String generateOrderCode() {
        return "OD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.US);
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    public static class AdminOrderSummary {
        public long id;
        public String code;
        public String status;
        public String paymentStatus;
        public String paymentMethod;
        public BigDecimal totalAmount;
        public BigDecimal shippingFee;
        public LocalDateTime orderDate;
        public LocalDateTime updatedAt;
        public String customerName;
        public String customerEmail;
    }

    private enum CheckoutMode {
        CART,
        BUY_NOW;

        static CheckoutMode from(String raw) {
            if (raw == null) {
                return CART;
            }
            return "buy-now".equalsIgnoreCase(raw) ? BUY_NOW : CART;
        }
    }

    public static final class ItemSelection {
        private final long bookId;
        private final int quantity;

        public ItemSelection(long bookId, int quantity) {
            this.bookId = bookId;
            this.quantity = quantity;
        }

        public long getBookId() {
            return bookId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    private static class CartData {
        private Long cartId;
        private final List<CartLine> items = new ArrayList<>();
        private BigDecimal subtotal = BigDecimal.ZERO;
        private String snapshotJson;
    }

    private static class CartLine {
        private long bookId;
        private int quantity;
        private BigDecimal unitPrice;
        private String title;
        private String author;
        private String imageUrl;
        private int stockQuantity;
    }

    private static class AddressSnapshot {
        private Long addressId;
        private String jsonSnapshot;
    }

    private static class CouponResult {
        private Long couponId;
        private long userId;
        private String code;
        private String type;
        private BigDecimal value;
        private BigDecimal maxDiscount;
        private BigDecimal minimumOrder;
        private Integer usageLimit;
        private Integer perUserLimit;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String status;
        private BigDecimal discount = BigDecimal.ZERO;
        private String snapshotJson;

        private static CouponResult empty() {
            CouponResult result = new CouponResult();
            result.couponId = null;
            result.code = null;
            result.discount = BigDecimal.ZERO;
            result.snapshotJson = null;
            return result;
        }
    }
}
