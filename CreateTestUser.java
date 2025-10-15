import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class CreateTestUser {
    public static void main(String[] args) {
        String url = System.getenv("DATABASE_URL");
        if (url == null) {
            System.out.println("DATABASE_URL not found");
            return;
        }
        
        try {
            // Parse DATABASE_URL for Heroku
            java.net.URI dbUri = new java.net.URI(url);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + 
                            (dbUri.getPort() != -1 ? ":" + dbUri.getPort() : "") + 
                            dbUri.getPath() + "?sslmode=require";
            
            // Create test user
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                
                // Check if test user already exists
                String checkSql = "SELECT id FROM users WHERE username = 'testuser'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Test user already exists!");
                            return;
                        }
                    }
                }
                
                // Create test user
                String insertSql = "INSERT INTO users (username, email, password_hash, full_name, email_verified) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setString(1, "testuser");
                    stmt.setString(2, "test@example.com");
                    stmt.setString(3, BCrypt.hashpw("123456", BCrypt.gensalt()));
                    stmt.setString(4, "Test User");
                    stmt.setBoolean(5, true); // Set as verified
                    
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        System.out.println("Test user created successfully!");
                        System.out.println("Username: testuser");
                        System.out.println("Password: 123456");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}