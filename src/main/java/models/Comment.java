package models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int bookId;
    private int userId;
    private String commentText;
    private String imageUrl;
    private String videoUrl;
    private boolean isVerifiedPurchase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data (for UI convenience)
    private String userName;
    private String userEmail;

    // Constructors
    public Comment() {}

    public Comment(int bookId, int userId, String commentText) {
        this.bookId = bookId;
        this.userId = userId;
        this.commentText = commentText;
    }

    public Comment(int bookId, int userId, String commentText, String imageUrl, String videoUrl, boolean isVerifiedPurchase) {
        this.bookId = bookId;
        this.userId = userId;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.isVerifiedPurchase = isVerifiedPurchase;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

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
        return "Comment{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", userId=" + userId +
                ", commentLength=" + (commentText != null ? commentText.length() : 0) +
                '}';
    }
}
