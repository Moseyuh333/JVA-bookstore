package bookstore.service;
import bookstore.entity.Book;
import bookstore.repository.BookRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    private final BookRepository repo;
    public BookService(BookRepository repo){ this.repo = repo; }

    public Book findById(Long id){ return repo.findById(id).orElse(null); }
    public List<Book> relatedByCategory(Long catId, Long exclude){
        return repo.findTop8ByCategoryIdAndIdNot(catId, exclude);
    }
    public List<Book> relatedByAuthor(String author, Long exclude){
        return repo.findTop8ByAuthorAndIdNot(author, exclude);
    }
}
