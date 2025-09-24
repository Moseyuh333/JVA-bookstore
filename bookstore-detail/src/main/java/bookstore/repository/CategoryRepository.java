package bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.entity.Category;
public interface CategoryRepository extends JpaRepository<Category, Long> { }
