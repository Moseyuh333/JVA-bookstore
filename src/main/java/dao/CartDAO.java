package dao;

import models.Cart;
import models.CartItem;
import utils.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CartDAO {

    private CartDAO() {
    }

    private static final class CartPointer {
        final long id;
        final Long userId;

        CartPointer(long id, Long userId) {
            this.id = id;
            this.userId = userId;
        }
    }

    public static Cart ensureActiveCart(Long userId, String sessionId) throws SQLException {
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long activeCartId = null;
                if (userId != null) {
                    activeCartId = findActiveCartIdByUser(conn, userId);
                }

                CartPointer sessionCart = (sessionId != null) ? findActiveCartBySession(conn, sessionId) : null;

                if (sessionCart != null && sessionCart.userId != null && (userId == null || !sessionCart.userId.equals(userId))) {
                    clearCartSession(conn, sessionCart.id, sessionId);
                    sessionCart = null;
                }

                if (userId != null && sessionCart != null) {
                    if (activeCartId == null) {
                        assignCartToUser(conn, sessionCart.id, userId, sessionId);
                        activeCartId = sessionCart.id;
                    } else if (sessionCart.id != activeCartId) {
                        mergeCarts(conn, activeCartId, sessionCart.id);
                        markCartStatus(conn, sessionCart.id, "merged");
                        clearCartSession(conn, sessionCart.id, null);
                        touchCart(conn, activeCartId);
                    }
                }

                if (activeCartId == null) {
                    if (sessionCart != null) {
                        activeCartId = sessionCart.id;
                        if (sessionId != null) {
                            updateCartSession(conn, activeCartId, sessionId);
                        }
                    } else {
                        activeCartId = createCart(conn, userId, sessionId);
                    }
                } else {
                    if (userId != null) {
                        updateCartOwnership(conn, activeCartId, userId, sessionId);
                    } else if (sessionId != null) {
                        updateCartSession(conn, activeCartId, sessionId);
                    }
                }

                conn.commit();
                return loadCart(conn, activeCartId);
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
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal price = findBookPrice(conn, bookId);
                if (price == null) {
                    price = BigDecimal.ZERO;
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
        ensureSchema();
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
        ensureSchema();
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
        ensureSchema();
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
        ensureSchema();
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
                + "b.title, b.author, b.image_url, b.shop_id, s.name AS shop_name "
                + "FROM cart_items ci "
                + "INNER JOIN books b ON b.id = ci.book_id "
                + "LEFT JOIN shops s ON s.id = b.shop_id "
                + "WHERE ci.cart_id = ? ORDER BY ci.created_at";
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
                    int shopIdValue = rs.getInt("shop_id");
                    item.setShopId(rs.wasNull() ? null : shopIdValue);
                    item.setShopName(rs.getString("shop_name"));
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

    private static CartPointer findActiveCartBySession(Connection conn, String sessionId) throws SQLException {
        String sql = "SELECT id, user_id FROM carts WHERE session_id = ? AND status = 'active' ORDER BY updated_at DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    long owner = rs.getLong("user_id");
                    Long resolvedOwner = rs.wasNull() ? null : owner;
                    return new CartPointer(id, resolvedOwner);
                }
            }
        }
        return null;
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

    private static void updateCartSession(Connection conn, long cartId, String sessionId) throws SQLException {
        String sql = "UPDATE carts SET session_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            stmt.setLong(2, cartId);
            stmt.executeUpdate();
        }
    }

    private static void clearCartSession(Connection conn, long cartId, String expectedSessionId) throws SQLException {
        String sql;
        if (expectedSessionId == null) {
            sql = "UPDATE carts SET session_id = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        } else {
            sql = "UPDATE carts SET session_id = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND session_id = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            if (expectedSessionId != null) {
                stmt.setString(2, expectedSessionId);
            }
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

    public static CartItem createStandaloneItem(long bookId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            return createStandaloneItem(conn, bookId, quantity);
        }
    }

    private static CartItem createStandaloneItem(Connection conn, long bookId, int quantity) throws SQLException {
        boolean canUseOriginalPrice = hasOriginalPriceColumn(conn);
        boolean hasStockQuantity = columnExists(conn, "books", "stock_quantity");
        boolean hasStockText = columnExists(conn, "books", "stock");

        StringBuilder sql = new StringBuilder("SELECT id, title, author, image_url, price");
        if (canUseOriginalPrice) {
            sql.append(", original_price");
        }
        if (hasStockQuantity) {
            sql.append(", stock_quantity");
        }
        if (hasStockText) {
            sql.append(", stock");
        }
        sql.append(" FROM books WHERE id = ?");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy sách với mã " + bookId);
                }
                CartItem item = new CartItem();
                item.setBookId(bookId);
                item.setTitle(rs.getString("title"));
                item.setAuthor(rs.getString("author"));
                item.setImageUrl(rs.getString("image_url"));
                item.setQuantity(quantity);
                BigDecimal price = rs.getBigDecimal("price");
                if ((price == null || price.compareTo(BigDecimal.ZERO) <= 0) && canUseOriginalPrice) {
                    BigDecimal fallback = rs.getBigDecimal("original_price");
                    if (fallback != null && fallback.compareTo(BigDecimal.ZERO) > 0) {
                        price = fallback;
                    }
                }
                if (price == null) {
                    price = BigDecimal.ZERO;
                }
                item.setUnitPrice(price);
                return item;
            }
        }
    }

    private static BigDecimal findBookPrice(Connection conn, long bookId) throws SQLException {
        boolean canUseOriginalPrice = hasOriginalPriceColumn(conn);
        String sql = "SELECT price" + (canUseOriginalPrice ? ", original_price" : "") + " FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                BigDecimal price = rs.getBigDecimal("price");
                if ((price == null || price.compareTo(BigDecimal.ZERO) <= 0) && canUseOriginalPrice) {
                    BigDecimal fallback = rs.getBigDecimal("original_price");
                    if (fallback != null && fallback.compareTo(BigDecimal.ZERO) > 0) {
                        price = fallback;
                    }
                }
                return price;
            }
        }
    }

    private static volatile Boolean booksHasOriginalPriceColumn;

    private static boolean hasOriginalPriceColumn(Connection conn) throws SQLException {
        Boolean cached = booksHasOriginalPriceColumn;
        if (cached != null) {
            return cached;
        }
        synchronized (CartDAO.class) {
            if (booksHasOriginalPriceColumn == null) {
                booksHasOriginalPriceColumn = columnExists(conn, "books", "original_price");
            }
            return booksHasOriginalPriceColumn;
        }
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    private static volatile boolean schemaReady = false;

    private static void ensureSchema() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (CartDAO.class) {
            if (schemaReady) {
                return;
            }
            try (Connection conn = DBUtil.getConnection();
                 Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                try {
                    stmt.execute("CREATE TABLE IF NOT EXISTS carts (" +
                            "id SERIAL PRIMARY KEY," +
                            "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE," +
                            "session_id VARCHAR(64)," +
                            "status VARCHAR(20) DEFAULT 'active'," +
                            "currency VARCHAR(10) DEFAULT 'VND'," +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "CONSTRAINT chk_carts_status CHECK (status IN ('active','merged','abandoned','checked_out'))" +
                            ")");

                    ensureColumn(conn, stmt, "carts", "session_id", "ALTER TABLE carts ADD COLUMN session_id VARCHAR(64)");
                    ensureColumn(conn, stmt, "carts", "status", "ALTER TABLE carts ADD COLUMN status VARCHAR(20) DEFAULT 'active'");
                    stmt.execute("ALTER TABLE carts ALTER COLUMN status SET DEFAULT 'active'");
                    stmt.execute("UPDATE carts SET status = 'active' WHERE status IS NULL");

                    ensureColumn(conn, stmt, "carts", "currency", "ALTER TABLE carts ADD COLUMN currency VARCHAR(10) DEFAULT 'VND'");
                    stmt.execute("ALTER TABLE carts ALTER COLUMN currency SET DEFAULT 'VND'");
                    stmt.execute("UPDATE carts SET currency = 'VND' WHERE currency IS NULL OR currency = ''");

                    ensureColumn(conn, stmt, "carts", "created_at", "ALTER TABLE carts ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("ALTER TABLE carts ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("UPDATE carts SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP)");

                    ensureColumn(conn, stmt, "carts", "updated_at", "ALTER TABLE carts ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("ALTER TABLE carts ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("UPDATE carts SET updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP)");

                    ensureConstraint(conn, stmt, "carts", "chk_carts_status",
                            "ALTER TABLE carts ADD CONSTRAINT chk_carts_status CHECK (status IN ('active','merged','abandoned','checked_out'))");

                    stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_carts_session_active ON carts(session_id) WHERE status = 'active'");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_carts_user ON carts(user_id)");

                    stmt.execute("CREATE TABLE IF NOT EXISTS cart_items (" +
                            "id SERIAL PRIMARY KEY," +
                            "cart_id INTEGER NOT NULL REFERENCES carts(id) ON DELETE CASCADE," +
                            "book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE," +
                            "quantity INTEGER NOT NULL CHECK (quantity > 0)," +
                            "unit_price DECIMAL(12,2) NOT NULL DEFAULT 0," +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "CONSTRAINT uq_cart_items_cart_book UNIQUE (cart_id, book_id)" +
                            ")");

                    ensureColumn(conn, stmt, "cart_items", "unit_price", "ALTER TABLE cart_items ADD COLUMN unit_price DECIMAL(12,2) DEFAULT 0");
                    stmt.execute("ALTER TABLE cart_items ALTER COLUMN unit_price SET DEFAULT 0");
                    stmt.execute("UPDATE cart_items SET unit_price = 0 WHERE unit_price IS NULL");

                    ensureColumn(conn, stmt, "cart_items", "created_at", "ALTER TABLE cart_items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("ALTER TABLE cart_items ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("UPDATE cart_items SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP)");

                    ensureColumn(conn, stmt, "cart_items", "updated_at", "ALTER TABLE cart_items ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("ALTER TABLE cart_items ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("UPDATE cart_items SET updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP)");

                    consolidateCartItemDuplicates(conn);
                    ensureCartItemsUniqueConstraint(conn, stmt);

                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id)");

                    conn.commit();
                    schemaReady = true;
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }
    }

    private static void ensureColumn(Connection conn, Statement stmt, String tableName, String columnName, String alterSql) throws SQLException {
        if (!columnExists(conn, tableName, columnName)) {
            stmt.execute(alterSql);
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName.toLowerCase());
            stmt.setString(2, columnName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void ensureConstraint(Connection conn, Statement stmt, String tableName, String constraintName, String alterSql) throws SQLException {
        if (!constraintExists(conn, tableName, constraintName)) {
            stmt.execute(alterSql);
        }
    }

    private static boolean constraintExists(Connection conn, String tableName, String constraintName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.table_constraints WHERE table_schema = current_schema() AND table_name = ? AND constraint_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName.toLowerCase());
            stmt.setString(2, constraintName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void consolidateCartItemDuplicates(Connection conn) throws SQLException {
        String aggregateSql = "WITH ranked AS ( "
                + "SELECT id, cart_id, book_id, quantity, unit_price, "
                + "ROW_NUMBER() OVER (PARTITION BY cart_id, book_id ORDER BY id) rn, "
                + "SUM(quantity) OVER (PARTITION BY cart_id, book_id) total_qty "
                + "FROM cart_items ) "
                + "UPDATE cart_items ci SET quantity = ranked.total_qty, updated_at = CURRENT_TIMESTAMP "
                + "FROM ranked WHERE ci.id = ranked.id AND ranked.rn = 1";

        String deleteSql = "WITH ranked AS ( "
                + "SELECT id, ROW_NUMBER() OVER (PARTITION BY cart_id, book_id ORDER BY id) rn "
                + "FROM cart_items ) "
                + "DELETE FROM cart_items WHERE id IN (SELECT id FROM ranked WHERE rn > 1)";

        try (Statement cleanup = conn.createStatement()) {
            cleanup.executeUpdate(aggregateSql);
            cleanup.executeUpdate(deleteSql);
        }
    }

    private static void ensureCartItemsUniqueConstraint(Connection conn, Statement stmt) throws SQLException {
        if (constraintExists(conn, "cart_items", "uq_cart_items_cart_book")) {
            return;
        }
        try {
            stmt.execute("ALTER TABLE cart_items ADD CONSTRAINT uq_cart_items_cart_book UNIQUE (cart_id, book_id)");
        } catch (SQLException ex) {
            String sqlState = ex.getSQLState();
            if (!"42710".equals(sqlState)) { // duplicate_object
                throw ex;
            }
        }
    }
}
