package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Order {
    private long id;
    private String code;
    private long userId;
    private Integer shopId;
    private LocalDateTime orderDate;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentProvider;
    private String paymentMetadata;
    private BigDecimal itemsSubtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private String currency;
    private String couponCode;
    private String customerName;
    private String customerEmail;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items = new ArrayList<>();
    private List<Map<String, Object>> itemsSnapshot;
    private Map<String, Object> receiverSnapshot;
    private Map<String, Object> shopSnapshot;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getPaymentMetadata() {
        return paymentMetadata;
    }

    public void setPaymentMetadata(String paymentMetadata) {
        this.paymentMetadata = paymentMetadata;
    }

    public BigDecimal getItemsSubtotal() {
        return itemsSubtotal;
    }

    public void setItemsSubtotal(BigDecimal itemsSubtotal) {
        this.itemsSubtotal = itemsSubtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public List<Map<String, Object>> getItemsSnapshot() {
        return itemsSnapshot;
    }

    public void setItemsSnapshot(List<Map<String, Object>> itemsSnapshot) {
        this.itemsSnapshot = itemsSnapshot;
    }

    public Map<String, Object> getReceiverSnapshot() {
        return receiverSnapshot;
    }

    public void setReceiverSnapshot(Map<String, Object> receiverSnapshot) {
        this.receiverSnapshot = receiverSnapshot;
    }

    public Map<String, Object> getShopSnapshot() {
        return shopSnapshot;
    }

    public void setShopSnapshot(Map<String, Object> shopSnapshot) {
        this.shopSnapshot = shopSnapshot;
    }
}
