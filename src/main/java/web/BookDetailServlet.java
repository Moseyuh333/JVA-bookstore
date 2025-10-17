package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name = "BookDetailServlet", urlPatterns = {"/books/detail"})
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
            // Lấy thông tin sách
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, title, author, price, original_price, discount, rating_avg, " +
                "review_count, stock, publisher, category, cover_image, shop_name, " +
                "highlights, specifications, description " +
                "FROM books WHERE id = ?"
            );
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                req.setAttribute("bookId", rs.getInt("id"));
                req.setAttribute("bookTitle", rs.getString("title"));
                req.setAttribute("bookAuthor", rs.getString("author"));
                req.setAttribute("bookPrice", rs.getBigDecimal("price"));
                req.setAttribute("bookOriginalPrice", rs.getBigDecimal("original_price"));
                req.setAttribute("bookDiscount", rs.getBigDecimal("discount"));
                req.setAttribute("bookRating", rs.getDouble("rating_avg"));
                req.setAttribute("bookReviewCount", rs.getInt("review_count"));
                req.setAttribute("bookStock", rs.getString("stock"));
                req.setAttribute("bookPublisher", rs.getString("publisher"));
                req.setAttribute("bookCategory", rs.getString("category"));
                req.setAttribute("bookImage", rs.getString("cover_image"));
                req.setAttribute("bookShop", rs.getString("shop_name"));
                req.setAttribute("bookHighlights", rs.getString("highlights"));
                req.setAttribute("bookSpecs", rs.getString("specifications"));
                req.setAttribute("bookDescription", rs.getString("description"));
            }

            // Gợi ý sách cùng thể loại
            PreparedStatement psRelated = conn.prepareStatement(
                "SELECT id, title, price, cover_image FROM books " +
                "WHERE category = ? AND id <> ? LIMIT 4"
            );
            psRelated.setString(1, rs.getString("category"));
            psRelated.setInt(2, rs.getInt("id"));
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

            // ==== Lấy danh sách review ====
            PreparedStatement psReviews = conn.prepareStatement(
                "SELECT r.rating, r.content, r.created_at, u.username " +
                "FROM reviews r LEFT JOIN users u ON r.user_id = u.id " +
                "WHERE r.book_id = ? ORDER BY r.created_at DESC"
            );
            psReviews.setInt(1, Integer.parseInt(id));
            ResultSet rsReviews = psReviews.executeQuery();

            List<Map<String, Object>> reviews = new ArrayList<>();
            Map<Integer, Integer> ratingCount = new HashMap<>();
            for (int i = 1; i <= 5; i++) ratingCount.put(i, 0);

            int total = 0;
            int sum = 0;

            while (rsReviews.next()) {
                int rating = rsReviews.getInt("rating");
                ratingCount.put(rating, ratingCount.get(rating) + 1);
                total++;
                sum += rating;

                Map<String, Object> r = new HashMap<>();
                r.put("authorName", rsReviews.getString("username") != null ? rsReviews.getString("username") : "Ẩn danh");
                r.put("rating", rating);
                r.put("comment", rsReviews.getString("content"));
                r.put("createdAt", rsReviews.getTimestamp("created_at"));
                reviews.add(r);
            }

            double avg = total > 0 ? (double) sum / total : 0;
            req.setAttribute("bookRating", avg);
            req.setAttribute("reviews", reviews);

            Map<Integer, Integer> reviewStats = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                int count = ratingCount.get(i);
                int percent = total > 0 ? (int) ((count * 100.0) / total) : 0;
                reviewStats.put(i, percent);
            }
            req.setAttribute("reviewStats", reviewStats);

        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }

        req.getRequestDispatcher("/book-detail.jsp").forward(req, resp);
    }
}
