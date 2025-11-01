package dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import dao.ShopCouponDAO;
import models.Order;
import models.OrderItem;
import models.OrderStatusHistory;
import models.ShopCoupon;
import models.ShippingQuote;
import utils.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.postgresql.util.PGobject;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public final class OrderDAO {

    private static final Gson GSON = new Gson();
    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(Arrays.asList(
        "new",
        "confirmed",
        "shipping",
        "delivered",
        "cancelled",
        "returned",
        "failed"
    ));
    private static final Set<String> USER_CANCELLABLE_STATUSES = new HashSet<>(Arrays.asList(
        "new",
        "confirmed",
        "shipping"
    ));
    private static final Map<String, Set<String>> STATUS_TRANSITIONS;
    static {
        Map<String, Set<String>> transitions = new HashMap<>();
        transitions.put("new", Set.of("confirmed", "cancelled"));
        transitions.put("confirmed", Set.of("shipping", "cancelled"));
        transitions.put("shipping", Set.of("delivered", "failed", "returned", "cancelled"));
        transitions.put("failed", Set.of("shipping", "cancelled"));
        transitions.put("returned", Set.of("shipping", "cancelled"));
        transitions.put("delivered", Collections.emptySet());
        transitions.put("cancelled", Collections.emptySet());
        STATUS_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    private OrderDAO() {
    }

    public static Order checkout(long userId, long addressId, String paymentMethod, PaymentDetails paymentDetails, String couponCode, String notes,
                                 String sessionId, List<ItemSelection> selections, String modeRaw, BigDecimal shippingFee, ShippingQuote shippingQuote) throws SQLException {
        if (selections == null || selections.isEmpty()) {
            throw new SQLException("Không có sản phẩm để thanh toán");
        }
        String normalizedMethod = normalizePaymentMethod(paymentMethod);
        CheckoutMode mode = CheckoutMode.from(modeRaw);
        BigDecimal effectiveShipping = resolveShippingFee(shippingFee, shippingQuote);
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
                if (shippingQuote != null) {
                    address.attachShipping(shippingQuote);
                } else if (effectiveShipping.compareTo(BigDecimal.ZERO) > 0) {
                    address.attachShippingFee(effectiveShipping);
                }
                Integer shopId = resolvePrimaryShopId(cartData);
                CouponResult couponResult = CouponResult.empty();
                if (couponCode != null && !couponCode.trim().isEmpty()) {
                    couponResult = applyCoupon(conn, userId, couponCode.trim(), cartData.subtotal, shopId);
                }
                BigDecimal shipping = cartData.items.isEmpty() ? BigDecimal.ZERO : effectiveShipping;
                BigDecimal total = cartData.subtotal.add(shipping).subtract(couponResult.discount);
                if (total.compareTo(BigDecimal.ZERO) < 0) {
                    total = BigDecimal.ZERO;
                }
                String orderCode = generateOrderCode();

                long orderId = insertOrder(conn, userId, normalizedMethod, paymentDetails, notes, address, cartData, shipping, total, couponResult, orderCode, shopId);
                insertOrderItems(conn, orderId, cartData);
                // Update snapshots immediately after inserting order items
                String snapshotSql =
                    "UPDATE orders o " +
                    "SET items_snapshot = (" +
                    "    SELECT jsonb_agg(" +
                    "        jsonb_build_object(" +
                    "            'book_id', b.id, " +
                    "            'title', b.title, " +
                    "            'price', b.price, " +
                    "            'quantity', oi.quantity, " +
                    "            'subtotal', b.price * oi.quantity, " +
                    "            'shop_id', s.id, " +
                    "            'shop_name', s.name" +
                    "        )" +
                    "    ) " +
                    "    FROM order_items oi " +
                    "    JOIN books b ON b.id = oi.book_id " +
                    "    JOIN shops s ON s.id = b.shop_id " +
                    "    WHERE oi.order_id = o.id" +
                    "), " +
                    "receiver_snapshot = (" +
                    "    SELECT jsonb_build_object(" +
                    "        'name', COALESCE(ua.recipient_name, ua.receiver), " +
                    "        'phone', ua.phone, " +
                    "        'address', " +
                    "            TRIM(" +
                    "                COALESCE(ua.line1, '') || ' ' || " +
                    "                COALESCE(ua.line2, '') || ', ' || " +
                    "                COALESCE(ua.ward, '') || ', ' || " +
                    "                COALESCE(ua.district, '') || ', ' || " +
                    "                COALESCE(ua.city, '') || ', ' || " +
                    "                COALESCE(ua.province, '')" +
                    "            ), " +
                    "        'note', ua.note" +
                    "    ) " +
                    "    FROM user_addresses ua " +
                    "    WHERE ua.id = o.shipping_address_id" +
                    ") " +
                    "WHERE o.id = ?";

                try (PreparedStatement ps = conn.prepareStatement(snapshotSql)) {
                    ps.setLong(1, orderId);
                    ps.executeUpdate();
                }
                updateInventory(conn, cartData);
                recordStatus(conn, orderId, "new", "Đặt hàng thành công", String.valueOf(userId));
                createPaymentRecord(conn, orderId, normalizedMethod, total);
                if (couponResult.hasCoupon()) {
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

    private static BigDecimal resolveShippingFee(BigDecimal shippingFee, ShippingQuote shippingQuote) {
        BigDecimal fee = null;
        if (shippingQuote != null) {
            fee = shippingQuote.getFee();
        }
        if (fee == null) {
            fee = shippingFee;
        }
        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return fee;
    }

    public static List<Order> findOrders(long userId, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder();
    sql.append("SELECT id, code, user_id, shop_id, order_date, status, payment_status, payment_method, payment_provider, payment_metadata, shipping_snapshot, items_snapshot, items_subtotal, discount_amount, shipping_fee, total_amount, currency, coupon_code, notes, created_at, updated_at, receiver_snapshot, shop_snapshot "
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
                    order.setItems(parseItemsFromSnapshot(rs));
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
        StringBuilder sql = new StringBuilder("SELECT o.id, o.code, o.user_id, o.shop_id, o.order_date, o.status, o.payment_status, o.payment_method, o.payment_provider, o.payment_metadata, o.shipping_snapshot, o.receiver_snapshot, o.shop_snapshot, o.items_snapshot, o.items_subtotal, o.discount_amount, o.shipping_fee, o.total_amount, o.currency, o.coupon_code, o.notes, o.created_at, o.updated_at, "
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
                String snapshotRaw = rs.getString("items_snapshot");
                List<OrderItem> items = parseItemsFromSnapshot(rs);
                if ((snapshotRaw == null || snapshotRaw.trim().isEmpty()) && items.isEmpty()) {
                    // chỉ fallback nếu snapshot hoàn toàn không có
                    items = findOrderItems(conn, orderId, userId);
                }
                order.setItems(items);
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
        sql.append("SELECT o.id, o.code, o.status, o.payment_status, o.payment_method, o.total_amount, o.shipping_fee, o.order_date, o.updated_at, o.payment_metadata, ")
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



    // Trong file dao.OrderDAO.java

// ... (Sau các hàm listOrdersForSeller, fetchOrderById, v.v.)

/**
 * Lấy tổng doanh thu tháng này cho một Shop cụ thể.
 * (Cần triển khai logic SQL tính tổng từ bảng orders/order_items theo shop_id và tháng hiện tại)
 */
public static BigDecimal getMonthlyRevenue(int shopId) throws SQLException {
    // Đây là SQL giả định. Cần thay bằng logic tính tổng doanh thu theo shopId và tháng.
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE shop_id = ? AND status = 'delivered' AND order_date >= date_trunc('month', CURRENT_DATE)";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, shopId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        }
    }
}

/**
 * Đếm tổng số đơn hàng đã hoàn tất (hoặc tất cả) cho một Shop cụ thể.
 */
public static int countTotalOrders(int shopId) throws SQLException {
    // Chỉ đếm đơn đã confirmed/delivered để phản ánh đơn hàng thực tế
    String sql = "SELECT COUNT(*) FROM orders WHERE shop_id = ? AND status IN ('confirmed', 'shipping', 'delivered')";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, shopId);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
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
                String currentStatus;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT status FROM orders WHERE id = ? FOR UPDATE")) {
                    stmt.setLong(1, orderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            currentStatus = normalizeStatusValue(rs.getString(1));
                        } else {
                            throw new SQLException("Order not found: " + orderId);
                        }
                    }
                }

                if (normalizedStatus.equals(currentStatus)) {
                    conn.commit();
                    return;
                }

                if (!isTransitionAllowed(currentStatus, normalizedStatus)) {
                    throw new SQLException(String.format(Locale.US,
                            "Không thể chuyển đơn từ '%s' sang '%s'", currentStatus, normalizedStatus));
                }

                int updated;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                    stmt.setString(1, normalizedStatus);
                    stmt.setLong(2, orderId);
                    updated = stmt.executeUpdate();
                }
                if (updated == 0) {
                    throw new SQLException("Order not found: " + orderId);
                }

                recordStatus(conn, orderId, normalizedStatus, effectiveNote, createdBy);

                if ("confirmed".equals(normalizedStatus) || "shipping".equals(normalizedStatus)) {
                    new ShipmentDAO().ensureShipmentAssigned(conn, orderId);
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

    public static String getOrderStatus(long orderId) throws SQLException {
        String sql = "SELECT status FROM orders WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return normalizeStatusValue(rs.getString(1));
                }
            }
        }
        return null;
    }

    public static Order updateOrderDetails(long orderId, OrderUpdateCommand command) throws SQLException {
        if (command == null) {
            throw new SQLException("Không có dữ liệu để cập nhật");
        }

        List<String> assignments = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        List<Integer> sqlTypes = new ArrayList<>();
        Integer shippingSnapshotIndex = null;
        String normalizedShippingAddress = null;

        if (command.paymentMethodSet) {
            assignments.add("payment_method = ?");
            values.add(command.paymentMethod);
            sqlTypes.add(Types.VARCHAR);
        }
        if (command.paymentStatusSet) {
            assignments.add("payment_status = ?");
            values.add(command.paymentStatus);
            sqlTypes.add(Types.VARCHAR);
        }
        if (command.paymentProviderSet) {
            assignments.add("payment_provider = ?");
            values.add(command.paymentProvider);
            sqlTypes.add(Types.VARCHAR);
        }
        if (command.shippingAddressSet) {
            normalizedShippingAddress = normalizeShippingAddress(command.shippingAddress);
            shippingSnapshotIndex = values.size();
            assignments.add("shipping_snapshot = ?");
            values.add(normalizedShippingAddress);
            sqlTypes.add(Types.OTHER);
        }
        if (command.shippingFeeSet) {
            assignments.add("shipping_fee = ?");
            values.add(command.shippingFee);
            sqlTypes.add(Types.DECIMAL);
        }
        if (command.notesSet) {
            assignments.add("notes = ?");
            values.add(command.notes);
            sqlTypes.add(Types.VARCHAR);
        }
        if (command.couponCodeSet) {
            assignments.add("coupon_code = ?");
            values.add(command.couponCode);
            sqlTypes.add(Types.VARCHAR);
        }

        if (assignments.isEmpty()) {
            throw new SQLException("Không có dữ liệu nào để cập nhật");
        }

        try (Connection conn = DBUtil.getConnection()) {
            boolean hasSnapshotColumn = shippingSnapshotIndex != null && columnExists(conn, "orders", "shipping_snapshot");
            if (shippingSnapshotIndex != null) {
                if (hasSnapshotColumn) {
                    String formattedSnapshot = buildShippingSnapshotForUpdate(conn, orderId, normalizedShippingAddress);
                    values.set(shippingSnapshotIndex, formattedSnapshot);
                } else {
                    assignments.remove((int) shippingSnapshotIndex);
                    values.remove((int) shippingSnapshotIndex);
                    sqlTypes.remove((int) shippingSnapshotIndex);
                    shippingSnapshotIndex = null;
                    if (assignments.isEmpty()) {
                        throw new SQLException("Không có dữ liệu nào để cập nhật");
                    }
                }
            }

            boolean hasLegacyColumn = command.shippingAddressSet && columnExists(conn, "orders", "shipping_address");
            if (hasLegacyColumn) {
                assignments.add("shipping_address = ?");
                values.add(normalizedShippingAddress);
                sqlTypes.add(Types.VARCHAR);
            }

            StringBuilder sql = new StringBuilder("UPDATE orders SET ");
            sql.append(String.join(", ", assignments));
            sql.append(", updated_at = CURRENT_TIMESTAMP WHERE id = ?");

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int index = 1;
                for (int i = 0; i < values.size(); i++) {
                    Object value = values.get(i);
                    int type = sqlTypes.get(i);
                    if (value == null) {
                        stmt.setNull(index++, type);
                    } else if (type == Types.DECIMAL || type == Types.NUMERIC) {
                        stmt.setBigDecimal(index++, (BigDecimal) value);
                    } else if (type == Types.OTHER && value instanceof String) {
                        PGobject jsonObject = new PGobject();
                        jsonObject.setType("jsonb");
                        jsonObject.setValue((String) value);
                        stmt.setObject(index++, jsonObject);
                    } else {
                        stmt.setObject(index++, value);
                    }
                }
                stmt.setLong(index, orderId);
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Order not found: " + orderId);
                }
            }
        }

        return fetchOrderForAdmin(orderId);
    }

    public static final class OrderUpdateCommand {
        private boolean paymentMethodSet;
        private String paymentMethod;
        private boolean paymentStatusSet;
        private String paymentStatus;
        private boolean paymentProviderSet;
        private String paymentProvider;
        private boolean shippingAddressSet;
        private String shippingAddress;
        private boolean shippingFeeSet;
        private BigDecimal shippingFee;
        private boolean notesSet;
        private String notes;
        private boolean couponCodeSet;
        private String couponCode;

        public OrderUpdateCommand withPaymentMethod(String value) {
            this.paymentMethod = value;
            this.paymentMethodSet = true;
            return this;
        }

        public OrderUpdateCommand withPaymentStatus(String value) {
            this.paymentStatus = value;
            this.paymentStatusSet = true;
            return this;
        }

        public OrderUpdateCommand withPaymentProvider(String value) {
            this.paymentProvider = value;
            this.paymentProviderSet = true;
            return this;
        }

        public OrderUpdateCommand withShippingAddress(String value) {
            this.shippingAddress = value;
            this.shippingAddressSet = true;
            return this;
        }

        public OrderUpdateCommand withShippingFee(BigDecimal value) {
            this.shippingFee = value;
            this.shippingFeeSet = true;
            return this;
        }

        public OrderUpdateCommand withNotes(String value) {
            this.notes = value;
            this.notesSet = true;
            return this;
        }

        public OrderUpdateCommand withCouponCode(String value) {
            this.couponCode = value;
            this.couponCodeSet = true;
            return this;
        }
    }

    private static boolean isTransitionAllowed(String currentStatus, String targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        if (currentStatus == null || currentStatus.equals(targetStatus)) {
            return true;
        }
        Set<String> allowed = STATUS_TRANSITIONS.get(currentStatus);
        if (allowed == null || allowed.isEmpty()) {
            return false;
        }
        return allowed.contains(targetStatus);
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
        
        int shopId = rs.getInt("shop_id");
        if (!rs.wasNull()) {
            order.setShopId(shopId);
        }

        order.setOrderDate(toLocalDateTime(rs.getTimestamp("order_date")));
        order.setStatus(rs.getString("status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentProvider(rs.getString("payment_provider"));
        order.setPaymentMetadata(rs.getString("payment_metadata"));
        order.setItemsSubtotal(rs.getBigDecimal("items_subtotal"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setShippingFee(rs.getBigDecimal("shipping_fee"));
        order.setShippingAddress(resolveShippingAddress(rs));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCurrency(rs.getString("currency"));
        order.setCouponCode(rs.getString("coupon_code"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        order.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));

        // ✅ Thêm phần đọc snapshot JSON
        Gson gson = new Gson();

        // 1️⃣ Items snapshot (sản phẩm)
        String itemsSnapshotJson = rs.getString("items_snapshot");
        if (itemsSnapshotJson != null && !itemsSnapshotJson.isEmpty()) {
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> itemsSnapshot = gson.fromJson(itemsSnapshotJson, listType);
            order.setItemsSnapshot(itemsSnapshot);
        }

        // 2️⃣ Receiver snapshot (thông tin người nhận)
        String receiverSnapshotJson = rs.getString("receiver_snapshot");
        if (receiverSnapshotJson != null && !receiverSnapshotJson.isEmpty()) {
            Map<String, Object> receiverSnapshot = gson.fromJson(receiverSnapshotJson, Map.class);
            order.setReceiverSnapshot(receiverSnapshot);
        }

        // 3️⃣ Shop snapshot (nếu có)
        String shopSnapshotJson = rs.getString("shop_snapshot");
        if (shopSnapshotJson != null && !shopSnapshotJson.isEmpty()) {
            Map<String, Object> shopSnapshot = gson.fromJson(shopSnapshotJson, Map.class);
            order.setShopSnapshot(shopSnapshot);
        }

        return order;
    }

    private static String normalizeShippingAddress(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 1000) {
            return trimmed.substring(0, 1000);
        }
        return trimmed;
    }

    private static String resolveShippingAddress(ResultSet rs) throws SQLException {
        String direct = getStringIfPresent(rs, "shipping_address");
        if (hasText(direct)) {
            return direct.trim();
        }
        String snapshot = getStringIfPresent(rs, "shipping_snapshot");
        if (!hasText(snapshot)) {
            return null;
        }
        try {
            JsonObject obj = GSON.fromJson(snapshot, JsonObject.class);
            if (obj == null) {
                return snapshot;
            }
            String formatted = chooseFirst(obj, "formatted");
            if (hasText(formatted)) {
                return formatted.trim();
            }
            List<String> lines = new ArrayList<>();
            String recipient = chooseFirst(obj, "recipientName", "recipient_name");
            String phone = chooseFirst(obj, "phone");
            String header = joinWithSeparator(" - ", recipient, phone);
            if (hasText(header)) {
                lines.add(header);
            }

            List<String> addressParts = new ArrayList<>();
            addIfHasText(addressParts, chooseFirst(obj, "line1", "addressLine1"));
            addIfHasText(addressParts, chooseFirst(obj, "line2", "addressLine2"));
            addIfHasText(addressParts, chooseFirst(obj, "ward", "commune"));
            addIfHasText(addressParts, chooseFirst(obj, "district"));
            addIfHasText(addressParts, chooseFirst(obj, "city"));
            addIfHasText(addressParts, chooseFirst(obj, "province", "state"));
            addIfHasText(addressParts, chooseFirst(obj, "postalCode", "postal_code"));
            addIfHasText(addressParts, chooseFirst(obj, "country"));
            if (!addressParts.isEmpty()) {
                lines.add(String.join(", ", addressParts));
            }

            String note = chooseFirst(obj, "note");
            if (hasText(note)) {
                lines.add("Ghi chú: " + note.trim());
            }

            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                if (!hasText(line)) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(line.trim());
            }
            if (builder.length() > 0) {
                return builder.toString();
            }
        } catch (Exception ignore) {
            // fall back to raw snapshot string
        }
        return snapshot;
    }

    private static String getStringIfPresent(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException ex) {
            String state = ex.getSQLState();
            if ("42703".equals(state) || (state != null && state.equalsIgnoreCase("S0022"))) {
                return null;
            }
            String message = ex.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(Locale.US);
                if (lower.contains("does not exist") || lower.contains("not found")) {
                    return null;
                }
            }
            return null;
        }
    }

    private static String chooseFirst(JsonObject obj, String... keys) {
        if (obj == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null || key.isEmpty() || !obj.has(key)) {
                continue;
            }
            JsonElement element = obj.get(key);
            if (element == null || element.isJsonNull()) {
                continue;
            }
            try {
                String value = element.getAsString();
                if (hasText(value)) {
                    return value.trim();
                }
            } catch (ClassCastException | IllegalStateException ignore) {
                // ignore non-string values
            }
        }
        return null;
    }

    private static void addIfHasText(List<String> target, String value) {
        if (target == null) {
            return;
        }
        if (hasText(value)) {
            target.add(value.trim());
        }
    }

    private static String joinWithSeparator(String separator, String... parts) {
        if (parts == null || parts.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!hasText(part)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(part.trim());
        }
        return sb.toString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String buildShippingSnapshotForUpdate(Connection conn, long orderId, String shippingAddress) throws SQLException {
        String existing = fetchShippingSnapshot(conn, orderId);
        JsonObject snapshot;
        if (hasText(existing)) {
            try {
                snapshot = GSON.fromJson(existing, JsonObject.class);
            } catch (Exception ex) {
                snapshot = new JsonObject();
            }
        } else {
            snapshot = new JsonObject();
        }

        String trimmedAddress = hasText(shippingAddress) ? shippingAddress.trim() : null;
        if (trimmedAddress == null) {
            snapshot.remove("formatted");
            snapshot.remove("updatedByAdmin");
            snapshot.remove("updatedByAdminAt");
        } else {
            snapshot.addProperty("formatted", trimmedAddress);
            snapshot.addProperty("updatedByAdmin", true);
            snapshot.addProperty("updatedByAdminAt", LocalDateTime.now().toString());
        }

        if (snapshot.entrySet().isEmpty()) {
            return null;
        }
        return GSON.toJson(snapshot);
    }

    private static String fetchShippingSnapshot(Connection conn, long orderId) throws SQLException {
        String sql = "SELECT shipping_snapshot FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private static List<OrderItem> findOrderItems(Connection conn, long orderId, Long userId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT oi.id, oi.order_id, oi.book_id, oi.quantity, oi.unit_price, oi.total_price, ");
        sql.append("oi.shop_id AS oi_shop_id, oi.shop_name AS oi_shop_name, ");
        sql.append("b.title, b.author, b.image_url, b.shop_id AS b_shop_id, b.shop_name AS b_shop_name");
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
                    
                    // Ưu tiên lấy shop info từ order_items (snapshot), nếu không có thì lấy từ books
                    Integer shopId = null;
                    String shopName = null;
                    
                    // Thử lấy từ order_items trước
                    int oiShopId = rs.getInt("oi_shop_id");
                    if (!rs.wasNull()) {
                        shopId = oiShopId;
                    }
                    shopName = rs.getString("oi_shop_name");
                    
                    // Nếu order_items không có, fallback sang books
                    if (shopId == null) {
                        int bShopId = rs.getInt("b_shop_id");
                        if (!rs.wasNull()) {
                            shopId = bShopId;
                        }
                    }
                    if (shopName == null || shopName.trim().isEmpty()) {
                        shopName = rs.getString("b_shop_name");
                    }
                    
                    item.setShopId(shopId);
                    item.setShopName(shopName);
                    
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

    private static List<OrderItem> parseItemsFromSnapshot(ResultSet rs) throws SQLException {
        String snapshot = rs.getString("items_snapshot");
        if (snapshot == null || snapshot.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<OrderItem> items = new ArrayList<>();

            // 1️⃣ Trường hợp A: snapshot là một JSON Array trực tiếp
            if (snapshot.trim().startsWith("[")) {
                JsonArray array = GSON.fromJson(snapshot, JsonArray.class);
                for (JsonElement elem : array) {
                    JsonObject itemObj = elem.getAsJsonObject();
                    items.add(parseItemFromJson(itemObj));
                }
                return items;
            }

            // 2️⃣ Trường hợp B: snapshot là object có "items": [...]
            JsonObject obj = GSON.fromJson(snapshot, JsonObject.class);
            if (obj != null && obj.has("items")) {
                JsonArray itemsArray = obj.getAsJsonArray("items");
                for (JsonElement elem : itemsArray) {
                    JsonObject itemObj = elem.getAsJsonObject();
                    items.add(parseItemFromJson(itemObj));
                }
            }

            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Helper nhỏ gọn để parse từng item
    private static OrderItem parseItemFromJson(JsonObject itemObj) {
        OrderItem item = new OrderItem();
        if (itemObj.has("book_id")) item.setBookId(itemObj.get("book_id").getAsLong());
        if (itemObj.has("bookId")) item.setBookId(itemObj.get("bookId").getAsLong());
        if (itemObj.has("quantity")) item.setQuantity(itemObj.get("quantity").getAsInt());
        if (itemObj.has("price")) {
            try {
                item.setUnitPrice(itemObj.get("price").getAsBigDecimal());
            } catch (Exception e) {
                item.setUnitPrice(BigDecimal.valueOf(itemObj.get("price").getAsDouble()));
            }
        }
        if (itemObj.has("unitPrice")) {
            try {
                item.setUnitPrice(itemObj.get("unitPrice").getAsBigDecimal());
            } catch (Exception e) {
                item.setUnitPrice(BigDecimal.valueOf(itemObj.get("unitPrice").getAsDouble()));
            }
        }
        if (itemObj.has("subtotal")) {
            try {
                item.setTotalPrice(itemObj.get("subtotal").getAsBigDecimal());
            } catch (Exception e) {
                item.setTotalPrice(BigDecimal.valueOf(itemObj.get("subtotal").getAsDouble()));
            }
        }
        if (itemObj.has("title")) item.setTitle(itemObj.get("title").getAsString());
        if (itemObj.has("author")) item.setAuthor(itemObj.get("author").getAsString());
        if (itemObj.has("imageUrl")) item.setImageUrl(itemObj.get("imageUrl").getAsString());
        if (itemObj.has("shop_id")) item.setShopId(itemObj.get("shop_id").getAsInt());
        if (itemObj.has("shopId")) item.setShopId(itemObj.get("shopId").getAsInt());
        if (itemObj.has("shop_name")) item.setShopName(itemObj.get("shop_name").getAsString());
        if (itemObj.has("shopName")) item.setShopName(itemObj.get("shopName").getAsString());
        item.setReviewId(null);
        item.setHasReview(false);
        return item;
    }

    private static void insertOrderItems(Connection conn, long orderId, CartData cartData) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, book_id, quantity, unit_price, total_price, shop_id, shop_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartLine item : cartData.items) {
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.bookId);
                stmt.setInt(3, item.quantity);
                stmt.setBigDecimal(4, item.unitPrice);
                stmt.setBigDecimal(5, item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)));
                
                // Lưu shop info snapshot
                if (item.shopId != null) {
                    stmt.setInt(6, item.shopId);
                } else {
                    stmt.setNull(6, java.sql.Types.INTEGER);
                }
                stmt.setString(7, item.shopName);
                
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
            if (coupon.snapshotJson != null) {
                stmt.setString(5, coupon.snapshotJson);
            } else {
                stmt.setNull(5, java.sql.Types.OTHER);
            }
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
        if (coupon.shopCouponId != null) {
            ShopCouponDAO.incrementUsage(conn, coupon.shopCouponId.intValue());
        }
    }

    private static long insertOrder(Connection conn, long userId, String paymentMethod, PaymentDetails paymentDetails, String notes, AddressSnapshot address, CartData cartData,
                                    BigDecimal shippingFee, BigDecimal total, CouponResult coupon, String orderCode, Integer shopId) throws SQLException {
        // Build immutable snapshots
        String receiverSnapshot = buildReceiverSnapshot(conn, userId, address.addressId);
        String shopSnapshot = shopId != null ? buildShopSnapshot(conn, shopId) : null;
        String itemsSnapshot = buildItemsSnapshot(conn, cartData);

        String sql = "INSERT INTO orders (user_id, shop_id, code, status, payment_status, payment_method, payment_provider, shipping_address_id, shipping_snapshot, cart_snapshot, payment_metadata, items_subtotal, discount_amount, shipping_fee, total_amount, currency, coupon_code, coupon_snapshot, receiver_snapshot, shop_snapshot, items_snapshot, notes) "
                + "VALUES (?, ?, ?, 'new', 'unpaid', ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?, ?, 'VND', ?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            // Thêm shop_id
            if (shopId != null) {
                stmt.setInt(2, shopId);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            stmt.setString(3, orderCode);
            stmt.setString(4, paymentMethod);
            stmt.setString(5, providerFor(paymentMethod));
            if (address.addressId == null) {
                stmt.setNull(6, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(6, address.addressId);
            }
            stmt.setString(7, address.toJson());
            stmt.setString(8, cartData.snapshotJson);
            if (paymentDetails != null && paymentDetails.hasMetadata()) {
                stmt.setString(9, paymentDetails.toJson());
            } else {
                stmt.setNull(9, java.sql.Types.OTHER);
            }
            stmt.setBigDecimal(10, cartData.subtotal);
            stmt.setBigDecimal(11, coupon.discount);
            stmt.setBigDecimal(12, shippingFee);
            stmt.setBigDecimal(13, total);
            stmt.setString(14, coupon.code);
            stmt.setString(15, coupon.snapshotJson);
            stmt.setString(16, receiverSnapshot);
            stmt.setString(17, shopSnapshot);
            stmt.setString(18, itemsSnapshot);
            stmt.setString(19, notes);
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
        StringBuilder sql = new StringBuilder("SELECT id, title, author, image_url, price, shop_id, shop_name");
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
                    
                    // Lấy thông tin shop
                    int shopIdValue = rs.getInt("shop_id");
                    if (!rs.wasNull()) {
                        item.shopId = shopIdValue;
                    }
                    item.shopName = rs.getString("shop_name");
                    
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
        StringBuilder sql = new StringBuilder("SELECT c.id AS cart_id, ci.book_id, ci.quantity, ci.unit_price, b.title, b.author, b.image_url, b.stock_quantity, b.shop_id, b.shop_name "
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
                    // Lấy thông tin shop
                    int shopIdValue = rs.getInt("shop_id");
                    if (!rs.wasNull()) {
                        item.shopId = shopIdValue;
                    }
                    item.shopName = rs.getString("shop_name");
                    
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
                snapshot.json = json;
                return snapshot;
            }
        }
    }

    private static CouponResult applyCoupon(Connection conn, long userId, String code, BigDecimal subtotal, Integer shopId) throws SQLException {
        CouponResult platformCoupon = tryLoadPlatformCoupon(conn, userId, code, subtotal);
        if (platformCoupon != null) {
            return platformCoupon;
        }
        if (shopId != null && shopId > 0) {
            CouponResult shopCoupon = tryLoadShopCoupon(conn, userId, shopId, code, subtotal);
            if (shopCoupon != null) {
                return shopCoupon;
            }
        }
        throw new SQLException("Mã giảm giá không áp dụng được cho đơn hàng này");
    }

    private static CouponResult tryLoadPlatformCoupon(Connection conn, long userId, String code, BigDecimal subtotal) throws SQLException {
        String sql = "SELECT id, coupon_type, value, max_discount, minimum_order, usage_limit, per_user_limit, start_date, end_date, status, description FROM coupon_codes WHERE code = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
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

    private static CouponResult tryLoadShopCoupon(Connection conn, long userId, int shopId, String code, BigDecimal subtotal) throws SQLException {
        ShopCoupon shopCoupon = ShopCouponDAO.findActiveForShop(conn, shopId, code, true);
        if (shopCoupon == null) {
            return null;
        }
        CouponResult result = new CouponResult();
        result.shopCoupon = true;
        result.shopCouponId = shopCoupon.getId();
        result.shopId = shopCoupon.getShopId();
        result.shopName = shopCoupon.getShopName();
        result.code = code;
        result.userId = userId;
        result.type = shopCoupon.getDiscountType();
        result.value = shopCoupon.getDiscountValue();
        result.minimumOrder = shopCoupon.getMinimumOrder();
        result.usageLimit = shopCoupon.getUsageLimit();
        result.usedCount = shopCoupon.getUsedCount();
        result.status = shopCoupon.getStatus();
        result.startDate = shopCoupon.getStartDate();
        result.endDate = shopCoupon.getEndDate();
        validateCoupon(conn, result, subtotal);
        result.discount = calculateDiscount(result, subtotal);
        JsonObject snapshot = new JsonObject();
        snapshot.addProperty("type", result.type);
        snapshot.addProperty("value", result.value);
        snapshot.addProperty("discount", result.discount);
        if (shopCoupon.getDescription() != null) {
            snapshot.addProperty("description", shopCoupon.getDescription());
        }
        snapshot.addProperty("shopId", shopCoupon.getShopId());
        if (shopCoupon.getShopName() != null) {
            snapshot.addProperty("shopName", shopCoupon.getShopName());
        }
        result.snapshotJson = GSON.toJson(snapshot);
        return result;
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
            if (coupon.shopCoupon) {
                int used = coupon.usedCount != null ? coupon.usedCount : 0;
                if (used >= coupon.usageLimit) {
                    throw new SQLException("Mã giảm giá của shop đã hết lượt sử dụng");
                }
            } else if (coupon.couponId != null) {
                int used = countUsage(conn, coupon.couponId, null);
                if (used >= coupon.usageLimit) {
                    throw new SQLException("Mã giảm giá đã đạt số lần sử dụng tối đa");
                }
            }
        }
        if (!coupon.shopCoupon && coupon.perUserLimit != null && coupon.couponId != null) {
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
        if ("percent".equalsIgnoreCase(coupon.type) || "percentage".equalsIgnoreCase(coupon.type)) {
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
            node.addProperty("subtotal", item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)));
            node.addProperty("title", item.title);
            node.addProperty("author", item.author);
            node.addProperty("imageUrl", item.imageUrl);
            node.addProperty("shopId", item.shopId);
            node.addProperty("shopName", item.shopName);
            itemsJson.add(node);
        }
        JsonObject snapshot = new JsonObject();
        snapshot.add("items", itemsJson);
        snapshot.addProperty("subtotal", cartData.subtotal);
        return GSON.toJson(snapshot);
    }

    private static String buildReceiverSnapshot(Connection conn, long userId, Long addressId) throws SQLException {
        if (addressId == null) {
            return null;
        }
        String sql = "SELECT id, recipient_name, phone, line1, line2, ward, district, city, province, postal_code, country, note FROM user_addresses WHERE user_id = ? AND id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, addressId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
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
                return GSON.toJson(json);
            }
        }
    }

    private static String buildShopSnapshot(Connection conn, int shopId) throws SQLException {
        String sql = "SELECT id, name, description, owner_id, created_at FROM shops WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                JsonObject json = new JsonObject();
                json.addProperty("id", rs.getInt("id"));
                json.addProperty("name", rs.getString("name"));
                json.addProperty("description", rs.getString("description"));
                json.addProperty("ownerId", rs.getLong("owner_id"));
                json.addProperty("createdAt", rs.getTimestamp("created_at").toString());
                return GSON.toJson(json);
            }
        }
    }

    private static String buildItemsSnapshot(Connection conn, CartData cartData) throws SQLException {
        return buildCartSnapshot(cartData);
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
        private Integer shopId;
        private String shopName;
    }

    private static Integer resolvePrimaryShopId(CartData cartData) {
        if (cartData == null || cartData.items.isEmpty()) {
            return null;
        }
        Integer shopId = null;
        for (CartLine line : cartData.items) {
            if (line.shopId == null) {
                continue;
            }
            if (shopId == null) {
                shopId = line.shopId;
            } else if (!shopId.equals(line.shopId)) {
                return null;
            }
        }
        return shopId;
    }

    private static class AddressSnapshot {
        private Long addressId;
        private JsonObject json;

        private String toJson() {
            return json == null ? null : GSON.toJson(json);
        }

        private void attachShipping(ShippingQuote quote) {
            if (quote == null) {
                return;
            }
            ensureJson();
            JsonObject method = new JsonObject();
            if (quote.getShipperId() != null) {
                method.addProperty("id", quote.getShipperId());
                json.addProperty("shipperId", quote.getShipperId());
            }
            if (quote.getShipperName() != null && !quote.getShipperName().isBlank()) {
                method.addProperty("name", quote.getShipperName());
                json.addProperty("shipperName", quote.getShipperName());
            }
            if (quote.getFee() != null) {
                method.addProperty("fee", quote.getFee());
                json.addProperty("shippingFee", quote.getFee());
            }
            if (quote.getEstimatedTime() != null && !quote.getEstimatedTime().isBlank()) {
                method.addProperty("estimatedTime", quote.getEstimatedTime());
            }
            if (quote.getServiceArea() != null && !quote.getServiceArea().isBlank()) {
                method.addProperty("serviceArea", quote.getServiceArea());
            }
            if (quote.getMatchLevel() != null) {
                method.addProperty("matchLevel", quote.getMatchLevel().name());
            }
            json.add("shippingMethod", method);
        }

        private void attachShippingFee(BigDecimal fee) {
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            ensureJson();
            json.addProperty("shippingFee", fee);
        }

        private void ensureJson() {
            if (json == null) {
                json = new JsonObject();
            }
        }
    }

    public static final class PaymentDetails {
        private final String method;
        private final String maskedPan;
        private final String last4;
        private final String expiryMonth;
        private final String expiryYear;

        private PaymentDetails(String method, String maskedPan, String last4, String expiryMonth, String expiryYear) {
            this.method = method;
            this.maskedPan = maskedPan;
            this.last4 = last4;
            this.expiryMonth = expiryMonth;
            this.expiryYear = expiryYear;
        }

        public static PaymentDetails wallet(String method, String cardNumber, String expiryMonth, String expiryYear) {
            String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
            String normalizedMethod = method == null ? null : method.trim().toLowerCase(Locale.US);
            String last4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : (digits.isEmpty() ? null : digits);
            String masked = digits.isEmpty() ? null : maskDigits(digits);
            return new PaymentDetails(normalizedMethod, masked, last4, sanitize(expiryMonth), sanitize(expiryYear));
        }

        private static String maskDigits(String digits) {
            int length = digits.length();
            if (length <= 4) {
                return digits;
            }
            StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length - 4; i++) {
                builder.append('*');
            }
            builder.append(digits.substring(length - 4));
            return builder.toString();
        }

        private static String sanitize(String value) {
            return hasText(value) ? value.trim() : null;
        }

        public boolean hasMetadata() {
            return hasText(maskedPan) || hasText(last4) || hasText(expiryMonth) || hasText(expiryYear);
        }

        public String toJson() {
            JsonObject json = new JsonObject();
            if (hasText(method)) {
                json.addProperty("method", method);
            }
            if (hasText(maskedPan)) {
                json.addProperty("cardMasked", maskedPan);
            }
            if (hasText(last4)) {
                json.addProperty("cardLast4", last4);
            }
            if (hasText(expiryMonth)) {
                json.addProperty("expiryMonth", expiryMonth);
            }
            if (hasText(expiryYear)) {
                json.addProperty("expiryYear", expiryYear);
            }
            return json.size() == 0 ? null : json.toString();
        }
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
        private Integer shopCouponId;
        private Integer shopId;
        private Integer usedCount;
        private boolean shopCoupon;
        private String shopName;

        private boolean hasCoupon() {
            if (couponId != null) {
                return true;
            }
            if (shopCouponId != null) {
                return true;
            }
            return code != null && !code.trim().isEmpty();
        }

        private static CouponResult empty() {
            CouponResult result = new CouponResult();
            result.couponId = null;
            result.code = null;
            result.discount = BigDecimal.ZERO;
            result.snapshotJson = null;
            result.shopCouponId = null;
            result.shopId = null;
            result.usedCount = null;
            result.shopCoupon = false;
            result.shopName = null;
            return result;
        }
    }

    /**
     * Lấy danh sách đơn hàng theo Shop ID
     */
    public static List<AdminOrderSummary> listOrdersForShop(int shopId, String statusFilter, String keyword, int limit) throws SQLException {
        String normalizedStatus = normalizeStatusValue(statusFilter);
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.id, o.code, o.status, o.payment_status, o.payment_method, o.total_amount, o.shipping_fee, o.order_date, o.updated_at, ")
                .append("u.email, COALESCE(NULLIF(u.full_name, ''), NULLIF(u.username, ''), u.email) AS customer_name ")
                .append("FROM orders o LEFT JOIN users u ON u.id = o.user_id WHERE o.shop_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(shopId);
        
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

    /**
     * Đếm số đơn hàng theo trạng thái của Shop
     */
    public static int countOrdersByStatus(int shopId, String status) throws SQLException {
        String normalizedStatus = normalizeStatusValue(status);
        if (normalizedStatus == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM orders WHERE shop_id = ? AND LOWER(status) = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            stmt.setString(2, normalizedStatus);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Kiểm tra đơn hàng có thuộc về Shop không
     */
    public static boolean orderBelongsToShop(long orderId, int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE id = ? AND shop_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setInt(2, shopId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Lấy dữ liệu doanh thu 7 ngày gần nhất
     */
    public static List<Map<String, Object>> getDailySalesLast7Days(int shopId) throws SQLException {
        String sql = "SELECT d.date::text as date, COALESCE(s.revenue, 0) as revenue " +
                     "FROM (SELECT CURRENT_DATE - INTERVAL '6 days' + INTERVAL '1 day' * generate_series(0, 6) as date) d " +
                     "LEFT JOIN (SELECT DATE(order_date) as sale_date, SUM(total_amount) as revenue " +
                     "           FROM orders WHERE shop_id = ? AND status = 'delivered' " +
                     "           AND order_date >= CURRENT_DATE - INTERVAL '6 days' " +
                     "           GROUP BY DATE(order_date)) s ON d.date = s.sale_date " +
                     "ORDER BY d.date";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dailyData = new HashMap<>();
                    dailyData.put("date", rs.getString("date"));
                    dailyData.put("revenue", rs.getBigDecimal("revenue"));
                    result.add(dailyData);
                }
            }
        }
        return result;
    }
}
