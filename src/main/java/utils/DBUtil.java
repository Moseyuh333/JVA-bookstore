package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URISyntaxException;

import models.User;

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
            runSqlScript(conn, "schema.sql");
            runSqlScript(conn, "otp_schema.sql");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    private static void runSqlScript(Connection conn, String resourceName) {
        try (InputStream resource = DBUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (resource == null) {
                System.err.println("SQL resource not found: " + resourceName);
                return;
            }

            String sql = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            // Remove single line comments to avoid execution issues when splitting.
            sql = sql.replaceAll("(?m)^\\s*--.*$", "");
            String[] statements = sql.split(";\\s*");

            try (Statement stmt = conn.createStatement()) {
                for (String rawStatement : statements) {
                    String statement = rawStatement.trim();
                    if (statement.isEmpty()) {
                        continue;
                    }
                    stmt.execute(statement);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to run SQL script " + resourceName + ": " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (url == null || username == null || password == null) {
            throw new SQLException("Database configuration not initialized. Ensure DATABASE_URL env var is set or db.properties exists.");
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static void createUser(String username, String email, String passwordHash, String verificationToken) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, role, email_verified, verification_token) VALUES (?, ?, ?, ?, ?, false, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, username);
            pstmt.setString(5, "USER");
            pstmt.setString(6, verificationToken);
            pstmt.executeUpdate();
        }
    }

    public static void createUserVerified(String username, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, role, email_verified) VALUES (?, ?, ?, ?, ?, true)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, username);
            pstmt.setString(5, "USER");
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

    public static User getUserDetailsByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, full_name, phone, address, birth_date, role, email_verified, created_at FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setFullName(rs.getString("full_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setBirthDate(rs.getDate("birth_date"));
                    String role = rs.getString("role");
                    user.setRole(role != null ? role : "USER");
                    user.setEmailVerified(rs.getBoolean("email_verified"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        return null;
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
