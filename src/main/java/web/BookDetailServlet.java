package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name = "BookDetailServlet", urlPatterns = { "/books/detail" })
public class BookDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String id = req.getParameter("id");
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, author, price, description, cover_image, category, rating_avg, " +
                            "stock_text, book_url, upc, availability, number_of_reviews, stock " +
                            "FROM books WHERE id = ?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                req.setAttribute("bookId", rs.getInt("id"));
                req.setAttribute("bookTitle", rs.getString("title"));
                req.setAttribute("bookAuthor", rs.getString("author"));
                req.setAttribute("bookPrice", rs.getBigDecimal("price"));
                req.setAttribute("bookDescription", rs.getString("description"));
                req.setAttribute("bookImage", rs.getString("cover_image"));
                req.setAttribute("bookCategory", rs.getString("category"));
                req.setAttribute("bookRating", rs.getDouble("rating_avg"));
                req.setAttribute("bookStockText", rs.getString("stock_text"));
                req.setAttribute("bookStock", rs.getInt("stock"));
                req.setAttribute("bookUrl", rs.getString("book_url"));
                req.setAttribute("bookUpc", rs.getString("upc"));
                req.setAttribute("bookAvailability", rs.getString("availability"));
                req.setAttribute("reviewCount", rs.getInt("number_of_reviews"));

                // Gợi ý sách liên quan (cùng category hoặc author)
                PreparedStatement psRelated = conn.prepareStatement(
                        "SELECT id, title, price, cover_image " +
                                "FROM books WHERE category = ? AND id <> ? LIMIT 4");
                psRelated.setString(1, rs.getString("category"));
                psRelated.setInt(3, rs.getInt("id"));

                ResultSet rsRelated = psRelated.executeQuery();
                List<Map<String, Object>> relatedBooks = new ArrayList<>();
                while (rsRelated.next()) {
                    Map<String, Object> b = new HashMap<>();
                    b.put("id", rsRelated.getInt("id"));
                    b.put("title", rsRelated.getString("title"));
                    b.put("price", rsRelated.getBigDecimal("price"));
                    b.put("coverImage", rsRelated.getString("cover_image"));
                    relatedBooks.add(b);
                }
                req.setAttribute("relatedBooks", relatedBooks);

                // === Reviews ===
                PreparedStatement psReviews = conn.prepareStatement(
                        "SELECT r.rating, r.content, r.created_at, u.username " +
                                "FROM reviews r LEFT JOIN users u ON r.user_id = u.id " +
                                "WHERE r.book_id = ? ORDER BY r.created_at DESC");
                psReviews.setInt(1, rs.getInt("id"));
                ResultSet rsReviews = psReviews.executeQuery();

                java.util.List<java.util.Map<String, Object>> reviews = new java.util.ArrayList<>();
                while (rsReviews.next()) {
                    java.util.Map<String, Object> r = new java.util.HashMap<>();
                    r.put("authorName",
                            rsReviews.getString("username") != null ? rsReviews.getString("username") : "Ẩn danh");
                    r.put("rating", rsReviews.getInt("rating"));
                    r.put("comment", rsReviews.getString("content"));
                    r.put("createdAt", rsReviews.getTimestamp("created_at"));
                    reviews.add(r);
                }
                req.setAttribute("reviews", reviews);

            } else {
                req.setAttribute("error", "Book not found!");
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }

        RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
        rd.forward(req, resp);

    }
}