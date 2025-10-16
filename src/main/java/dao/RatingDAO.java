package dao;

import models.Rating;
import utils.DBUtil;

import java.sql.*;
import java.util.*;

public class RatingDAO {
    
    /**
     * Add a new rating
     */
    public static boolean addRating(Rating rating) {
        // Check if user already rated this book
        if (getUserRatingForBook(rating.getUserId(), rating.getBookId()) != null) {
            return updateRating(rating);
        }
        
        String sql = "INSERT INTO ratings (user_id, book_id, rating, review, is_verified_purchase) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, rating.getUserId());
            stmt.setInt(2, rating.getBookId());
            stmt.setInt(3, rating.getRating());
            stmt.setString(4, rating.getReview());
            stmt.setBoolean(5, rating.isVerifiedPurchase());
            
            boolean success = stmt.executeUpdate() > 0;
            
            if (success) {
                // Update book's average rating
                BookDAO.updateAverageRating(rating.getBookId());
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all ratings for a book
     */
    public static List<Rating> getRatingsByBook(int bookId) {
        String sql = "SELECT r.id, r.user_id, r.book_id, r.rating, r.review, r.helpful_count, " +
                    "r.is_verified_purchase, r.created_at, r.updated_at, u.username, u.full_name " +
                    "FROM ratings r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "WHERE r.book_id = ? " +
                    "ORDER BY r.is_verified_purchase DESC, r.created_at DESC";
        
        List<Rating> ratings = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setUserId(rs.getInt("user_id"));
                rating.setBookId(rs.getInt("book_id"));
                rating.setRating(rs.getInt("rating"));
                rating.setReview(rs.getString("review"));
                rating.setHelpfulCount(rs.getInt("helpful_count"));
                rating.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    rating.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    rating.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                // Add username for display (note: setUserName not setUsername)
                rating.setUserName(rs.getString("username"));
                
                ratings.add(rating);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return ratings;
    }
    
    /**
     * Get user's rating for a specific book
     */
    public static Rating getUserRatingForBook(int userId, int bookId) {
        String sql = "SELECT id, user_id, book_id, rating, review, helpful_count, " +
                    "is_verified_purchase, created_at, updated_at " +
                    "FROM ratings WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setUserId(rs.getInt("user_id"));
                rating.setBookId(rs.getInt("book_id"));
                rating.setRating(rs.getInt("rating"));
                rating.setReview(rs.getString("review"));
                rating.setHelpfulCount(rs.getInt("helpful_count"));
                rating.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    rating.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    rating.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                return rating;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update existing rating
     */
    public static boolean updateRating(Rating rating) {
        String sql = "UPDATE ratings SET rating = ?, review = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, rating.getRating());
            stmt.setString(2, rating.getReview());
            stmt.setInt(3, rating.getUserId());
            stmt.setInt(4, rating.getBookId());
            
            boolean success = stmt.executeUpdate() > 0;
            
            if (success) {
                // Update book's average rating
                BookDAO.updateAverageRating(rating.getBookId());
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete rating
     */
    public static boolean deleteRating(int ratingId) {
        // Get book_id before deleting
        String getBookIdSql = "SELECT book_id FROM ratings WHERE id = ?";
        int bookId = -1;
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getBookIdSql)) {
            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                bookId = rs.getInt("book_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String sql = "DELETE FROM ratings WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ratingId);
            boolean success = stmt.executeUpdate() > 0;
            
            if (success && bookId > 0) {
                // Update book's average rating
                BookDAO.updateAverageRating(bookId);
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if user has purchased a book (verified purchase)
     */
    public static boolean hasUserPurchasedBook(int userId, int bookId) {
        return OrderDAO.hasUserPurchasedBook(userId, bookId);
    }
    
    /**
     * Increment helpful count for a rating
     */
    public static boolean incrementHelpfulCount(int ratingId) {
        String sql = "UPDATE ratings SET helpful_count = helpful_count + 1 WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ratingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
