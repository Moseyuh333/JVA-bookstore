package bookstore.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "books")
public class Book implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    private String author;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @Column(columnDefinition="TEXT")
    private String description;

    private Double price;

    private Integer stock;

    private String image; // URL or filename

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
