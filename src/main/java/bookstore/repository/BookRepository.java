package bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.entity.Book;

import java.util.List;
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findTop8ByCategoryIdAndIdNot(Long categoryId, Long excludeId);
    List<Book> findTop8ByAuthorAndIdNot(String author, Long excludeId);
}
