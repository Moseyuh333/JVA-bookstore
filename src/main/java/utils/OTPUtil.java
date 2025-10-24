package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;

public class OTPUtil {
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    
    /**
     * Generate a random 6-digit OTP
     */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6 digits
        return String.valueOf(otp);
    }
    
    /**
     * Store OTP in database
     */
    public static boolean storeOTP(String email, String otp) throws SQLException {
        // First, delete any existing OTP for this email
        deleteOTPByEmail(email);
        
        String sql = "INSERT INTO otp_verifications (email, otp_code, expires_at) VALUES (?, ?, NOW() + INTERVAL '" + OTP_VALIDITY_MINUTES + " minutes')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, otp);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Verify OTP
     */
    public static boolean verifyOTP(String email, String otp) throws SQLException {
        String sql = "SELECT id, otp_code, expires_at, verified, attempts FROM otp_verifications " +
                     "WHERE email = ? AND verified = FALSE ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String storedOTP = rs.getString("otp_code");
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    int attempts = rs.getInt("attempts");
                    
                    // Check if OTP has expired
                    if (expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
                        System.out.println("OTP expired for email: " + email);
                        return false;
                    }
                    
                    // Check max attempts
                    if (attempts >= MAX_ATTEMPTS) {
                        System.out.println("Max OTP attempts reached for email: " + email);
                        return false;
                    }
                    
                    // Increment attempts
                    incrementAttempts(id);
                    
                    // Verify OTP
                    if (storedOTP.equals(otp)) {
                        markOTPAsVerified(id);
                        System.out.println("OTP verified successfully for email: " + email);
                        return true;
                    }
                    
                    System.out.println("Invalid OTP for email: " + email);
                    return false;
                }
            }
        }
        
        System.out.println("No OTP found for email: " + email);
        return false;
    }
    
    /**
     * Check if email can request new OTP (2 minutes cooldown)
     */
    public static boolean canRequestNewOTP(String email) throws SQLException {
        String sql = "SELECT created_at FROM otp_verifications " +
                     "WHERE email = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp lastCreated = rs.getTimestamp("created_at");
                    long diffMinutes = (System.currentTimeMillis() - lastCreated.getTime()) / (60 * 1000);
                    return diffMinutes >= 2; // 2 minutes cooldown
                }
                return true; // No previous OTP, can send
            }
        }
    }
    
    /**
     * Get remaining cooldown seconds
     */
    public static long getRemainingCooldownSeconds(String email) throws SQLException {
        String sql = "SELECT created_at FROM otp_verifications " +
                     "WHERE email = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp lastCreated = rs.getTimestamp("created_at");
                    long diffSeconds = (System.currentTimeMillis() - lastCreated.getTime()) / 1000;
                    long cooldownSeconds = 120; // 2 minutes
                    long remaining = cooldownSeconds - diffSeconds;
                    return remaining > 0 ? remaining : 0;
                }
                return 0;
            }
        }
    }
    
    private static void incrementAttempts(int id) throws SQLException {
        String sql = "UPDATE otp_verifications SET attempts = attempts + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private static void markOTPAsVerified(int id) throws SQLException {
        String sql = "UPDATE otp_verifications SET verified = TRUE WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private static void deleteOTPByEmail(String email) throws SQLException {
        String sql = "DELETE FROM otp_verifications WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        }
    }
}
