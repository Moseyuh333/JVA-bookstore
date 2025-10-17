package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final long id;
    private final Long userId;
    private final String orderNumber;
    private final String status;
    private final String paymentStatus;
    private final String paymentMethod;
    private final String paymentReference;
    private final BigDecimal subtotalAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal shippingFee;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;
    private final String currency;
    private final String shippingFullName;
    private final String shippingPhone;
    private final String shippingEmail;
    private final String shippingAddress;
    private final String shippingCity;
    private final String shippingPostalCode;
    private final String shippingCountry;
    private final String shippingNotes;
    private final String customerMessage;
    private final String notes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<OrderItem> items;
    private final List<PaymentRecord> payments;
    private final List<OrderStatusEntry> statusHistory;

    public Order(long id,
                 Long userId,
                 String orderNumber,
                 String status,
                 String paymentStatus,
                 String paymentMethod,
                 String paymentReference,
                 BigDecimal subtotalAmount,
                 BigDecimal taxAmount,
                 BigDecimal shippingFee,
                 BigDecimal discountAmount,
                 BigDecimal totalAmount,
                 String currency,
                 String shippingFullName,
                 String shippingPhone,
                 String shippingEmail,
                 String shippingAddress,
                 String shippingCity,
                 String shippingPostalCode,
                 String shippingCountry,
                 String shippingNotes,
                 String customerMessage,
                 String notes,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt,
                 List<OrderItem> items,
                 List<PaymentRecord> payments,
                 List<OrderStatusEntry> statusHistory) {
        this.id = id;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.subtotalAmount = defaultZero(subtotalAmount);
        this.taxAmount = defaultZero(taxAmount);
        this.shippingFee = defaultZero(shippingFee);
        this.discountAmount = defaultZero(discountAmount);
        this.totalAmount = defaultZero(totalAmount);
        this.currency = currency != null ? currency : "VND";
        this.shippingFullName = shippingFullName;
        this.shippingPhone = shippingPhone;
        this.shippingEmail = shippingEmail;
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingPostalCode = shippingPostalCode;
        this.shippingCountry = shippingCountry;
        this.shippingNotes = shippingNotes;
        this.customerMessage = customerMessage;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = wrap(items);
        this.payments = wrap(payments);
        this.statusHistory = wrap(statusHistory);
    }

    private static BigDecimal defaultZero(BigDecimal input) {
        return input != null ? input : BigDecimal.ZERO;
    }

    private static <T> List<T> wrap(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getShippingFullName() {
        return shippingFullName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public String getShippingEmail() {
        return shippingEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public String getShippingNotes() {
        return shippingNotes;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public List<PaymentRecord> getPayments() {
        return payments;
    }

    public List<OrderStatusEntry> getStatusHistory() {
        return statusHistory;
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public boolean hasPayments() {
        return !payments.isEmpty();
    }

    public boolean hasStatusHistory() {
        return !statusHistory.isEmpty();
    }
}
