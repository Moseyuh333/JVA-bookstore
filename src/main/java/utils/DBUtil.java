package utils;

import java.math.BigDecimal;
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
                // Ensure SSL for Heroku and UTF-8 encoding
                url = jdbcUrl + "?sslmode=require&charSet=UTF-8";
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
                    // Ensure UTF-8 encoding for local database
                    if (!url.contains("charSet")) {
                        url += (url.contains("?") ? "&" : "?") + "charSet=UTF-8";
                    }
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
                    "role VARCHAR(20) DEFAULT 'user'," +
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

                // Add role column if missing
                try {
                    String addRoleSQL = "ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'customer'";
                    stmt.execute(addRoleSQL);
                } catch (SQLException e) {
                    // Ignore if column already exists
                    if (!e.getMessage().contains("already exists")) {
                        throw e;
                    }
                }

                // Add status column if missing
                try {
                    String addStatusSQL = "ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'pending'";
                    stmt.execute(addStatusSQL);
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
                    "payment_metadata JSONB," +
                    "notes TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createOrdersTableSQL);
                ensureOrdersSchema(conn);

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
                ensureOrderItemsSchema(conn);

                String createOrderStatusHistoryTableSQL = "CREATE TABLE IF NOT EXISTS order_status_history (" +
                    "id SERIAL PRIMARY KEY," +
                    "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE," +
                    "status VARCHAR(20) NOT NULL," +
                    "note TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "created_by VARCHAR(100)" +
                    ")";
                stmt.execute(createOrderStatusHistoryTableSQL);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_status_history_order ON order_status_history(order_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_status_history_status ON order_status_history(status)");
                ensureOrderStatusHistorySchema(conn);

                String createOrderPaymentsTableSQL = "CREATE TABLE IF NOT EXISTS order_payments (" +
                    "id SERIAL PRIMARY KEY," +
                    "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE," +
                    "method VARCHAR(20) NOT NULL," +
                    "provider VARCHAR(50)," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'pending'," +
                    "amount DECIMAL(12, 2) NOT NULL," +
                    "currency VARCHAR(10) DEFAULT 'VND'," +
                    "transaction_code VARCHAR(120)," +
                    "paid_at TIMESTAMP," +
                    "metadata JSONB," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.execute(createOrderPaymentsTableSQL);
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_order_payments_transaction ON order_payments(transaction_code) WHERE transaction_code IS NOT NULL");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_payments_order ON order_payments(order_id)");
                ensureOrderPaymentsSchema(conn);

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
                ensureBookFavoritesSchema(conn);

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
            }

            ensurePasswordHashColumn(conn);
            BookDataLoader.seedBooksIfEmpty(conn);
            BookDataLoader.refreshBookAssets(conn);
            ensureMinimumStockLevels(conn);
            ensureCouponSchema(conn);
            ensureSampleCoupons(conn);
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

    private static void ensureOrdersSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "orders", "order_date")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "orders", "total_amount")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0");
            }
            if (!columnExists(conn, "orders", "status")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN status VARCHAR(20) DEFAULT 'new'");
            }
            if (!columnExists(conn, "orders", "payment_status")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'unpaid'");
            }
            stmt.execute("UPDATE orders SET payment_status = COALESCE(payment_status, 'unpaid')");

            if (!columnExists(conn, "orders", "payment_provider")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN payment_provider VARCHAR(30)");
            }
            if (!columnExists(conn, "orders", "shipping_address_id")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN shipping_address_id INTEGER REFERENCES user_addresses(id) ON DELETE SET NULL");
            }
            if (!columnExists(conn, "orders", "shipping_snapshot")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN shipping_snapshot JSONB");
            }
            if (!columnExists(conn, "orders", "cart_snapshot")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN cart_snapshot JSONB");
            }
            if (!columnExists(conn, "orders", "payment_metadata")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN payment_metadata JSONB");
            }
            if (!columnExists(conn, "orders", "items_subtotal")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN items_subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0");
            }
            stmt.execute("UPDATE orders SET items_subtotal = COALESCE(items_subtotal, total_amount)");

            if (!columnExists(conn, "orders", "discount_amount")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0");
            }
            stmt.execute("UPDATE orders SET discount_amount = COALESCE(discount_amount, 0)");

            if (!columnExists(conn, "orders", "shipping_fee")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0");
            }
            stmt.execute("UPDATE orders SET shipping_fee = COALESCE(shipping_fee, 0)");

            if (!columnExists(conn, "orders", "currency")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN currency VARCHAR(10) DEFAULT 'VND'");
            }
            stmt.execute("UPDATE orders SET currency = COALESCE(currency, 'VND')");

            if (!columnExists(conn, "orders", "coupon_code")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(50)");
            }
            if (!columnExists(conn, "orders", "coupon_snapshot")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN coupon_snapshot JSONB");
            }

            if (!columnExists(conn, "orders", "notes")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN notes TEXT");
            }
            if (!columnExists(conn, "orders", "created_at")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "orders", "updated_at")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "orders", "code")) {
                stmt.execute("ALTER TABLE orders ADD COLUMN code VARCHAR(40)");
            }
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_code ON orders(code)");
            stmt.execute("UPDATE orders SET code = CONCAT('ORD', LPAD(CAST(id AS TEXT), 6, '0')) WHERE code IS NULL OR code = ''");
            normalizeOrderColumnTypes(conn);
            ensureOrderRelatedEnums(conn);
            ensureOrderConstraints(conn);
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile orders schema: " + ex.getMessage());
        }
    }

    private static void ensureOrderItemsSchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "order_items", "created_at")) {
                stmt.execute("ALTER TABLE order_items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "order_items", "unit_price")) {
                stmt.execute("ALTER TABLE order_items ADD COLUMN unit_price DECIMAL(12, 2) NOT NULL DEFAULT 0");
            }
            if (!columnExists(conn, "order_items", "total_price")) {
                stmt.execute("ALTER TABLE order_items ADD COLUMN total_price DECIMAL(12, 2) NOT NULL DEFAULT 0");
                stmt.execute("UPDATE order_items SET total_price = COALESCE(total_price, quantity * unit_price)");
            }
            stmt.execute("UPDATE order_items SET unit_price = COALESCE(unit_price, 0)");
            stmt.execute("UPDATE order_items SET total_price = COALESCE(total_price, quantity * unit_price)");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile order_items schema: " + ex.getMessage());
        }
    }

    private static void ensureOrderStatusHistorySchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "order_status_history", "created_by")) {
                stmt.execute("ALTER TABLE order_status_history ADD COLUMN created_by VARCHAR(100)");
            }
            if (!columnExists(conn, "order_status_history", "created_at")) {
                stmt.execute("ALTER TABLE order_status_history ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            stmt.execute("UPDATE order_status_history SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP)");
            stmt.execute("UPDATE order_status_history SET status = COALESCE(status, 'new')");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile order_status_history schema: " + ex.getMessage());
        }
    }

    private static void ensureCouponSchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            String createCouponsSql = "CREATE TABLE IF NOT EXISTS coupon_codes (" +
                "id SERIAL PRIMARY KEY," +
                "code VARCHAR(50) UNIQUE NOT NULL," +
                "description TEXT," +
                "coupon_type VARCHAR(20) NOT NULL," +
                "value DECIMAL(10, 2) NOT NULL," +
                "max_discount DECIMAL(10, 2)," +
                "minimum_order DECIMAL(10, 2) DEFAULT 0," +
                "usage_limit INTEGER," +
                "per_user_limit INTEGER," +
                "start_date TIMESTAMP," +
                "end_date TIMESTAMP," +
                "status VARCHAR(20) DEFAULT 'active'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createCouponsSql);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_coupon_codes_status ON coupon_codes(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_coupon_codes_date ON coupon_codes(start_date, end_date)");

            String createUserCouponsSql = "CREATE TABLE IF NOT EXISTS user_coupons (" +
                "id SERIAL PRIMARY KEY," +
                "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                "coupon_id INTEGER NOT NULL REFERENCES coupon_codes(id) ON DELETE CASCADE," +
                "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "redeemed_at TIMESTAMP," +
                "usage_count INTEGER DEFAULT 0," +
                "status VARCHAR(20) DEFAULT 'available'," +
                "CONSTRAINT uq_user_coupons UNIQUE (user_id, coupon_id)" +
                ")";
            stmt.execute(createUserCouponsSql);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_coupons_user ON user_coupons(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_coupons_status ON user_coupons(status)");

            String createShopCouponsSql = "CREATE TABLE IF NOT EXISTS shop_coupons (" +
                "id SERIAL PRIMARY KEY," +
                "shop_id INTEGER NOT NULL REFERENCES shops(id) ON DELETE CASCADE," +
                "code VARCHAR(60) NOT NULL," +
                "description TEXT," +
                "discount_type VARCHAR(20) NOT NULL DEFAULT 'percentage'," +
                "discount_value DECIMAL(10, 2) NOT NULL," +
                "minimum_order DECIMAL(10, 2) DEFAULT 0," +
                "usage_limit INTEGER," +
                "used_count INTEGER DEFAULT 0," +
                "start_date TIMESTAMP," +
                "end_date TIMESTAMP," +
                "status VARCHAR(20) DEFAULT 'active'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createShopCouponsSql);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shop_coupons_shop ON shop_coupons(shop_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shop_coupons_status ON shop_coupons(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shop_coupons_date ON shop_coupons(start_date, end_date)");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_shop_coupons_code ON shop_coupons(shop_id, code)");

            String createOrderCouponsSql = "CREATE TABLE IF NOT EXISTS order_coupons (" +
                "id SERIAL PRIMARY KEY," +
                "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE," +
                "coupon_id INTEGER REFERENCES coupon_codes(id) ON DELETE SET NULL," +
                "code VARCHAR(50) NOT NULL," +
                "discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0," +
                "snapshot JSONB," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createOrderCouponsSql);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_coupons_order ON order_coupons(order_id)");

            ensureCouponColumns(conn);
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to ensure coupon schema: " + ex.getMessage());
        }
    }

    private static void ensureOrderPaymentsSchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "order_payments", "provider")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN provider VARCHAR(50)");
            }
            if (!columnExists(conn, "order_payments", "currency")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN currency VARCHAR(10) DEFAULT 'VND'");
                stmt.execute("UPDATE order_payments SET currency = 'VND' WHERE currency IS NULL");
            }
            if (!columnExists(conn, "order_payments", "transaction_code")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN transaction_code VARCHAR(120)");
            }
            if (!columnExists(conn, "order_payments", "paid_at")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN paid_at TIMESTAMP");
            }
            if (!columnExists(conn, "order_payments", "metadata")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN metadata JSONB");
            }
            if (!columnExists(conn, "order_payments", "created_at")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "order_payments", "updated_at")) {
                stmt.execute("ALTER TABLE order_payments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            stmt.execute("UPDATE order_payments SET method = COALESCE(method, 'cod')");
            stmt.execute("UPDATE order_payments SET status = COALESCE(status, 'pending')");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile order_payments schema: " + ex.getMessage());
        }
    }

    private static void ensureRecentViewsSchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "user_recent_views", "viewed_at")) {
                stmt.execute("ALTER TABLE user_recent_views ADD COLUMN viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            stmt.execute("UPDATE user_recent_views SET viewed_at = COALESCE(viewed_at, CURRENT_TIMESTAMP)");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile user_recent_views schema: " + ex.getMessage());
        }
    }

    private static void ensureBookFavoritesSchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "book_favorites", "created_at")) {
                stmt.execute("ALTER TABLE book_favorites ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            stmt.execute("UPDATE book_favorites SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP)");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile book_favorites schema: " + ex.getMessage());
        }
    }

    private static void ensureBookReviewsSchema(Connection conn) {
        if (conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "book_reviews", "updated_at")) {
                stmt.execute("ALTER TABLE book_reviews ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "book_reviews", "status")) {
                stmt.execute("ALTER TABLE book_reviews ADD COLUMN status VARCHAR(20) DEFAULT 'published'");
            }
            stmt.execute("UPDATE book_reviews SET updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP)");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile book_reviews schema: " + ex.getMessage());
        }
    }

    private static void ensureCouponColumns(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "coupon_codes", "coupon_type")) {
                stmt.execute("ALTER TABLE coupon_codes ADD COLUMN coupon_type VARCHAR(20) NOT NULL DEFAULT 'fixed'");
            }
            if (!columnExists(conn, "coupon_codes", "status")) {
                stmt.execute("ALTER TABLE coupon_codes ADD COLUMN status VARCHAR(20) DEFAULT 'active'");
            }
            if (!columnExists(conn, "coupon_codes", "updated_at")) {
                stmt.execute("ALTER TABLE coupon_codes ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columnExists(conn, "user_coupons", "status")) {
                stmt.execute("ALTER TABLE user_coupons ADD COLUMN status VARCHAR(20) DEFAULT 'available'");
            }
            if (!columnExists(conn, "user_coupons", "usage_count")) {
                stmt.execute("ALTER TABLE user_coupons ADD COLUMN usage_count INTEGER DEFAULT 0");
            }
        }
    }

    private static void ensureOrderRelatedEnums(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            ensureEnumValue(conn, "order_status", "new");
            ensureEnumValue(conn, "order_status", "confirmed");
            ensureEnumValue(conn, "order_status", "shipping");
            ensureEnumValue(conn, "order_status", "delivered");
            ensureEnumValue(conn, "order_status", "cancelled");
            ensureEnumValue(conn, "order_status", "returned");

            ensureEnumValue(conn, "payment_status", "unpaid");
            ensureEnumValue(conn, "payment_status", "processing");
            ensureEnumValue(conn, "payment_status", "paid");
            ensureEnumValue(conn, "payment_status", "failed");
            ensureEnumValue(conn, "payment_status", "refunded");

            ensureEnumValue(conn, "payment_method", "cod");
            ensureEnumValue(conn, "payment_method", "vnpay");
            ensureEnumValue(conn, "payment_method", "momo");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to reconcile order enums: " + ex.getMessage());
        }
    }

    private static void ensureMinimumStockLevels(Connection conn) {
        if (conn == null) {
            return;
        }
        String sql = "UPDATE books SET stock_quantity = ? WHERE stock_quantity IS NULL OR stock_quantity <= ?";
        try {
            if (!columnExists(conn, "books", "stock_quantity")) {
                return;
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to inspect book stock column: " + ex.getMessage());
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 100);
            stmt.setInt(2, 0);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("DBUtil - Refilled stock for " + updated + " book(s).");
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to ensure minimum stock levels: " + ex.getMessage());
        }
    }

    private static void ensureSampleCoupons(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!columnExists(conn, "coupon_codes", "code")) {
                return;
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to inspect coupon schema: " + ex.getMessage());
            return;
        }

        String freeShipSql = "INSERT INTO coupon_codes (code, description, coupon_type, value, max_discount, minimum_order, usage_limit, per_user_limit, start_date, end_date, status) "
            + "VALUES (?, ?, 'fixed', ?, NULL, 0, NULL, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '30 days', 'active') "
            + "ON CONFLICT (code) DO UPDATE SET description = EXCLUDED.description, coupon_type = EXCLUDED.coupon_type, value = EXCLUDED.value, max_discount = EXCLUDED.max_discount, minimum_order = EXCLUDED.minimum_order, status = 'active', updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement stmt = conn.prepareStatement(freeShipSql)) {
            stmt.setString(1, "FREESHIP");
            stmt.setString(2, "Miễn phí vận chuyển cho đơn hàng bất kỳ (tối đa 26.000đ).");
            stmt.setBigDecimal(3, BigDecimal.valueOf(26000));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to seed FREESHIP coupon: " + ex.getMessage());
            return;
        }

        Long couponId = null;
        String couponIdSql = "SELECT id FROM coupon_codes WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(couponIdSql)) {
            stmt.setString(1, "FREESHIP");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    couponId = rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to fetch FREESHIP coupon id: " + ex.getMessage());
            return;
        }

        if (couponId == null) {
            return;
        }

        try {
            if (!tableExists(conn, "user_coupons")) {
                return;
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to inspect user_coupons table: " + ex.getMessage());
            return;
        }

        String assignSql = "INSERT INTO user_coupons (user_id, coupon_id, status) "
            + "SELECT u.id, ?, 'available' FROM users u "
            + "WHERE NOT EXISTS (SELECT 1 FROM user_coupons uc WHERE uc.user_id = u.id AND uc.coupon_id = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(assignSql)) {
            stmt.setLong(1, couponId);
            stmt.setLong(2, couponId);
            int assigned = stmt.executeUpdate();
            if (assigned > 0) {
                System.out.println("DBUtil - Assigned FREESHIP coupon to " + assigned + " user(s).");
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to assign FREESHIP coupon: " + ex.getMessage());
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

    private static void normalizeOrderColumnTypes(Connection conn) {
        if (conn == null) {
            return;
        }
        coerceColumnToVarchar(conn, "orders", "status", 20, "'new'");
        coerceColumnToVarchar(conn, "orders", "payment_status", 20, "'unpaid'");
        coerceColumnToVarchar(conn, "orders", "payment_method", 20, "'cod'");
        coerceColumnToVarchar(conn, "order_status_history", "status", 20, null);
        coerceColumnToVarchar(conn, "order_payments", "method", 20, "'cod'");
        coerceColumnToVarchar(conn, "order_payments", "status", 20, "'pending'");
    }

    private static void ensureOrderConstraints(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            dropConstraintIfExists(conn, "orders", "chk_orders_status_text");
            addCheckConstraint(conn, "orders", "chk_orders_status_text", "status IN ('new', 'confirmed', 'shipping', 'delivered', 'failed', 'cancelled', 'returned')");
            addCheckConstraint(conn, "orders", "chk_orders_payment_status_text", "payment_status IN ('unpaid', 'processing', 'paid', 'failed', 'refunded')");
            addCheckConstraint(conn, "orders", "chk_orders_payment_method_text", "payment_method IN ('cod', 'vnpay', 'momo')");
            addCheckConstraint(conn, "order_payments", "chk_order_payments_method_text", "method IN ('cod', 'vnpay', 'momo')");
            addCheckConstraint(conn, "order_payments", "chk_order_payments_status_text", "status IN ('pending', 'processing', 'paid', 'failed', 'refunded')");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to ensure order constraints: " + ex.getMessage());
        }
    }

    private static void coerceColumnToVarchar(Connection conn, String table, String column, int length, String defaultValue) {
        try {
            if (!columnExists(conn, table, column)) {
                return;
            }
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to inspect column " + table + "." + column + ": " + ex.getMessage());
            return;
        }
        String typeSql = "ALTER TABLE " + table + " ALTER COLUMN " + column + " TYPE VARCHAR(" + length + ") USING " + column + "::text";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(typeSql);
        } catch (SQLException ex) {
            if (!isRedundantAlterError(ex)) {
                System.err.println("DBUtil - Unable to alter column type for " + table + "." + column + ": " + ex.getMessage());
            }
        }
        if (defaultValue != null) {
            String defaultSql = "ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DEFAULT " + defaultValue;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(defaultSql);
            } catch (SQLException ex) {
                if (!isRedundantAlterError(ex)) {
                    System.err.println("DBUtil - Unable to set default for " + table + "." + column + ": " + ex.getMessage());
                }
            }
        }
    }

    private static void addCheckConstraint(Connection conn, String table, String constraintName, String expression) throws SQLException {
        if (constraintExists(conn, table, constraintName)) {
            return;
        }
        String sql = "ALTER TABLE " + table + " ADD CONSTRAINT " + constraintName + " CHECK (" + expression + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void dropConstraintIfExists(Connection conn, String table, String constraintName) throws SQLException {
        String sql = "ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + constraintName;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static boolean constraintExists(Connection conn, String table, String constraintName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.table_constraints WHERE table_name = ? AND constraint_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, table);
            stmt.setString(2, constraintName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean isRedundantAlterError(SQLException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("already") || message.contains("is of type");
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        if (conn == null) {
            return false;
        }
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean enumTypeExists(Connection conn, String typeName) throws SQLException {
        if (conn == null) {
            return false;
        }
        String sql = "SELECT 1 FROM pg_type WHERE typname = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, typeName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void ensureEnumValue(Connection conn, String typeName, String value) throws SQLException {
        if (!enumTypeExists(conn, typeName)) {
            return;
        }
        String existsSql = "SELECT 1 FROM pg_enum e JOIN pg_type t ON e.enumtypid = t.oid WHERE t.typname = ? AND e.enumlabel = ?";
        try (PreparedStatement stmt = conn.prepareStatement(existsSql)) {
            stmt.setString(1, typeName);
            stmt.setString(2, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        if (!typeName.matches("[a-zA-Z0-9_]+")) {
            throw new SQLException("Invalid enum type name: " + typeName);
        }
        String sanitizedValue = value.replace("'", "''");
        String alterSql = "DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_enum e JOIN pg_type t ON e.enumtypid = t.oid "
            + "WHERE t.typname = '" + typeName + "' AND e.enumlabel = '" + sanitizedValue + "') THEN "
            + "ALTER TYPE " + typeName + " ADD VALUE '" + sanitizedValue + "'; END IF; END $$;";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(alterSql);
        }
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
        Connection connection = DriverManager.getConnection(url, username, password);
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET client_encoding TO 'UTF8'");
        } catch (SQLException ex) {
            System.err.println("DBUtil - Unable to enforce UTF-8 client encoding: " + ex.getMessage());
        }
        return connection;
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

    public static String getEmailByUsername(String username) throws SQLException {
        String sql = "SELECT email FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
                return null;
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

    // utils/DBUtil.java
    public static String getUserRole(String username) throws SQLException {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String r = rs.getString("role");
                    return (r == null || r.isBlank()) ? "user" : r.trim().toLowerCase();
                }
            }
        }
        return "user";
    }
    
    /**
     * Lấy user ID theo username
     * @param username Username cần tìm
     * @return User ID hoặc -1 nếu không tìm thấy
     */
    public static int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                return -1;
            }
        }
    }

    // /**
    //  * Cập nhật role của user
    //  * @param userId User ID cần cập nhật
    //  * @param newRole Role mới (admin, seller, user)
    //  * @return true nếu cập nhật thành công
    //  */
    // public static boolean updateUserRole(int userId, String newRole) throws SQLException {
    //     String sql = "UPDATE users SET role = ? WHERE id = ?";
    //     try (Connection conn = getConnection(); 
    //          PreparedStatement pstmt = conn.prepareStatement(sql)) {
    //         pstmt.setString(1, newRole);
    //         pstmt.setInt(2, userId);
    //         return pstmt.executeUpdate() > 0;
    //     }
    // }

    public static void updateUserRole(int userId, String role, String status) throws SQLException {
    String sql = "UPDATE users SET role = ?, status = ? WHERE id = ?";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, role);
        stmt.setString(2, status);
        stmt.setInt(3, userId);
        stmt.executeUpdate();
    }
}

    /**
     * Lấy status của user theo username
     * @param username Username cần tìm
     * @return Status hoặc null nếu không tìm thấy
     */
    public static String getUserStatus(String username) throws SQLException {
        String sql = "SELECT status FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
                return null;
            }
        }
    }



   
}


