package dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.CartItem;
import models.Order;
import models.OrderItem;
import models.OrderStatusEntry;
import models.PaymentRecord;
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
import java.util.Optional;

public final class OrderDAO {
    private static final Gson GSON = new Gson();

    private OrderDAO() {
    }

    public static class CheckoutRequest {
        public String fullName;
        public String email;
        public String phone;
        public String address;
        public String city;
        public String postalCode;
        public String country;
        public String notes;
        public String paymentMethod;
        public String paymentProvider;
        public String paymentReference;
        public Double shippingFee;
        public Double taxAmount;
        public Double discountAmount;
        public String customerMessage;
        public String currency;
    }

    public static Order checkoutCart(long userId, String sessionId, CheckoutRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Checkout data is required");
        }
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                Long cartId = findActiveCartId(conn, userId, sessionId);
                if (cartId == null) {
                    throw new SQLException("Không tìm thấy giỏ hàng để thanh toán");
                }

                CartSnapshot snapshot = loadCartSnapshot(conn, cartId);
                if (snapshot.items.isEmpty()) {
                    throw new SQLException("Giỏ hàng trống, không thể tạo đơn hàng");
                }

                BigDecimal shippingFee = toAmount(request.shippingFee);
                BigDecimal taxAmount = toAmount(request.taxAmount);
                BigDecimal discountAmount = toAmount(request.discountAmount);
                BigDecimal subtotal = snapshot.subtotal;
                BigDecimal total = subtotal.add(shippingFee).add(taxAmount).subtract(discountAmount);
                if (total.compareTo(BigDecimal.ZERO) < 0) {
                    total = BigDecimal.ZERO;
                }

                String currency = request.currency != null && !request.currency.trim().isEmpty()
                        ? request.currency.trim().toUpperCase()
                        : snapshot.currency;

                String cartSnapshotJson = buildCartSnapshotJson(snapshot, shippingFee, taxAmount, discountAmount, total, currency);

                long orderId = insertOrder(conn, userId, request, subtotal, shippingFee, taxAmount,
                        discountAmount, total, currency, cartSnapshotJson);

                String orderNumber = generateOrderNumber(orderId);
                updateOrderNumber(conn, orderId, orderNumber);

                insertOrderItems(conn, orderId, snapshot.items);

                insertOrderStatusHistory(conn, orderId, "pending", "Đơn hàng được tạo", "system");

                Optional<PaymentRecord> paymentRecord = insertPaymentRecord(conn, orderId, request, total, currency);

                markCartCheckedOut(conn, cartId);

                conn.commit();

                List<OrderItem> orderItems = loadOrderItems(conn, orderId);
                List<OrderStatusEntry> history = loadOrderStatusHistory(conn, orderId);
                List<PaymentRecord> payments;
                if (paymentRecord.isPresent()) {
                    payments = new ArrayList<>();
                    payments.add(paymentRecord.get());
                } else {
                    payments = loadPayments(conn, orderId);
                }

                return mapOrder(conn, orderId, orderNumber, subtotal, shippingFee, taxAmount, discountAmount, total,
                        currency, request, orderItems, payments, history);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static List<Order> getOrdersForUser(long userId, int limit, int offset) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, order_number, subtotal_amount, tax_amount, shipping_fee, discount_amount, total_amount, " +
                "currency, status, payment_status, payment_method, payment_reference, shipping_full_name, shipping_phone, " +
                "shipping_email, shipping_address, shipping_city, shipping_postal_code, shipping_country, shipping_notes, " +
                "customer_message, notes, created_at, updated_at FROM orders WHERE user_id = ? ORDER BY created_at DESC, id DESC " +
                "LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, Math.max(1, limit));
            stmt.setInt(3, Math.max(0, offset));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("id");
                    List<OrderItem> items = loadOrderItems(conn, orderId);
                    List<PaymentRecord> payments = loadPayments(conn, orderId);
                    List<OrderStatusEntry> history = loadOrderStatusHistory(conn, orderId);
                    orders.add(mapOrder(rs, orderId, items, payments, history));
                }
            }
        }
        return orders;
    }

    public static Order getOrderDetail(long userId, long orderId) throws SQLException {
        String sql = "SELECT id, order_number, subtotal_amount, tax_amount, shipping_fee, discount_amount, total_amount, " +
                "currency, status, payment_status, payment_method, payment_reference, shipping_full_name, shipping_phone, " +
                "shipping_email, shipping_address, shipping_city, shipping_postal_code, shipping_country, shipping_notes, " +
                "customer_message, notes, created_at, updated_at FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                List<OrderItem> items = loadOrderItems(conn, orderId);
                List<PaymentRecord> payments = loadPayments(conn, orderId);
                List<OrderStatusEntry> history = loadOrderStatusHistory(conn, orderId);
                return mapOrder(rs, orderId, items, payments, history);
            }
        }
    }

    private static Order mapOrder(ResultSet rs,
                                  long orderId,
                                  List<OrderItem> items,
                                  List<PaymentRecord> payments,
                                  List<OrderStatusEntry> history) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
        LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
        return new Order(
                orderId,
                null,
                rs.getString("order_number"),
                rs.getString("status"),
                rs.getString("payment_status"),
                rs.getString("payment_method"),
                rs.getString("payment_reference"),
                rs.getBigDecimal("subtotal_amount"),
                rs.getBigDecimal("tax_amount"),
                rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("total_amount"),
                rs.getString("currency"),
                rs.getString("shipping_full_name"),
                rs.getString("shipping_phone"),
                rs.getString("shipping_email"),
                rs.getString("shipping_address"),
                rs.getString("shipping_city"),
                rs.getString("shipping_postal_code"),
                rs.getString("shipping_country"),
                rs.getString("shipping_notes"),
                rs.getString("customer_message"),
                rs.getString("notes"),
                createdAt,
                updatedAt,
                items,
                payments,
                history
        );
    }

    private static Order mapOrder(Connection conn,
                                  long orderId,
                                  String orderNumber,
                                  BigDecimal subtotal,
                                  BigDecimal shippingFee,
                                  BigDecimal taxAmount,
                                  BigDecimal discountAmount,
                                  BigDecimal total,
                                  String currency,
                                  CheckoutRequest request,
                                  List<OrderItem> items,
                                  List<PaymentRecord> payments,
                                  List<OrderStatusEntry> history) throws SQLException {
        String sql = "SELECT status, payment_status, payment_reference, notes, created_at, updated_at FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy đơn hàng mới tạo");
                }
                Timestamp createdTs = rs.getTimestamp("created_at");
                Timestamp updatedTs = rs.getTimestamp("updated_at");
                return new Order(
                        orderId,
                        null,
                        orderNumber,
                        rs.getString("status"),
                        rs.getString("payment_status"),
                        request.paymentMethod,
                        rs.getString("payment_reference"),
                        subtotal,
                        taxAmount,
                        shippingFee,
                        discountAmount,
                        total,
                        currency,
                        request.fullName,
                        request.phone,
                        request.email,
                        request.address,
                        request.city,
                        request.postalCode,
                        request.country,
                        request.notes,
                        request.customerMessage,
                        rs.getString("notes"),
                        createdTs != null ? createdTs.toLocalDateTime() : null,
                        updatedTs != null ? updatedTs.toLocalDateTime() : null,
                        items,
                        payments,
                        history
                );
            }
        }
    }

    private static Optional<PaymentRecord> insertPaymentRecord(Connection conn,
                                                               long orderId,
                                                               CheckoutRequest request,
                                                               BigDecimal total,
                                                               String currency) throws SQLException {
        String method = request.paymentMethod != null ? request.paymentMethod.trim() : null;
        if (method == null || method.isEmpty()) {
            return Optional.empty();
        }
        String provider = request.paymentProvider != null && !request.paymentProvider.trim().isEmpty()
                ? request.paymentProvider.trim()
                : "manual";
        String sql = "INSERT INTO payments (order_id, provider, method, status, amount, currency, transaction_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, provider);
            stmt.setString(3, method);
            stmt.setString(4, "pending");
            stmt.setBigDecimal(5, total);
            stmt.setString(6, currency);
            stmt.setString(7, request.paymentReference);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    return Optional.of(new PaymentRecord(
                            rs.getLong("id"),
                            orderId,
                            provider,
                            method,
                            "pending",
                            total,
                            currency,
                            request.paymentReference,
                            createdTs != null ? createdTs.toLocalDateTime() : null,
                            updatedTs != null ? updatedTs.toLocalDateTime() : null
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private static void insertOrderStatusHistory(Connection conn,
                                                  long orderId,
                                                  String status,
                                                  String note,
                                                  String createdBy) throws SQLException {
        String sql = "INSERT INTO order_status_history (order_id, status, note, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, status);
            stmt.setString(3, note);
            stmt.setString(4, createdBy);
            stmt.executeUpdate();
        }
    }

    private static void insertOrderItems(Connection conn, long orderId, List<CartItem> cartItems) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, book_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : cartItems) {
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.getBookId());
                stmt.setInt(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPrice());
                stmt.setBigDecimal(5, item.getLineTotal());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void markCartCheckedOut(Connection conn, long cartId) throws SQLException {
        try (PreparedStatement deleteItems = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
            deleteItems.setLong(1, cartId);
            deleteItems.executeUpdate();
        }
        try (PreparedStatement updateCart = conn.prepareStatement(
                "UPDATE carts SET status = 'checked_out', updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            updateCart.setLong(1, cartId);
            updateCart.executeUpdate();
        }
    }

    private static long insertOrder(Connection conn,
                                     long userId,
                                     CheckoutRequest request,
                                     BigDecimal subtotal,
                                     BigDecimal shippingFee,
                                     BigDecimal taxAmount,
                                     BigDecimal discountAmount,
                                     BigDecimal total,
                                     String currency,
                                     String cartSnapshotJson) throws SQLException {
        String sql = "INSERT INTO orders (user_id, subtotal_amount, tax_amount, shipping_fee, discount_amount, total_amount, " +
                "currency, status, payment_status, payment_method, payment_reference, shipping_full_name, shipping_phone, " +
                "shipping_email, shipping_address, shipping_city, shipping_postal_code, shipping_country, shipping_notes, " +
                "customer_message, cart_snapshot) VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', 'pending', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setBigDecimal(2, subtotal);
            stmt.setBigDecimal(3, taxAmount);
            stmt.setBigDecimal(4, shippingFee);
            stmt.setBigDecimal(5, discountAmount);
            stmt.setBigDecimal(6, total);
            stmt.setString(7, currency);
            stmt.setString(8, request.paymentMethod);
            stmt.setString(9, request.paymentReference);
            stmt.setString(10, request.fullName);
            stmt.setString(11, request.phone);
            stmt.setString(12, request.email);
            stmt.setString(13, request.address);
            stmt.setString(14, request.city);
            stmt.setString(15, request.postalCode);
            stmt.setString(16, request.country);
            stmt.setString(17, request.notes);
            stmt.setString(18, request.customerMessage);
            stmt.setString(19, cartSnapshotJson);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        throw new SQLException("Không thể tạo đơn hàng");
    }

    private static void updateOrderNumber(Connection conn, long orderId, String orderNumber) throws SQLException {
        String sql = "UPDATE orders SET order_number = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderNumber);
            stmt.setLong(2, orderId);
            stmt.executeUpdate();
        }
    }

    private static String generateOrderNumber(long orderId) {
        return String.format("ORD-%06d", orderId);
    }

    private static String buildCartSnapshotJson(CartSnapshot snapshot,
                                                BigDecimal shippingFee,
                                                BigDecimal taxAmount,
                                                BigDecimal discountAmount,
                                                BigDecimal total,
                                                String currency) {
        JsonObject root = new JsonObject();
        root.addProperty("cartId", snapshot.cartId);
        root.addProperty("currency", currency);
        root.addProperty("subtotal", snapshot.subtotal.doubleValue());
        root.addProperty("shippingFee", shippingFee.doubleValue());
        root.addProperty("taxAmount", taxAmount.doubleValue());
        root.addProperty("discountAmount", discountAmount.doubleValue());
        root.addProperty("total", total.doubleValue());
        JsonArray items = new JsonArray();
        for (CartItem item : snapshot.items) {
            JsonObject row = new JsonObject();
            row.addProperty("bookId", item.getBookId());
            row.addProperty("title", item.getBookTitle());
            row.addProperty("quantity", item.getQuantity());
            row.addProperty("unitPrice", item.getUnitPrice().doubleValue());
            row.addProperty("lineTotal", item.getLineTotal().doubleValue());
            items.add(row);
        }
        root.add("items", items);
        return GSON.toJson(root);
    }

    private static CartSnapshot loadCartSnapshot(Connection conn, long cartId) throws SQLException {
        String cartSql = "SELECT user_id, session_id, currency FROM carts WHERE id = ? FOR UPDATE";
        String currency = "VND";
        try (PreparedStatement stmt = conn.prepareStatement(cartSql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy giỏ hàng");
                }
                String dbCurrency = rs.getString("currency");
                if (dbCurrency != null && !dbCurrency.isEmpty()) {
                    currency = dbCurrency;
                }
            }
        }

        String itemsSql = "SELECT ci.id, ci.book_id, ci.quantity, ci.unit_price, ci.created_at, ci.updated_at, " +
                "b.title, b.author, b.image_url FROM cart_items ci JOIN books b ON b.id = ci.book_id WHERE ci.cart_id = ? ORDER BY ci.created_at, ci.id";
        List<CartItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
                    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                    CartItem item = new CartItem(
                            rs.getLong("id"),
                            cartId,
                            rs.getLong("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("image_url"),
                            rs.getBigDecimal("unit_price"),
                            rs.getInt("quantity"),
                            createdAt,
                            updatedAt
                    );
                    items.add(item);
                    subtotal = subtotal.add(item.getLineTotal());
                }
            }
        }
    return new CartSnapshot(cartId, currency, items, subtotal);
    }

    private static Long findActiveCartId(Connection conn, long userId, String sessionId) throws SQLException {
        String sqlUser = "SELECT id FROM carts WHERE user_id = ? AND status = 'active' ORDER BY updated_at DESC, id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUser)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        if (sessionId != null && !sessionId.isEmpty()) {
            String sqlSession = "SELECT id FROM carts WHERE session_id = ? AND status = 'active' ORDER BY updated_at DESC, id DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sqlSession)) {
                stmt.setString(1, sessionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }
        }
        return null;
    }

    private static List<OrderItem> loadOrderItems(Connection conn, long orderId) throws SQLException {
        String sql = "SELECT oi.id, oi.order_id, oi.book_id, oi.quantity, oi.unit_price, oi.total_price, oi.created_at, " +
                "b.title, b.author, b.image_url FROM order_items oi JOIN books b ON b.id = oi.book_id WHERE oi.order_id = ? ORDER BY oi.id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
                    items.add(new OrderItem(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getLong("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("image_url"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price"),
                            rs.getBigDecimal("total_price"),
                            createdAt
                    ));
                }
                return items;
            }
        }
    }

    private static List<PaymentRecord> loadPayments(Connection conn, long orderId) throws SQLException {
        String sql = "SELECT id, order_id, provider, method, status, amount, currency, transaction_code, created_at, updated_at " +
                "FROM payments WHERE order_id = ? ORDER BY created_at";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<PaymentRecord> payments = new ArrayList<>();
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    payments.add(new PaymentRecord(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getString("provider"),
                            rs.getString("method"),
                            rs.getString("status"),
                            rs.getBigDecimal("amount"),
                            rs.getString("currency"),
                            rs.getString("transaction_code"),
                            createdTs != null ? createdTs.toLocalDateTime() : null,
                            updatedTs != null ? updatedTs.toLocalDateTime() : null
                    ));
                }
                return payments;
            }
        }
    }

    private static List<OrderStatusEntry> loadOrderStatusHistory(Connection conn, long orderId) throws SQLException {
        String sql = "SELECT id, order_id, status, note, created_by, created_at FROM order_status_history WHERE order_id = ? ORDER BY created_at";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderStatusEntry> history = new ArrayList<>();
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    history.add(new OrderStatusEntry(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getString("status"),
                            rs.getString("note"),
                            rs.getString("created_by"),
                            createdTs != null ? createdTs.toLocalDateTime() : null
                    ));
                }
                return history;
            }
        }
    }

    private static BigDecimal toAmount(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(Math.max(0.0, value));
    }

    private static class CartSnapshot {
        final long cartId;
        final String currency;
        final List<CartItem> items;
        final BigDecimal subtotal;

        CartSnapshot(long cartId, String currency, List<CartItem> items, BigDecimal subtotal) {
            this.cartId = cartId;
            this.currency = currency != null ? currency : "VND";
            this.items = items;
            this.subtotal = subtotal;
        }
    }
}
