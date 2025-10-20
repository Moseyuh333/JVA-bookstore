package utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

@WebListener
public class VendorContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Ensure vendor-related tables exist
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS stores (" +
                    "id SERIAL PRIMARY KEY, " +
                    "owner_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                    "name VARCHAR(255), " +
                    "description TEXT, " +
                    "avatar_url VARCHAR(500), " +
                    "cover_url VARCHAR(500), " +
                    "featured_images JSONB, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS store_employees (" +
                    "id SERIAL PRIMARY KEY, " +
                    "store_id INTEGER NOT NULL REFERENCES stores(id) ON DELETE CASCADE, " +
                    "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                    "role VARCHAR(50) DEFAULT 'staff', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE(store_id, user_id)" +
                    ")");
                    stmt.execute("CREATE TABLE IF NOT EXISTS store_products (" +
                    "id SERIAL PRIMARY KEY, " +
                    "store_id INTEGER REFERENCES stores(id) ON DELETE CASCADE, " +
                    "book_id INTEGER REFERENCES books(id), " +
                    "price DECIMAL(12,2), " +
                    "stock INTEGER DEFAULT 0, " +
                    "status VARCHAR(20) DEFAULT 'active', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS store_wallets (" +
                    "store_id INTEGER PRIMARY KEY REFERENCES stores(id) ON DELETE CASCADE, " +
                    "balance DECIMAL(14,2) DEFAULT 0, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS store_wallet_transactions (" +
                    "id SERIAL PRIMARY KEY, " +
                    "store_id INTEGER NOT NULL REFERENCES stores(id) ON DELETE CASCADE, " +
                    "amount DECIMAL(14,2) NOT NULL, " +
                    "type VARCHAR(20), " +
                    "description TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS store_orders (" +
                    "id SERIAL PRIMARY KEY, " +
                    "store_id INTEGER NOT NULL REFERENCES stores(id) ON DELETE CASCADE, " +
                    "order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE, " +
                    "status VARCHAR(20), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

        } catch (SQLException ex) {
            System.err.println("VendorContextListener - Failed to ensure vendor schema: " + ex.getMessage());
            }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing
    }
}