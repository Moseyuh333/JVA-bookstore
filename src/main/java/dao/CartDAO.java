package dao;

import models.Cart;
import models.CartItem;
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

public final class CartDAO {

    private CartDAO() {
    }

    public static Cart ensureActiveCart(Long userId, String sessionId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long cartId = null;
                if (userId != null) {
                    cartId = findActiveCartIdByUser(conn, userId);
                }
                Long sessionCartId = null;
                if (cartId == null && sessionId != null) {
                    sessionCartId = findActiveCartIdBySession(conn, sessionId);
                    cartId = sessionCartId;
                }
                if (userId != null && sessionCartId != null && !sessionCartId.equals(cartId)) {
                    assignCartToUser(conn, sessionCartId, userId, sessionId);
                    cartId = sessionCartId;
                } else if (cartId == null) {
                    cartId = createCart(conn, userId, sessionId);
                } else if (userId != null) {
                    updateCartOwnership(conn, cartId, userId, sessionId);
                }
                conn.commit();
                return loadCart(conn, cartId);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void addOrIncrementItem(long cartId, long bookId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal price = findBookPrice(conn, bookId);
                if (price == null) {
                    throw new SQLException("Book not found: " + bookId);
                }
                String upsertSql = "INSERT INTO cart_items (cart_id, book_id, quantity, unit_price) "
                        + "VALUES (?, ?, ?, ?) "
                        + "ON CONFLICT (cart_id, book_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity, "
                        + "unit_price = EXCLUDED.unit_price, updated_at = CURRENT_TIMESTAMP";
                try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                    stmt.setLong(1, cartId);
                    stmt.setLong(2, bookId);
                    stmt.setInt(3, quantity);
                    stmt.setBigDecimal(4, price);
                    stmt.executeUpdate();
                }
                touchCart(conn, cartId);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void updateQuantity(long cartId, long bookId, int quantity) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (quantity <= 0) {
                    removeItem(conn, cartId, bookId);
                } else {
                    String sql = "UPDATE cart_items SET quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE cart_id = ? AND book_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, quantity);
                        stmt.setLong(2, cartId);
                        stmt.setLong(3, bookId);
                        stmt.executeUpdate();
                    }
                }
                touchCart(conn, cartId);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void removeItem(long cartId, long bookId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                removeItem(conn, cartId, bookId);
                touchCart(conn, cartId);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void clearCart(long cartId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "DELETE FROM cart_items WHERE cart_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, cartId);
                    stmt.executeUpdate();
                }
                touchCart(conn, cartId);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static Cart loadCart(long cartId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return loadCart(conn, cartId);
        }
    }

    private static Cart loadCart(Connection conn, long cartId) throws SQLException {
        Cart cart = new Cart();
        String sql = "SELECT id, user_id, session_id, status, currency, created_at, updated_at FROM carts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Cart not found: " + cartId);
                }
                cart.setId(rs.getLong("id"));
                long userId = rs.getLong("user_id");
                cart.setUserId(rs.wasNull() ? null : userId);
                cart.setSessionId(rs.getString("session_id"));
                cart.setStatus(rs.getString("status"));
                cart.setCurrency(rs.getString("currency"));
                cart.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                cart.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
            }
        }
        cart.setItems(loadItems(conn, cartId));
        cart.setSubtotal(calculateSubtotal(cart.getItems()));
        return cart;
    }

    private static List<CartItem> loadItems(Connection conn, long cartId) throws SQLException {
        String sql = "SELECT ci.id, ci.cart_id, ci.book_id, ci.quantity, ci.unit_price, ci.created_at, ci.updated_at, "
                + "b.title, b.author, b.image_url "
                + "FROM cart_items ci INNER JOIN books b ON b.id = ci.book_id WHERE ci.cart_id = ? ORDER BY ci.created_at";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setCartId(rs.getLong("cart_id"));
                    item.setBookId(rs.getLong("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
                    item.setTitle(rs.getString("title"));
                    item.setAuthor(rs.getString("author"));
                    item.setImageUrl(rs.getString("image_url"));
                    items.add(item);
                }
                return items;
            }
        }
    }

    private static BigDecimal calculateSubtotal(List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.getUnitPrice() != null) {
                subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return subtotal;
    }

    private static Long findActiveCartIdByUser(Connection conn, long userId) throws SQLException {
        String sql = "SELECT id FROM carts WHERE user_id = ? AND status = 'active' ORDER BY updated_at DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        }
    }

    private static Long findActiveCartIdBySession(Connection conn, String sessionId) throws SQLException {
        String sql = "SELECT id FROM carts WHERE session_id = ? AND status = 'active' ORDER BY updated_at DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        }
    }

    private static long createCart(Connection conn, Long userId, String sessionId) throws SQLException {
        String sql = "INSERT INTO carts (user_id, session_id, status) VALUES (?, ?, 'active') RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId == null) {
                stmt.setNull(1, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(1, userId);
            }
            stmt.setString(2, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to create cart");
            }
        }
    }

    private static void updateCartOwnership(Connection conn, long cartId, long userId, String sessionId) throws SQLException {
        String sql = "UPDATE carts SET user_id = ?, session_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, sessionId);
            stmt.setLong(3, cartId);
            stmt.executeUpdate();
        }
    }

    private static void assignCartToUser(Connection conn, long cartId, long userId, String sessionId) throws SQLException {
        Long existingCartId = findActiveCartIdByUser(conn, userId);
        if (existingCartId != null && existingCartId != cartId) {
            mergeCarts(conn, existingCartId, cartId);
            markCartStatus(conn, cartId, "merged");
            touchCart(conn, existingCartId);
        } else {
            updateCartOwnership(conn, cartId, userId, sessionId);
        }
    }

    private static void mergeCarts(Connection conn, long targetCartId, long sourceCartId) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_id, book_id, quantity, unit_price) "
                + "SELECT ?, book_id, quantity, unit_price FROM cart_items WHERE cart_id = ? "
                + "ON CONFLICT (cart_id, book_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity, "
                + "updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, targetCartId);
            stmt.setLong(2, sourceCartId);
            stmt.executeUpdate();
        }
    }

    private static void markCartStatus(Connection conn, long cartId, String status) throws SQLException {
        String sql = "UPDATE carts SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, cartId);
            stmt.executeUpdate();
        }
    }

    private static void removeItem(Connection conn, long cartId, long bookId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND book_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            stmt.setLong(2, bookId);
            stmt.executeUpdate();
        }
    }

    private static void touchCart(Connection conn, long cartId) throws SQLException {
        String sql = "UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            stmt.executeUpdate();
        }
    }

    private static BigDecimal findBookPrice(Connection conn, long bookId) throws SQLException {
        String sql = "SELECT price FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
                return null;
            }
        }
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
