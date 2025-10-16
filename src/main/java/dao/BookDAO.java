package dao;

import models.Book;
import utils.DBUtil;

import java.sql.*;
import java.util.*;

public class BookDAO {
    
    /**
     * Get newest books for homepage
     */
    public static List<Book> getNewestBooks(int limit, int offset) {
        String sql = "SELECT id, title, author, price, category, stock_quantity, image_url, " +
                    "average_rating, rating_count FROM books ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return queryBooks(sql, new Object[]{limit, offset});
    }

    /**
     * Get best-selling books for homepage
     */
    public static List<Book> getBestSellingBooks(int limit, int offset) {
        String sql = "SELECT id, title, author, price, category, stock_quantity, image_url, " +
                    "average_rating, rating_count FROM books ORDER BY sales_count DESC LIMIT ? OFFSET ?";
        return queryBooks(sql, new Object[]{limit, offset});
    }

    /**
     * Get top-rated books for homepage
     */
    public static List<Book> getTopRatedBooks(int limit, int offset) {
        String sql = "SELECT id, title, author, price, category, stock_quantity, image_url, " +
                    "average_rating, rating_count FROM books WHERE rating_count > 0 " +
                    "ORDER BY average_rating DESC, rating_count DESC LIMIT ? OFFSET ?";
        return queryBooks(sql, new Object[]{limit, offset});
    }

    /**
     * Get favorite books (most wishlisted)
     */
    public static List<Book> getFavoriteBooks(int limit, int offset) {
        String sql = "SELECT b.id, b.title, b.author, b.price, b.category, b.stock_quantity, " +
                    "b.image_url, b.average_rating, b.rating_count " +
                    "FROM books b LEFT JOIN wishlist w ON b.id = w.book_id " +
                    "GROUP BY b.id ORDER BY COUNT(w.id) DESC LIMIT ? OFFSET ?";
        return queryBooks(sql, new Object[]{limit, offset});
    }

    /**
     * Search books by keyword
     */
    public static List<Book> searchBooks(String keyword, int limit, int offset) {
        String sql = "SELECT id, title, author, price, category, stock_quantity, image_url, " +
                    "average_rating, rating_count FROM books " +
                    "WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(description) LIKE ? " +
                    "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        String pattern = "%" + keyword.toLowerCase() + "%";
        return queryBooks(sql, new Object[]{pattern, pattern, pattern, limit, offset});
    }

    /**
     * Get books by category
     */
    public static List<Book> getByCategory(String category, String sortBy, int limit, int offset) {
        String orderBy = "created_at DESC"; // default
        if ("price_asc".equals(sortBy)) {
            orderBy = "price ASC";
        } else if ("price_desc".equals(sortBy)) {
            orderBy = "price DESC";
        } else if ("rating".equals(sortBy)) {
            orderBy = "average_rating DESC, rating_count DESC";
        } else if ("best_selling".equals(sortBy)) {
            orderBy = "sales_count DESC";
        }

        String sql = "SELECT id, title, author, price, category, stock_quantity, image_url, " +
                    "average_rating, rating_count FROM books WHERE category = ? " +
                    "ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
        return queryBooks(sql, new Object[]{category, limit, offset});
    }

    /**
     * Get book by ID with full details
     */
    public static Book getById(int bookId) {
        String sql = "SELECT id, title, author, isbn, price, description, category, " +
                    "stock_quantity, image_url, average_rating, rating_count, views_count, " +
                    "sales_count, created_at, updated_at FROM books WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all categories
     */
    public static List<String> getAllCategories() {
        String sql = "SELECT DISTINCT category FROM books WHERE category IS NOT NULL ORDER BY category";
        List<String> categories = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Get total books count
     */
    public static int getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM books";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get total books count in category
     */
    public static int getCategoryCount(String category) {
        String sql = "SELECT COUNT(*) as total FROM books WHERE category = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Increment views count for a book
     */
    public static void incrementViewsCount(int bookId) {
        String sql = "UPDATE books SET views_count = views_count + 1, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increment sales count for a book
     */
    public static void incrementSalesCount(int bookId, int quantity) {
        String sql = "UPDATE books SET sales_count = sales_count + ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update average rating for a book
     */
    public static void updateAverageRating(int bookId) {
        String sql = "UPDATE books SET average_rating = " +
                    "(SELECT COALESCE(AVG(rating), 0) FROM ratings WHERE book_id = ?), " +
                    "rating_count = (SELECT COUNT(*) FROM ratings WHERE book_id = ?), " +
                    "updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Private helper method to execute book queries
     */
    private static List<Book> queryBooks(String sql, Object[] params) {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String) {
                    pstmt.setString(i + 1, (String) params[i]);
                } else if (params[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params[i]);
                }
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getBigDecimal("price"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getString("image_url"),
                    rs.getDouble("average_rating"),
                    rs.getInt("rating_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /**
     * Map ResultSet to Book object
     */
    private static Book mapBook(ResultSet rs) throws SQLException {
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
        book.setCreatedAt(rs.getTimestamp("created_at") != null ? 
            rs.getTimestamp("created_at").toLocalDateTime() : null);
        book.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
            rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return book;
    }
}
