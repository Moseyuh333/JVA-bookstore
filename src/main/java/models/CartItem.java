package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItem {
    private int id;
    private int cartId;
    private int bookId;
    private int quantity;
    private LocalDateTime addedAt;
    
    // Related data (for UI convenience)
    private Book book;

    // Constructors
    public CartItem() {}

    public CartItem(int bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public CartItem(int id, int cartId, int bookId, int quantity, LocalDateTime addedAt) {
        this.id = id;
        this.cartId = cartId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    // Helper method to calculate line total
    public BigDecimal getLineTotal() {
        if (book != null && book.getPrice() != null) {
            return book.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", quantity=" + quantity +
                '}';
    }
}
