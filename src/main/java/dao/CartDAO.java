package dao;

import models.CartItem;
import models.Book;
import utils.DBUtil;

import java.sql.*;
import java.util.*;

public class CartDAO {
    
    /**
     * Get all items in a user's cart with book details.
     * This is a convenience method that handles getting/creating the cart.
     */
    public static List<CartItem> getCartItems(int userId) {
        int cartId = getOrCreateCart(userId);
        if (cartId == -1) {
            return new ArrayList<>(); // Return empty list if cart creation fails
        }
        
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
     * Add an item to a user's cart or update quantity if it already exists.
     */
    public static boolean addToCart(int userId, int bookId, int quantity) {
        int cartId = getOrCreateCart(userId);
        if (cartId == -1) {
            return false;
        }

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
                return updateCartItemQuantity(itemId, currentQty + quantity);
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update a cart item's quantity.
     */
    public static boolean updateCartItemQuantity(int cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeCartItem(cartItemId, 0); // userId is not strictly needed here but good for consistency
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove an item from the cart, ensuring it belongs to the user.
     */
    public static boolean removeCartItem(int cartItemId, int userId) {
        String sql = "DELETE FROM cart_items WHERE id = ? AND cart_id = (SELECT id FROM shopping_cart WHERE user_id = ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartItemId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get or create a shopping cart for a user.
     */
    private static int getOrCreateCart(int userId) {
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

        // Create new cart if it doesn't exist
        String insertSql = "INSERT INTO shopping_cart (user_id, created_at, updated_at) VALUES (?, NOW(), NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicates failure
    }
    
    /**
     * Clear all items from a user's cart.
     */
    public static boolean clearCart(int userId) {
        int cartId = getOrCreateCart(userId);
        if (cartId == -1) {
            return false;
        }
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the total number of items in a user's cart.
     */
    public static int getCartItemCount(int userId) {
        int cartId = getOrCreateCart(userId);
        if (cartId == -1) {
            return 0;
        }
        String sql = "SELECT SUM(quantity) as total_items FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_items");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
