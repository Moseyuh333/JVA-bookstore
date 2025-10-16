package models;

import java.time.LocalDateTime;

public class DeliveryAddress {
    private int id;
    private int userId;
    private String recipientName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public DeliveryAddress() {}

    public DeliveryAddress(int userId, String recipientName, String phoneNumber,
                          String province, String district, String ward, String addressDetail) {
        this.userId = userId;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.addressDetail = addressDetail;
        this.isDefault = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper method to get full address
    public String getFullAddress() {
        return addressDetail + ", " + ward + ", " + district + ", " + province;
    }

    @Override
    public String toString() {
        return "DeliveryAddress{" +
                "id=" + id +
                ", recipientName='" + recipientName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + getFullAddress() + '\'' +
                '}';
    }
}
