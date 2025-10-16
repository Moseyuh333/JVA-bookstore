package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Coupon {
    private int id;
    private String code;
    private String description;
    private String discountType; // 'percent' or 'fixed'
    private BigDecimal discountValue;
    private BigDecimal minPurchaseAmount;
    private Integer maxUsageCount;
    private int usageCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Coupon() {}

    public Coupon(String code, String discountType, BigDecimal discountValue) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.usageCount = 0;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getMinPurchaseAmount() { return minPurchaseAmount; }
    public void setMinPurchaseAmount(BigDecimal minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }

    public Integer getMaxUsageCount() { return maxUsageCount; }
    public void setMaxUsageCount(Integer maxUsageCount) { this.maxUsageCount = maxUsageCount; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isValidNow() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && validFrom.isBefore(now) && validUntil.isAfter(now);
    }

    public boolean canUse() {
        if (maxUsageCount == null) return isValidNow();
        return isValidNow() && usageCount < maxUsageCount;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if ("percent".equals(discountType)) {
            return amount.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            return discountValue;
        }
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discountType='" + discountType + '\'' +
                ", discountValue=" + discountValue +
                '}';
    }
}
