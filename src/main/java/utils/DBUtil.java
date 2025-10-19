package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class DBUtil {
    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            String databaseUrl = System.getenv("DATABASE_URL");
            System.out.println("=== DATABASE CONFIGURATION ===");
            System.out.println("DATABASE_URL env present: " + (databaseUrl != null && !databaseUrl.isEmpty()));
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                // Expected format: postgres://user:pass@host:port/db
                URI dbUri = new URI(databaseUrl);
                username = dbUri.getUserInfo().split(":")[0];
                password = dbUri.getUserInfo().split(":")[1];
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + (dbUri.getPort() != -1 ? ":" + dbUri.getPort() : "") + dbUri.getPath();
                // Ensure SSL for Heroku
                url = jdbcUrl + "?sslmode=require";
            } else {
                try (InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (input == null) {
                        throw new RuntimeException("db.properties not found in classpath and DATABASE_URL env var not set");
                    }
                    Properties prop = new Properties();
                    prop.load(input);
                    url = prop.getProperty("db.url");
                    username = prop.getProperty("db.username");
                    password = prop.getProperty("db.password");
                }
            }
            Class.forName("org.postgresql.Driver");
            System.out.println("DB URL: " + (url != null ? url.replaceAll("(?<=[a-z]://)[^:]*:[^@]*", "***:***") : "NULL"));
            System.out.println("DB User: " + (username != null ? username : "NULL"));
            System.out.println("DB Password set: " + (password != null && !password.isEmpty()) + "");
            System.out.println("=============================");
            initDatabase();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid DATABASE_URL", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initDatabase() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "email VARCHAR(100) UNIQUE NOT NULL," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "email_verified BOOLEAN DEFAULT FALSE," +
                    "verification_token VARCHAR(255)," +
                    "reset_token VARCHAR(255)," +
                    "reset_expiry TIMESTAMP," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createTableSQL);

                // Add verification_token column if missing
                try {
                    String addColumnSQL = "ALTER TABLE users ADD COLUMN verification_token VARCHAR(255)";
                    stmt.execute(addColumnSQL);
                } catch (SQLException e) {
                    // Ignore if column already exists
                    if (!e.getMessage().contains("already exists")) {
                        throw e;
                    }
                }

                // Add reset_token column if missing
                try {
                    String addResetTokenSQL = "ALTER TABLE users ADD COLUMN reset_token VARCHAR(255)";
                    stmt.execute(addResetTokenSQL);
                } catch (SQLException e) {
                    // Ignore if column already exists
                    if (!e.getMessage().contains("already exists")) {
                        throw e;
                    }
                }

                // Add reset_expiry column if missing
                try {
                    String addResetExpirySQL = "ALTER TABLE users ADD COLUMN reset_expiry TIMESTAMP";
                    stmt.execute(addResetExpirySQL);
                } catch (SQLException e) {
                    // Ignore if column already exists
                    if (!e.getMessage().contains("already exists")) {
                        throw e;
                    }
                }

                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users(reset_token)");
                
                // Create OTP verifications table
                String createOTPTableSQL = "CREATE TABLE IF NOT EXISTS otp_verifications (" +
                    "id SERIAL PRIMARY KEY," +
                    "email VARCHAR(100) NOT NULL," +
                    "otp_code VARCHAR(6) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "expires_at TIMESTAMP NOT NULL," +
                    "verified BOOLEAN DEFAULT FALSE," +
                    "attempts INT DEFAULT 0" +
                    ")";
                stmt.execute(createOTPTableSQL);
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_otp_email ON otp_verifications(email)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_otp_code ON otp_verifications(otp_code)");

                // Core catalog tables
                String createBooksTableSQL = "CREATE TABLE IF NOT EXISTS books (" +
                    "id SERIAL PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "author VARCHAR(255)," +
                    "isbn VARCHAR(20)," +
                    "price DECIMAL(10, 2)," +
                    "description TEXT," +
                    "category VARCHAR(100)," +
                    "stock_quantity INTEGER DEFAULT 0," +
                    "image_url VARCHAR(500)," +
                    "shop_id INTEGER REFERENCES shops(id) ON DELETE CASCADE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createBooksTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_category ON books(category)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_shop_id ON books(shop_id)");

                ensureBooksSchema(conn);

                String createBookMetricsTableSQL = "CREATE TABLE IF NOT EXISTS book_metrics (" +
                    "book_id INTEGER PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE," +
                    "total_sold INTEGER DEFAULT 0," +
                    "avg_rating DOUBLE PRECISION DEFAULT 0," +
                    "rating_count INTEGER DEFAULT 0," +
                    "favorite_count INTEGER DEFAULT 0" +
                    ")";
                stmt.execute(createBookMetricsTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_book_metrics_popularity ON book_metrics(total_sold DESC, favorite_count DESC)");

                String createOrdersTableSQL = "CREATE TABLE IF NOT EXISTS orders (" +
                    "id SERIAL PRIMARY KEY," +
                    "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "total_amount DECIMAL(10, 2) NOT NULL," +
                    "status VARCHAR(50) DEFAULT 'pending'," +
                    "shipping_address TEXT," +
                    "payment_method VARCHAR(50)," +
                    "notes TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createOrdersTableSQL);

                if (!columnExists(conn, "orders", "order_date")) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                }
                if (!columnExists(conn, "orders", "total_amount")) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0");
                }
                if (!columnExists(conn, "orders", "status")) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN status VARCHAR(50) DEFAULT 'pending'");
                }
                if (!columnExists(conn, "orders", "created_at")) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                }
                if (!columnExists(conn, "orders", "updated_at")) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                }

                stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date)");

                String createOrderItemsTableSQL = "CREATE TABLE IF NOT EXISTS order_items (" +
                    "id SERIAL PRIMARY KEY," +
                    "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE," +
                    "book_id INTEGER NOT NULL REFERENCES books(id)," +
                    "quantity INTEGER NOT NULL," +
                    "unit_price DECIMAL(10, 2) NOT NULL," +
                    "total_price DECIMAL(10, 2) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createOrderItemsTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_items_book_id ON order_items(book_id)");

                // Engagement tables that power catalog ranking
                String createBookFavoritesTableSQL = "CREATE TABLE IF NOT EXISTS book_favorites (" +
                    "id SERIAL PRIMARY KEY," +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE," +
                    "book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createBookFavoritesTableSQL);
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_book_favorites_user_book ON book_favorites(user_id, book_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_book_favorites_book_id ON book_favorites(book_id)");

                String createBookReviewsTableSQL = "CREATE TABLE IF NOT EXISTS book_reviews (" +
                    "id SERIAL PRIMARY KEY," +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE," +
                    "book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE," +
                    "rating INTEGER CHECK (rating BETWEEN 1 AND 5)," +
                    "title VARCHAR(255)," +
                    "content TEXT," +
                    "media_url VARCHAR(500)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "status VARCHAR(20) DEFAULT 'published'" +
                    ")";
                stmt.execute(createBookReviewsTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_book_reviews_book_id ON book_reviews(book_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_book_reviews_user_id ON book_reviews(user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_book_reviews_status ON book_reviews(status)");

                // Admin-specific tables
                String createCategoriesTableSQL = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id SERIAL PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL UNIQUE," +
                    "slug VARCHAR(255) NOT NULL UNIQUE," +
                    "description TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createCategoriesTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug)");

                String createShopsTableSQL = "CREATE TABLE IF NOT EXISTS shops (" +
                    "id SERIAL PRIMARY KEY," +
                    "owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE," +
                    "name VARCHAR(255) NOT NULL," +
                    "description TEXT," +
                    "logo_url VARCHAR(500)," +
                    "status VARCHAR(50) DEFAULT 'active'," +
                    "commission_rate DECIMAL(5,2) DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createShopsTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_owner_id ON shops(owner_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_status ON shops(status)");

                String createCouponsTableSQL = "CREATE TABLE IF NOT EXISTS coupons (" +
                    "id SERIAL PRIMARY KEY," +
                    "code VARCHAR(50) NOT NULL UNIQUE," +
                    "description TEXT," +
                    "type VARCHAR(20) NOT NULL CHECK (type IN ('percentage', 'fixed', 'shipping'))," +
                    "discount_value DECIMAL(10,2) NOT NULL," +
                    "max_discount DECIMAL(10,2)," +
                    "min_order DECIMAL(10,2)," +
                    "usage_limit INTEGER," +
                    "used_count INTEGER DEFAULT 0," +
                    "start_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "end_at TIMESTAMP," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "apply_to VARCHAR(20) DEFAULT 'product' CHECK (apply_to IN ('product', 'shipping'))," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createCouponsTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_coupons_code ON coupons(code)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(active)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_coupons_start_end ON coupons(start_at, end_at)");

                String createShippersTableSQL = "CREATE TABLE IF NOT EXISTS shippers (" +
                    "id SERIAL PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "phone VARCHAR(20)," +
                    "email VARCHAR(100)," +
                    "base_fee DECIMAL(10,2) NOT NULL DEFAULT 0," +
                    "service_area TEXT," +
                    "estimated_time VARCHAR(100)," +
                    "status VARCHAR(50) DEFAULT 'active'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createShippersTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_shippers_status ON shippers(status)");
            }

            ensurePasswordHashColumn(conn);
            BookDataLoader.seedBooksIfEmpty(conn);
            BookDataLoader.refreshBookAssets(conn);
            seedBookMetrics(conn);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    private static void ensurePasswordHashColumn(Connection conn) {
        try {
            boolean hasPasswordHash = columnExists(conn, "users", "password_hash");
            boolean hasLegacyPassword = columnExists(conn, "users", "password");

            try (Statement stmt = conn.createStatement()) {
                if (!hasPasswordHash) {
                    stmt.execute("ALTER TABLE users ADD COLUMN password_hash VARCHAR(255)");
                }

                if (hasLegacyPassword) {
                    stmt.execute("UPDATE users SET password_hash = password WHERE password_hash IS NULL");
                    stmt.execute("ALTER TABLE users DROP COLUMN password");
                }
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile password column: " + ex.getMessage());
        }
    }

    private static void ensureBooksSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "books", "isbn")) {
                stmt.execute("ALTER TABLE books ADD COLUMN isbn VARCHAR(20)");
            }

            if (!columnExists(conn, "books", "price")) {
                stmt.execute("ALTER TABLE books ADD COLUMN price DECIMAL(10, 2)");
            }
            stmt.execute("UPDATE books SET price = COALESCE(price, 0)");

            if (!columnExists(conn, "books", "description")) {
                stmt.execute("ALTER TABLE books ADD COLUMN description TEXT");
            }

            if (!columnExists(conn, "books", "category")) {
                stmt.execute("ALTER TABLE books ADD COLUMN category VARCHAR(100)");
            }

            if (!columnExists(conn, "books", "stock_quantity")) {
                stmt.execute("ALTER TABLE books ADD COLUMN stock_quantity INTEGER DEFAULT 0");
            }
            stmt.execute("UPDATE books SET stock_quantity = COALESCE(stock_quantity, 0)");

            if (!columnExists(conn, "books", "image_url")) {
                stmt.execute("ALTER TABLE books ADD COLUMN image_url VARCHAR(500)");
            }

            if (!columnExists(conn, "books", "created_at")) {
                stmt.execute("ALTER TABLE books ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            stmt.execute("UPDATE books SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP)");

            if (!columnExists(conn, "books", "updated_at")) {
                stmt.execute("ALTER TABLE books ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            stmt.execute("UPDATE books SET updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP)");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile books schema: " + ex.getMessage());
        }
    }

    private static void seedBookMetrics(Connection conn) {
        try {
            List<Long> bookIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM books ORDER BY id");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookIds.add(rs.getLong(1));
                }
            }

            if (bookIds.isEmpty()) {
                return;
            }

            int beforeCount = countRows(conn, "book_metrics");
        try (PreparedStatement insert = conn.prepareStatement(
            "INSERT INTO book_metrics (book_id, total_sold, avg_rating, rating_count, favorite_count) " +
            "VALUES (?, ?, ?, ?, ?) ON CONFLICT (book_id) DO UPDATE SET " +
            "total_sold = EXCLUDED.total_sold, " +
            "avg_rating = EXCLUDED.avg_rating, " +
            "rating_count = EXCLUDED.rating_count, " +
            "favorite_count = EXCLUDED.favorite_count")) {
                for (Long bookId : bookIds) {
                    Random random = new Random(20251017L + (bookId * 1973));
                    boolean highlight = random.nextDouble() < 0.28;
                    int totalSold = highlight ? 120 + random.nextInt(220) : random.nextInt(130);
                    int ratingCount = highlight ? 25 + random.nextInt(70) : random.nextInt(35);
                    double averageRating;
                    if (ratingCount == 0) {
                        averageRating = 0.0;
                    } else {
                        double base = highlight ? 4.0 : 3.1;
                        averageRating = Math.min(5.0, base + random.nextDouble() * 1.2);
                    }
                    int favoriteCount = highlight ? 90 + random.nextInt(130) : random.nextInt(80);

                    insert.setLong(1, bookId);
                    insert.setInt(2, totalSold);
                    insert.setDouble(3, Math.round(averageRating * 100.0) / 100.0);
                    insert.setInt(4, ratingCount);
                    insert.setInt(5, favoriteCount);
                    insert.addBatch();
                }
                insert.executeBatch();
            }

            int afterCount = countRows(conn, "book_metrics");
            if (afterCount > beforeCount) {
                System.out.println("BookDataLoader - Seeded synthetic engagement metrics for " + (afterCount - beforeCount) + " books.");
            } else if (!bookIds.isEmpty()) {
                System.out.println("BookDataLoader - Refreshed synthetic engagement metrics for " + bookIds.size() + " books.");
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to seed book metrics: " + ex.getMessage());
        }
    }

    private static int countRows(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (url == null || username == null || password == null) {
            throw new SQLException("Database configuration not initialized. Ensure DATABASE_URL env var is set or db.properties exists.");
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static void createUser(String username, String email, String passwordHash, String verificationToken) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, verification_token) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, verificationToken);
            pstmt.executeUpdate();
        }
    }

    public static void createUserVerified(String username, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, email_verified) VALUES (?, ?, ?, true)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.executeUpdate();
        }
    }

    public static boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static String getUserPasswordHash(String username) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
                return null;
            }
        }
    }

    public static boolean isUserVerified(String username) throws SQLException {
        String sql = "SELECT email_verified FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("email_verified");
                }
                return false;
            }
        }
    }

    public static String getUserByEmail(String email) throws SQLException {
        String sql = "SELECT username FROM users WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
                return null;
            }
        }
    }

    public static boolean verifyUser(String token) throws SQLException {
        String sql = "UPDATE users SET email_verified = TRUE, verification_token = NULL WHERE verification_token = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean setResetToken(String email, String token) throws SQLException {
        String sql = "UPDATE users SET reset_token = ?, reset_expiry = NOW() + INTERVAL '1 hour' WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static String getResetToken(String token) throws SQLException {
        String sql = "SELECT email FROM users WHERE reset_token = ? AND reset_expiry > NOW()";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
                return null;
            }
        }
    }

    public static boolean updatePassword(String email, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, reset_token = NULL, reset_expiry = NULL WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHash);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static void deleteAllUsers() throws SQLException {
        String sql = "DELETE FROM users";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = pstmt.executeUpdate();
            System.out.println("Deleted " + count + " users from database");
        }
    }
}
