package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class CartItem {
    private final long id;
    private final long cartId;
    private final long bookId;
    private final String bookTitle;
    private final String bookAuthor;
    private final String bookImageUrl;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CartItem(long id,
                    long cartId,
                    long bookId,
                    String bookTitle,
                    String bookAuthor,
                    String bookImageUrl,
                    BigDecimal unitPrice,
                    int quantity,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.cartId = cartId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookImageUrl = bookImageUrl;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.quantity = Math.max(quantity, 0);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public long getCartId() {
        return cartId;
    }

    public long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public String getBookImageUrl() {
        return bookImageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CartItem)) {
            return false;
        }
        CartItem cartItem = (CartItem) o;
        return id == cartItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
