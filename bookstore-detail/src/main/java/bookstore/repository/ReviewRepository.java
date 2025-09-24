package bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.entity.Review;

import java.util.List;
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);
}
