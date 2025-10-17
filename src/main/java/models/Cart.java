package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cart {
    private final long id;
    private final Long userId;
    private final String sessionId;
    private final String status;
    private final String currency;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<CartItem> items;
    private final BigDecimal subtotal;
    private final int totalQuantity;

    public Cart(long id,
                Long userId,
                String sessionId,
                String status,
                String currency,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                List<CartItem> items) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.status = status;
        this.currency = currency != null ? currency : "VND";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        List<CartItem> safeItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.items = Collections.unmodifiableList(safeItems);

        BigDecimal runningSubtotal = BigDecimal.ZERO;
        int runningQuantity = 0;
        for (CartItem item : this.items) {
            runningSubtotal = runningSubtotal.add(item.getLineTotal());
            runningQuantity += item.getQuantity();
        }
        this.subtotal = runningSubtotal;
        this.totalQuantity = runningQuantity;
    }

    public long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotal() {
        return subtotal;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cart)) {
            return false;
        }
        Cart cart = (Cart) o;
        return id == cart.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
