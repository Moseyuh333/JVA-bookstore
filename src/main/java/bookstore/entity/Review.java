package bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating; // 1..5

    @Column(columnDefinition="TEXT")
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}
