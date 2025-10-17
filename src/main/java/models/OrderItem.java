package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderItem {
    private final long id;
    private final long orderId;
    private final long bookId;
    private final String bookTitle;
    private final String bookAuthor;
    private final String bookImageUrl;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalPrice;
    private final LocalDateTime createdAt;

    public OrderItem(long id,
                     long orderId,
                     long bookId,
                     String bookTitle,
                     String bookAuthor,
                     String bookImageUrl,
                     int quantity,
                     BigDecimal unitPrice,
                     BigDecimal totalPrice,
                     LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookImageUrl = bookImageUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.totalPrice = totalPrice != null ? totalPrice : this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getOrderId() {
        return orderId;
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

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
