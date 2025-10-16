package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status; // pending, confirmed, shipping, delivered, cancelled, returned
    private int deliveryAddressId;
    private String shippingAddress;
    private String paymentMethod; // COD, VNPAY, MOMO
    private String notes;
    private int couponId;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Order() {}

    public Order(int userId, BigDecimal totalAmount, String status) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.discountAmount = BigDecimal.ZERO;
        this.finalTotal = totalAmount;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getDeliveryAddressId() { return deliveryAddressId; }
    public void setDeliveryAddressId(int deliveryAddressId) { this.deliveryAddressId = deliveryAddressId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalTotal() { return finalTotal; }
    public void setFinalTotal(BigDecimal finalTotal) { this.finalTotal = finalTotal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
