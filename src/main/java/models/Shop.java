package models;

import java.sql.Timestamp;

public class Shop {
    private int id;
    private String name;
    private int ownerId;
    private String description;
    private String logoUrl;
    private String status;
    private Double commissionRate;
    private Timestamp createdAt;
    
    // Các cột mở rộng (có thể null)
    private String avatarUrl;
    private String coverUrl;
    private String featuredImageUrl;
    private String phone;
    private String email;
    private String address;
    private String logoText;
    private String slogan;
    private String bannerColor;
    private String themeColor;
    
    // Constructor
    public Shop() {
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // Alias cho compatibility với code cũ
    public String getShopName() {
        return name;
    }
    
    public void setShopName(String name) {
        this.name = name;
    }
    
    public int getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Extended fields
    public String getAvatarUrl() {
        // Fallback to logoUrl if avatarUrl is null
        return avatarUrl != null ? avatarUrl : logoUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
    
    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }
    
    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getLogoText() {
        return logoText;
    }
    
    public void setLogoText(String logoText) {
        this.logoText = logoText;
    }
    
    public String getSlogan() {
        return slogan;
    }
    
    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }
    
    public String getBannerColor() {
        // Default color if null
        return bannerColor != null && !bannerColor.isEmpty() ? bannerColor : "#FF6B35";
    }
    
    public void setBannerColor(String bannerColor) {
        this.bannerColor = bannerColor;
    }
    
    public String getThemeColor() {
        // Default color if null
        return themeColor != null && !themeColor.isEmpty() ? themeColor : "#FF8C42";
    }
    
    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }
    
    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                ", description='" + description + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", status='" + status + '\'' +
                ", commissionRate=" + commissionRate +
                '}';
    }
}