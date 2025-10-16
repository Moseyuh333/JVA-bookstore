package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private String description;
    private String category;
    private int stockQuantity;
    private String imageUrl;
    private double averageRating;
    private int ratingCount;
    private int viewsCount;
    private int salesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Book() {}

    public Book(int id, String title, String author, BigDecimal price, String category, 
                int stockQuantity, String imageUrl, double averageRating, int ratingCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public int getViewsCount() { return viewsCount; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }

    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", averageRating=" + averageRating +
                ", ratingCount=" + ratingCount +
                '}';
    }
}
