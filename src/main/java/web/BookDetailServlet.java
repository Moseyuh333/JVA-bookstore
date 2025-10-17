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
            // --- Truy vấn chi tiết sách ---
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, author, price, original_price, discount, " +
                    "rating_avg, review_count, stock, publisher, category, cover_image, " +
                    "shop_name, book_url, highlights, specifications, description, reviews " +
                    "FROM books WHERE id = ?");
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
                req.setAttribute("reviewCount", rs.getInt("review_count"));
                req.setAttribute("bookStock", rs.getString("stock"));
                req.setAttribute("bookPublisher", rs.getString("publisher"));
                req.setAttribute("bookCategory", rs.getString("category"));
                req.setAttribute("bookImage", rs.getString("cover_image"));
                req.setAttribute("bookShop", rs.getString("shop_name"));
                req.setAttribute("bookUrl", rs.getString("book_url"));
                req.setAttribute("bookHighlights", rs.getString("highlights"));
                req.setAttribute("bookSpecifications", rs.getString("specifications"));
                req.setAttribute("bookDescription", rs.getString("description"));
                req.setAttribute("bookReviews", rs.getString("reviews"));

                // --- Gợi ý sách liên quan ---
                String category = rs.getString("category");
                PreparedStatement psRelated = conn.prepareStatement(
                        "SELECT id, title, price, cover_image " +
                        "FROM books WHERE category = ? AND id <> ? LIMIT 4");
                psRelated.setString(1, category);
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

            } else {
                req.setAttribute("error", "Không tìm thấy sách!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
        }

        RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
        rd.forward(req, resp);
    }
}
