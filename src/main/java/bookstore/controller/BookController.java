package bookstore.controller;

import bookstore.entity.Book;
import bookstore.entity.Review;
import bookstore.service.BookService;
import bookstore.service.ReviewService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/book")
public class BookController {
    private final BookService bookService;
    private final ReviewService reviewService;

    public BookController(BookService bookService, ReviewService reviewService){
        this.bookService = bookService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model){
        Book book = bookService.findById(id);
        if (book == null) return "redirect:/";
        model.addAttribute("book", book);
        model.addAttribute("sameCategory", 
            book.getCategory() != null ? bookService.relatedByCategory(book.getCategory().getId(), book.getId()) : List.of());
        model.addAttribute("sameAuthor", 
            book.getAuthor() != null ? bookService.relatedByAuthor(book.getAuthor(), book.getId()) : List.of());
        model.addAttribute("reviews", reviewService.forBook(id));
        model.addAttribute("newReview", new Review());
        return "book/detail";
    }

    @PostMapping("/{id}/review")
    public String addReview(@PathVariable("id") Long id, @ModelAttribute Review newReview){
        Book book = bookService.findById(id);
        if (book == null) return "redirect:/";
        newReview.setBook(book);
        reviewService.save(newReview);
        return "redirect:/book/" + id;
    }
}
