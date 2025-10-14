package web;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "BookDetailServlet", urlPatterns = { "/books/detail" })
public class BookDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            req.setAttribute("error", "Missing book ID.");
            req.getRequestDispatcher("/books-detail.jsp").forward(req, resp);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid book ID format.");
            req.getRequestDispatcher("/books-detail.jsp").forward(req, resp);
            return;
        }

        List<String[]> books = loadBooksFromCSV(req);
        if (id < 1 || id > books.size()) {
            req.setAttribute("error", "Book not found.");
            req.getRequestDispatcher("/books-detail.jsp").forward(req, resp);
            return;
        }

        String[] b = books.get(id - 1);

        // CSV columns: title, price, stock, rating, category, book_url, description
        String title = b.length > 0 ? b[0] : "Unknown";
        String price = b.length > 1 ? b[1] : "";
        String stockText = b.length > 2 ? b[2] : "Out of stock";
        String rating = b.length > 3 ? b[3] : "0";
        String category = b.length > 4 ? b[4] : "Uncategorized";
        String bookUrl = b.length > 5 ? b[5] : "";
        String description = b.length > 6 ? b[6] : "";

        // set attributes to JSP
        req.setAttribute("bookTitle", title);
        req.setAttribute("bookPrice", price);
        req.setAttribute("bookStockText", stockText);
        req.setAttribute("bookInStock", stockText.toLowerCase().contains("in stock"));
        req.setAttribute("bookRatingInt", rating);
        req.setAttribute("bookCategory", category);
        req.setAttribute("bookUrl", bookUrl);
        req.setAttribute("bookDescription", description);
        req.setAttribute("bookAuthor", null);
        req.setAttribute("bookImage", "http://static.photos/books/320x240/" + id);

        req.getRequestDispatcher("/books-detail.jsp").forward(req, resp);
    }

    private List<String[]> loadBooksFromCSV(HttpServletRequest req) throws IOException {
        String csvPath = req.getServletContext().getRealPath("/boks_full_fast.csv");
        List<String[]> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                list.add(line.split(",", -1));
            }
        }
        return list;
    }
}
