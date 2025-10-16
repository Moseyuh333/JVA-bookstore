package dao;

import models.Book;
import utils.DBUtil;

import java.sql.*;
import java.util.*;

public class ProductViewDAO {
    
    /**
     * Record a product view
     */
    public static boolean recordView(int userId, int bookId) {
        String sql = "INSERT INTO product_views (user_id, book_id, viewed_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        
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
     * Get recently viewed products for user
     */
    public static List<Book> getRecentlyViewed(int userId, int limit) {
        String sql = "SELECT DISTINCT ON (b.id) b.id, b.title, b.author, b.isbn, b.price, " +
                    "b.description, b.category, b.stock_quantity, b.image_url, b.average_rating, " +
                    "b.rating_count, b.views_count, b.sales_count, b.created_at, b.updated_at, " +
                    "pv.viewed_at " +
                    "FROM product_views pv " +
                    "JOIN books b ON pv.book_id = b.id " +
                    "WHERE pv.user_id = ? " +
                    "ORDER BY b.id, pv.viewed_at DESC " +
                    "LIMIT ?";
        
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
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
     * Clear view history for user
     */
    public static boolean clearViewHistory(int userId) {
        String sql = "DELETE FROM product_views WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get view count for user
     */
    public static int getViewCount(int userId) {
        String sql = "SELECT COUNT(DISTINCT book_id) FROM product_views WHERE user_id = ?";
        
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
     * Clear old view history (older than X days)
     */
    public static boolean clearOldViews(int userId, int daysOld) {
        String sql = "DELETE FROM product_views WHERE user_id = ? AND viewed_at < (CURRENT_TIMESTAMP - INTERVAL '" + daysOld + " days')";
        
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
