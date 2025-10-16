package dao;

import models.CartItem;
import models.Book;
import utils.DBUtil;

import java.sql.*;
import java.util.*;

public class CartDAO {
    
    /**
     * Get or create shopping cart for user
     */
    public static int getOrCreateCart(int userId) {
        // First try to get existing cart
        String selectSql = "SELECT id FROM shopping_cart WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create new cart if doesn't exist
        String insertSql = "INSERT INTO shopping_cart (user_id, created_at, updated_at) VALUES (?, NOW(), NOW()) RETURNING id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Add item to cart or update quantity if already exists
     */
    public static boolean addToCart(int cartId, int bookId, int quantity) {
        // Check if item already in cart
        String checkSql = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, bookId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Update existing item
                int itemId = rs.getInt("id");
                int currentQty = rs.getInt("quantity");
                return updateCartItem(itemId, currentQty + quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add new item
        String insertSql = "INSERT INTO cart_items (cart_id, book_id, quantity, added_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update cart item quantity
     */
    public static boolean updateCartItem(int itemId, int quantity) {
        if (quantity <= 0) {
            return removeCartItem(itemId);
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove item from cart
     */
    public static boolean removeCartItem(int itemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all items in cart with book details
     */
    public static List<CartItem> getCartItems(int cartId) {
        String sql = "SELECT ci.id, ci.cart_id, ci.book_id, ci.quantity, ci.added_at, " +
                    "b.id as book_id, b.title, b.author, b.price, b.image_url, b.category " +
                    "FROM cart_items ci " +
                    "JOIN books b ON ci.book_id = b.id " +
                    "WHERE ci.cart_id = ? ORDER BY ci.added_at DESC";
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setCartId(rs.getInt("cart_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());

                // Set book data
                Book book = new Book();
                book.setId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setImageUrl(rs.getString("image_url"));
                book.setCategory(rs.getString("category"));
                item.setBook(book);

                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Clear all items from cart
     */
    public static boolean clearCart(int cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get cart item count
     */
    public static int getCartItemCount(int cartId) {
        String sql = "SELECT COUNT(*) as count FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get specific cart item
     */
    public static CartItem getCartItem(int itemId) {
        String sql = "SELECT ci.id, ci.cart_id, ci.book_id, ci.quantity, ci.added_at " +
                    "FROM cart_items ci WHERE ci.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setCartId(rs.getInt("cart_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                
                // Load book details
                Book book = BookDAO.getById(item.getBookId());
                item.setBook(book);
                
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
