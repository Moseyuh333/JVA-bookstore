package models;

import java.time.LocalDateTime;

public class OrderStatusEntry {
    private final long id;
    private final long orderId;
    private final String status;
    private final String note;
    private final String createdBy;
    private final LocalDateTime createdAt;

    public OrderStatusEntry(long id,
                            long orderId,
                            String status,
                            String note,
                            String createdBy,
                            LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.note = note;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
