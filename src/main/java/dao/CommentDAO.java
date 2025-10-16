package dao;

import models.Comment;
import utils.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CommentDAO {
    
    /**
     * Add a new comment (with validation)
     */
    public static boolean addComment(Comment comment) {
        // Server-side validation: comment text must be at least 50 characters
        if (comment.getCommentText() == null || comment.getCommentText().trim().length() < 50) {
            throw new IllegalArgumentException("Bình luận phải có ít nhất 50 ký tự");
        }
        
        String sql = "INSERT INTO comments (book_id, user_id, comment_text, image_url, video_url, is_verified_purchase) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, comment.getBookId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getCommentText().trim());
            stmt.setString(4, comment.getImageUrl());
            stmt.setString(5, comment.getVideoUrl());
            stmt.setBoolean(6, comment.isVerifiedPurchase());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all comments for a book
     */
    public static List<Comment> getCommentsByBook(int bookId) {
        String sql = "SELECT c.id, c.book_id, c.user_id, c.comment_text, c.image_url, c.video_url, " +
                    "c.is_verified_purchase, c.created_at, c.updated_at, u.username, u.full_name " +
                    "FROM comments c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE c.book_id = ? " +
                    "ORDER BY c.is_verified_purchase DESC, c.created_at DESC";
        
        List<Comment> comments = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setBookId(rs.getInt("book_id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setCommentText(rs.getString("comment_text"));
                comment.setImageUrl(rs.getString("image_url"));
                comment.setVideoUrl(rs.getString("video_url"));
                comment.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    comment.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    comment.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                // Store username in a temp variable or skip if not needed in model
                // comment.setUserName(rs.getString("username")); // if model has this field
                
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return comments;
    }
    
    /**
     * Get comment by ID
     */
    public static Comment getCommentById(int commentId) {
        String sql = "SELECT c.id, c.book_id, c.user_id, c.comment_text, c.image_url, c.video_url, " +
                    "c.is_verified_purchase, c.created_at, c.updated_at, u.username " +
                    "FROM comments c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE c.id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setBookId(rs.getInt("book_id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setCommentText(rs.getString("comment_text"));
                comment.setImageUrl(rs.getString("image_url"));
                comment.setVideoUrl(rs.getString("video_url"));
                comment.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    comment.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    comment.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                return comment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update existing comment
     */
    public static boolean updateComment(Comment comment) {
        // Server-side validation: comment text must be at least 50 characters
        if (comment.getCommentText() == null || comment.getCommentText().trim().length() < 50) {
            throw new IllegalArgumentException("Bình luận phải có ít nhất 50 ký tự");
        }
        
        String sql = "UPDATE comments SET comment_text = ?, image_url = ?, video_url = ?, " +
                    "updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, comment.getCommentText().trim());
            stmt.setString(2, comment.getImageUrl());
            stmt.setString(3, comment.getVideoUrl());
            stmt.setInt(4, comment.getId());
            stmt.setInt(5, comment.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete comment
     */
    public static boolean deleteComment(int commentId, int userId) {
        String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
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
     * Get comment count for a book
     */
    public static int getCommentCountByBook(int bookId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE book_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
