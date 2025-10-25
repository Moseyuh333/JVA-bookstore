package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Book {
    private final long id;
    private final String title;
    private final String author;
    private final String isbn;
    private final BigDecimal price;
    private final String description;
    private final String category;
    private final int stockQuantity;
    private final String imageUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final int totalSold;
    private final double averageRating;
    private final int ratingCount;
    private final int favoriteCount;
    private final String status;
    private final Integer shopId;
    private final String shopName;

    public Book(
            long id,
            String title,
            String author,
            String isbn,
            BigDecimal price,
            String description,
            String category,
            int stockQuantity,
            String imageUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int totalSold,
            double averageRating,
            int ratingCount,
            int favoriteCount,
            String status,
            Integer shopId,
            String shopName) {
            
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.description = description;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalSold = totalSold;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.favoriteCount = favoriteCount;
        this.status = status;
        this.shopId = shopId;
        this.shopName = shopName;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public String getStatus() {
        return status;
    }

    public Integer getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }
}
