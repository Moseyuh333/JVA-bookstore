package dao;

import models.Book;
import utils.DBUtil;

import java.sql.*;
import java.util.*;

public class WishlistDAO {
    
    /**
     * Add book to wishlist
     */
    public static boolean addToWishlist(int userId, int bookId) {
        // Check if already in wishlist
        if (isInWishlist(userId, bookId)) {
            return true; // Already in wishlist
        }
        
        String sql = "INSERT INTO wishlist (user_id, book_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Remove book from wishlist
     */
    public static boolean removeFromWishlist(int userId, int bookId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all books in user's wishlist
     */
    public static List<Book> getWishlist(int userId) {
        String sql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.description, " +
                    "b.category, b.stock_quantity, b.image_url, b.average_rating, b.rating_count, " +
                    "b.views_count, b.sales_count, b.created_at, b.updated_at, w.added_at " +
                    "FROM wishlist w " +
                    "JOIN books b ON w.book_id = b.id " +
                    "WHERE w.user_id = ? " +
                    "ORDER BY w.added_at DESC";
        
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setDescription(rs.getString("description"));
                book.setCategory(rs.getString("category"));
                book.setStockQuantity(rs.getInt("stock_quantity"));
                book.setImageUrl(rs.getString("image_url"));
                book.setAverageRating(rs.getDouble("average_rating"));
                book.setRatingCount(rs.getInt("rating_count"));
                book.setViewsCount(rs.getInt("views_count"));
                book.setSalesCount(rs.getInt("sales_count"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    book.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    book.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return books;
    }
    
    /**
     * Check if book is in user's wishlist
     */
    public static boolean isInWishlist(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get wishlist count for user
     */
    public static int getWishlistCount(int userId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Clear entire wishlist for user
     */
    public static boolean clearWishlist(int userId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
