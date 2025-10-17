package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRecord {
    private final long id;
    private final long orderId;
    private final String provider;
    private final String method;
    private final String status;
    private final BigDecimal amount;
    private final String currency;
    private final String transactionCode;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PaymentRecord(long id,
                         long orderId,
                         String provider,
                         String method,
                         String status,
                         BigDecimal amount,
                         String currency,
                         String transactionCode,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.provider = provider;
        this.method = method;
        this.status = status;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = currency;
        this.transactionCode = transactionCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getProvider() {
        return provider;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
