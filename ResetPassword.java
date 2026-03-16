import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;
import java.io.FileInputStream;

public class ResetPassword {
    public static void main(String[] args) {
        try {
            // Read database properties
            Properties props = new Properties();
            props.load(new FileInputStream("src/main/resources/db.properties"));
            
            String url = props.getProperty("db.url");
            String dbUser = props.getProperty("db.username");
            String dbPass = props.getProperty("db.password");
            
            System.out.println("Connecting to database: " + url);
            
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
                System.out.println("Connected successfully!");
                
                // Reset password for shino113399
                String username = "shino113399";
                String newPassword = "123456";
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
                
                // Check if user exists first
                String checkSql = "SELECT id, username, email, role FROM users WHERE username = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, username);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            int userId = rs.getInt("id");
                            String email = rs.getString("email");
                            String role = rs.getString("role");
                            
                            System.out.println("\nUser found:");
                            System.out.println("  ID: " + userId);
                            System.out.println("  Username: " + username);
                            System.out.println("  Email: " + email);
                            System.out.println("  Role: " + role);
                            
                            // Update password
                            String updateSql = "UPDATE users SET password_hash = ? WHERE username = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setString(1, hashedPassword);
                                updateStmt.setString(2, username);
                                
                                int result = updateStmt.executeUpdate();
                                if (result > 0) {
                                    System.out.println("\n✓ Password updated successfully!");
                                    System.out.println("  New password: " + newPassword);
                                    System.out.println("\nYou can now login with:");
                                    System.out.println("  Username: " + username);
                                    System.out.println("  Password: " + newPassword);
                                }
                            }
                        } else {
                            System.out.println("User '" + username + "' not found!");
                            
                            // Create new user
                            System.out.println("\nCreating new user...");
                            String insertSql = "INSERT INTO users (username, email, password_hash, full_name, role, status, email_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                insertStmt.setString(1, username);
                                insertStmt.setString(2, "shino@example.com");
                                insertStmt.setString(3, hashedPassword);
                                insertStmt.setString(4, "Shino User");
                                insertStmt.setString(5, "customer");
                                insertStmt.setString(6, "active");
                                insertStmt.setBoolean(7, true);
                                
                                int result = insertStmt.executeUpdate();
                                if (result > 0) {
                                    System.out.println("✓ User created successfully!");
                                    System.out.println("  Username: " + username);
                                    System.out.println("  Password: " + newPassword);
                                }
                            }
                        }
                    }
                }
                
                // Also reset admin01 password
                System.out.println("\n" + "=".repeat(50));
                System.out.println("Resetting admin01 password...");
                String updateAdminSql = "UPDATE users SET password_hash = ? WHERE username = 'admin01'";
                try (PreparedStatement stmt = conn.prepareStatement(updateAdminSql)) {
                    stmt.setString(1, BCrypt.hashpw("123456", BCrypt.gensalt(10)));
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        System.out.println("✓ admin01 password reset to: 123456");
                    }
                }
                
                // Reset seller1 password
                String updateSellerSql = "UPDATE users SET password_hash = ? WHERE username = 'seller1'";
                try (PreparedStatement stmt = conn.prepareStatement(updateSellerSql)) {
                    stmt.setString(1, BCrypt.hashpw("123456", BCrypt.gensalt(10)));
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        System.out.println("✓ seller1 password reset to: 123456");
                    }
                }
                
                System.out.println("=".repeat(50));
                
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
