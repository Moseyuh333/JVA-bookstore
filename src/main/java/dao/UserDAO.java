package dao;

import utils.DBUtil;

import java.sql.*;

public class UserDAO {
    
    /**
     * Get user by ID
     */
    public static java.util.Map<String, Object> getUserById(int userId) {
        String sql = "SELECT id, username, email, full_name, phone, birth_date, address, verified " +
                    "FROM users WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                java.util.Map<String, Object> user = new java.util.HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("full_name", rs.getString("full_name"));
                user.put("phone", rs.getString("phone"));
                user.put("birth_date", rs.getDate("birth_date"));
                user.put("address", rs.getString("address"));
                user.put("verified", rs.getBoolean("verified"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get user by username
     */
    public static java.util.Map<String, Object> getUserByUsername(String username) {
        String sql = "SELECT id, username, email, full_name, phone, birth_date, address, verified " +
                    "FROM users WHERE username = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                java.util.Map<String, Object> user = new java.util.HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("full_name", rs.getString("full_name"));
                user.put("phone", rs.getString("phone"));
                user.put("birth_date", rs.getDate("birth_date"));
                user.put("address", rs.getString("address"));
                user.put("verified", rs.getBoolean("verified"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update user profile
     */
    public static boolean updateProfile(int userId, String fullName, String phone, 
                                       java.sql.Date birthDate, String address) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, birth_date = ?, address = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setDate(3, birthDate);
            stmt.setString(4, address);
            stmt.setInt(5, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update email
     */
    public static boolean updateEmail(int userId, String email) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
