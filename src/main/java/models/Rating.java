package models;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int userId;
    private int bookId;
    private int rating; // 1-5
    private String review;
    private int helpfulCount;
    private boolean isVerifiedPurchase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data (for UI convenience)
    private String userName;
    private String userEmail;

    // Constructors
    public Rating() {}

    public Rating(int userId, int bookId, int rating) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.helpfulCount = 0;
    }

    public Rating(int userId, int bookId, int rating, String review, boolean isVerifiedPurchase) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.review = review;
        this.isVerifiedPurchase = isVerifiedPurchase;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { isVerifiedPurchase = verifiedPurchase; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", rating=" + rating +
                ", isVerifiedPurchase=" + isVerifiedPurchase +
                '}';
    }
}
