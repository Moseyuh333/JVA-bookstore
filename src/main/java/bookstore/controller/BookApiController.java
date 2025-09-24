package bookstore.controller;

import bookstore.entity.Book;
import bookstore.entity.Review;
import bookstore.service.BookService;
import bookstore.service.ReviewService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookApiController {
    private final BookService bookService;
    private final ReviewService reviewService;
    public BookApiController(BookService bookService, ReviewService reviewService){
        this.bookService = bookService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable("id") Long id){ return bookService.findById(id); }

    @GetMapping("/{id}/reviews")
    public List<Review> reviews(@PathVariable("id") Long id){ return reviewService.forBook(id); }
}
