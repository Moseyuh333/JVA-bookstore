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
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CartDAO {

    private CartDAO() {
    }

    public static Cart getOrCreateCart(Long userId, String sessionId) throws SQLException {
        Long normalizedUserId = normalizeUserId(userId);
        String normalizedSession = normalizeSession(sessionId);
        if (normalizedUserId == null && normalizedSession == null) {
            throw new IllegalArgumentException("Either userId or sessionId must be provided to resolve a cart");
        }

        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                Long existingUserCartId = findCartIdByUser(conn, normalizedUserId);
                Long existingSessionCartId = findCartIdBySession(conn, normalizedSession);

                Long resolvedCartId;
                if (existingUserCartId != null) {
                    resolvedCartId = existingUserCartId;
                    if (existingSessionCartId != null && !existingSessionCartId.equals(existingUserCartId)) {
                        mergeCarts(conn, existingSessionCartId, existingUserCartId);
                    }
                } else if (existingSessionCartId != null) {
                    attachCartToUser(conn, existingSessionCartId, normalizedUserId, normalizedSession);
                    resolvedCartId = existingSessionCartId;
                } else {
                    resolvedCartId = createCart(conn, normalizedUserId, normalizedSession);
                }

                touchCart(conn, resolvedCartId);
                Cart cart = hydrateCart(conn, resolvedCartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static Cart addOrIncrementItem(long cartId, long bookId, int quantity) throws SQLException {
        int safeQuantity = quantity <= 0 ? 1 : quantity;
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                BigDecimal unitPrice = resolveBookPrice(conn, bookId);
                if (unitPrice == null) {
                    throw new SQLException("Book not found for id=" + bookId);
                }

                String sql = "INSERT INTO cart_items (cart_id, book_id, quantity, unit_price) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (cart_id, book_id) DO UPDATE SET " +
                        "quantity = cart_items.quantity + EXCLUDED.quantity, " +
                        "unit_price = EXCLUDED.unit_price, " +
                        "updated_at = CURRENT_TIMESTAMP";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, cartId);
                    stmt.setLong(2, bookId);
                    stmt.setInt(3, safeQuantity);
                    stmt.setBigDecimal(4, unitPrice);
                    stmt.executeUpdate();
                }

                touchCart(conn, cartId);
                Cart cart = hydrateCart(conn, cartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static Cart setItemQuantity(long cartId, long bookId, int quantity) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                if (quantity <= 0) {
                    deleteCartItem(conn, cartId, bookId);
                } else {
                    BigDecimal unitPrice = resolveBookPrice(conn, bookId);
                    if (unitPrice == null) {
                        throw new SQLException("Book not found for id=" + bookId);
                    }
                    int updated;
                    try (PreparedStatement update = conn.prepareStatement(
                            "UPDATE cart_items SET quantity = ?, unit_price = ?, updated_at = CURRENT_TIMESTAMP " +
                                    "WHERE cart_id = ? AND book_id = ?")) {
                        update.setInt(1, quantity);
                        update.setBigDecimal(2, unitPrice);
                        update.setLong(3, cartId);
                        update.setLong(4, bookId);
                        updated = update.executeUpdate();
                    }
                    if (updated == 0) {
                        try (PreparedStatement insert = conn.prepareStatement(
                                "INSERT INTO cart_items (cart_id, book_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                            insert.setLong(1, cartId);
                            insert.setLong(2, bookId);
                            insert.setInt(3, quantity);
                            insert.setBigDecimal(4, unitPrice);
                            insert.executeUpdate();
                        }
                    }
                }

                touchCart(conn, cartId);
                Cart cart = hydrateCart(conn, cartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static Cart removeItem(long cartId, long bookId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                deleteCartItem(conn, cartId, bookId);
                touchCart(conn, cartId);
                Cart cart = hydrateCart(conn, cartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static Cart clearCart(long cartId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement delete = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                    delete.setLong(1, cartId);
                    delete.executeUpdate();
                }
                touchCart(conn, cartId);
                Cart cart = hydrateCart(conn, cartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static Cart getCartById(long cartId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return hydrateCart(conn, cartId);
        }
    }

    public static Cart attachCartToUser(long cartId, long userId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                attachCartToUser(conn, cartId, normalizeUserId(userId), null);
                Cart cart = hydrateCart(conn, cartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static Cart mergeGuestCartIntoUserCart(String sessionId, long userId) throws SQLException {
        Long normalizedUserId = normalizeUserId(userId);
        String normalizedSession = normalizeSession(sessionId);
        if (normalizedSession == null) {
            throw new IllegalArgumentException("sessionId is required to merge guest cart");
        }
        try (Connection conn = DBUtil.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                Long sessionCartId = findCartIdBySession(conn, normalizedSession);
                if (sessionCartId == null) {
                    conn.commit();
                    return null;
                }
                Long userCartId = findCartIdByUser(conn, normalizedUserId);
                if (userCartId == null) {
                    attachCartToUser(conn, sessionCartId, normalizedUserId, normalizedSession);
                    Cart cart = hydrateCart(conn, sessionCartId);
                    conn.commit();
                    return cart;
                }
                mergeCarts(conn, sessionCartId, userCartId);
                Cart cart = hydrateCart(conn, userCartId);
                conn.commit();
                return cart;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private static Long findCartIdByUser(Connection conn, Long userId) throws SQLException {
        if (userId == null) {
            return null;
        }
        String sql = "SELECT id FROM carts WHERE user_id = ? AND status = 'active' ORDER BY updated_at DESC, id DESC LIMIT 1";
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

    private static Long findCartIdBySession(Connection conn, String sessionId) throws SQLException {
        if (sessionId == null) {
            return null;
        }
        String sql = "SELECT id FROM carts WHERE session_id = ? AND status = 'active' ORDER BY updated_at DESC, id DESC LIMIT 1";
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

    private static Long createCart(Connection conn, Long userId, String sessionId) throws SQLException {
        String sql = "INSERT INTO carts (user_id, session_id, status) VALUES (?, ?, 'active') RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setLong(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            if (sessionId != null) {
                stmt.setString(2, sessionId);
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Unable to create cart record");
            }
        }
    }

    private static void attachCartToUser(Connection conn, long cartId, Long userId, String sessionId) throws SQLException {
        if (userId == null) {
            return;
        }
        String sql = "UPDATE carts SET user_id = ?, session_id = NULL, status = 'active', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, cartId);
            stmt.executeUpdate();
        }
        if (sessionId != null) {
            try (PreparedStatement deactivate = conn.prepareStatement(
                    "UPDATE carts SET status = 'merged', updated_at = CURRENT_TIMESTAMP WHERE session_id = ? AND id <> ?")) {
                deactivate.setString(1, sessionId);
                deactivate.setLong(2, cartId);
                deactivate.executeUpdate();
            }
        }
    }

    private static void mergeCarts(Connection conn, long sourceCartId, long targetCartId) throws SQLException {
        if (sourceCartId == targetCartId) {
            return;
        }
        String mergeItemsSql = "INSERT INTO cart_items (cart_id, book_id, quantity, unit_price, created_at, updated_at) " +
                "SELECT ?, book_id, quantity, unit_price, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM cart_items WHERE cart_id = ? " +
                "ON CONFLICT (cart_id, book_id) DO UPDATE SET " +
                "quantity = cart_items.quantity + EXCLUDED.quantity, " +
                "unit_price = EXCLUDED.unit_price, " +
                "updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement merge = conn.prepareStatement(mergeItemsSql)) {
            merge.setLong(1, targetCartId);
            merge.setLong(2, sourceCartId);
            merge.executeUpdate();
        }

        try (PreparedStatement deleteItems = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
            deleteItems.setLong(1, sourceCartId);
            deleteItems.executeUpdate();
        }

        try (PreparedStatement markSource = conn.prepareStatement(
                "UPDATE carts SET status = 'merged', session_id = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            markSource.setLong(1, sourceCartId);
            markSource.executeUpdate();
        }

        touchCart(conn, targetCartId);
    }

    private static void deleteCartItem(Connection conn, long cartId, long bookId) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ? AND book_id = ?")) {
            delete.setLong(1, cartId);
            delete.setLong(2, bookId);
            delete.executeUpdate();
        }
    }

    private static Cart hydrateCart(Connection conn, long cartId) throws SQLException {
        String cartSql = "SELECT id, user_id, session_id, status, currency, created_at, updated_at FROM carts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(cartSql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                List<CartItem> items = fetchCartItems(conn, cartId);
                Timestamp createdTs = rs.getTimestamp("created_at");
                Timestamp updatedTs = rs.getTimestamp("updated_at");
                LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
                LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                Long userId = (Long) rs.getObject("user_id");
                return new Cart(
                        rs.getLong("id"),
                        userId,
                        rs.getString("session_id"),
                        rs.getString("status"),
                        rs.getString("currency"),
                        createdAt,
                        updatedAt,
                        items
                );
            }
        }
    }

    private static List<CartItem> fetchCartItems(Connection conn, long cartId) throws SQLException {
        String sql = "SELECT ci.id, ci.cart_id, ci.book_id, ci.quantity, ci.unit_price, ci.created_at, ci.updated_at, " +
                "b.title, b.author, b.image_url " +
                "FROM cart_items ci " +
                "JOIN books b ON b.id = ci.book_id " +
                "WHERE ci.cart_id = ? ORDER BY ci.created_at, ci.id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
                    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                    CartItem item = new CartItem(
                            rs.getLong("id"),
                            rs.getLong("cart_id"),
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
                }
                return items;
            }
        }
    }

    private static void touchCart(Connection conn, long cartId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            stmt.setLong(1, cartId);
            stmt.executeUpdate();
        }
    }

    private static BigDecimal resolveBookPrice(Connection conn, long bookId) throws SQLException {
        String sql = "SELECT price FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal price = rs.getBigDecimal(1);
                    return price != null ? price : BigDecimal.ZERO;
                }
                return null;
            }
        }
    }

    private static Long normalizeUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        long value = userId;
        return value > 0 ? value : null;
    }

    private static Long normalizeUserId(long userId) {
        return userId > 0 ? userId : null;
    }

    private static String normalizeSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        String trimmed = sessionId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
