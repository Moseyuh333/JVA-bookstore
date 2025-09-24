package bookstore.service;
import bookstore.entity.Review;
import bookstore.repository.ReviewRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository repo;
    public ReviewService(ReviewRepository repo){ this.repo = repo; }

    public List<Review> forBook(Long bookId){ return repo.findByBookIdOrderByCreatedAtDesc(bookId); }
    public Review save(Review r){ return repo.save(r); }
}
