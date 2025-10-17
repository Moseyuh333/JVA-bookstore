package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "BookDetailServlet", urlPatterns = { "/books/detail" })
public class BookDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String id = req.getParameter("id");
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Đảm bảo PostgreSQL dùng UTF-8
            try (Statement st = conn.createStatement()) {
                st.execute("SET client_encoding TO 'UTF8'");
            }

            // --- Lấy chi tiết sách ---
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, author, price, original_price, discount, " +
                            "rating_avg, review_count, stock, publisher, category, cover_image, " +
                            "shop_name, book_url, highlights, specifications, description, reviews " +
                            "FROM books WHERE id = ?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                req.setAttribute("error", "Không tìm thấy sách!");
                RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
                rd.forward(req, resp);
                return;
            }

            // --- Gán thông tin sách ---
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

            // --- Parse reviews từ CSV (cột reviews trong bảng books) ---
            String rawReviews = rs.getString("reviews");
            List<Map<String, Object>> reviews = new ArrayList<>();

            if (rawReviews != null && !rawReviews.trim().isEmpty()) {
                String[] parts = rawReviews.split("\\|");
                Pattern pattern = Pattern.compile("^\\s*([^\\(]+)\\((\\d+)\\s*⭐\\):\\s*(.*)$");
                // nhóm 1: tên, nhóm 2: số sao, nhóm 3: nội dung sau sao

                for (String part : parts) {
                    part = part.trim();
                    if (part.isEmpty())
                        continue;

                    Matcher m = pattern.matcher(part);
                    String name = "";
                    String comment = "";
                    int rating = 0;

                    if (m.find()) {
                        name = m.group(1).trim();
                        try {
                            rating = Integer.parseInt(m.group(2));
                        } catch (Exception ignored) {
                        }
                        comment = m.group(3).trim();
                    } else {
                        // fallback: nếu không match, tách thủ công
                        int sep = part.indexOf(':');
                        if (sep != -1) {
                            name = part.substring(0, sep).replaceAll("\\(.*\\)", "").trim();
                            comment = part.substring(sep + 1).trim();
                        } else {
                            name = part.trim();
                        }
                    }

                    // Dọn text: bỏ bullet đầu dòng, dấu **, khoảng trắng thừa
                    comment = comment
                            .replaceAll("^[-•\\s]+", "")
                            .replaceAll("\\*+", "")
                            .replaceAll("\\s{2,}", " ")
                            .trim();

                    Map<String, Object> r = new HashMap<>();
                    r.put("authorName", name.isEmpty() ? "Ẩn danh" : name);
                    r.put("rating", rating);
                    r.put("comment", comment);
                    reviews.add(r);
                }
            }

            // --- Nếu có đánh giá trong CSV thì hiển thị ra ---
            if (!reviews.isEmpty()) {
                req.setAttribute("reviews", reviews);

                // Tính điểm trung bình và phần trăm
                Map<Integer, Integer> ratingCount = new HashMap<>();
                for (int i = 1; i <= 5; i++)
                    ratingCount.put(i, 0);
                int total = 0, sum = 0;

                for (Map<String, Object> r : reviews) {
                    int rating = (int) r.get("rating");
                    if (rating >= 1 && rating <= 5) {
                        ratingCount.put(rating, ratingCount.get(rating) + 1);
                        total++;
                        sum += rating;
                    }
                }

                double avg = total > 0 ? (double) sum / total : rs.getDouble("rating_avg");
                req.setAttribute("bookRating", avg);

                Map<Integer, Integer> reviewStats = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    int count = ratingCount.get(i);
                    int percent = total > 0 ? (int) ((count * 100.0) / total) : 0;
                    reviewStats.put(i, percent);
                }
                req.setAttribute("reviewStats", reviewStats);
            } else {
                req.setAttribute("bookReviewsRaw", rawReviews);
            }

            // --- Lấy sách cùng danh mục (gợi ý) ---
            PreparedStatement psRelated = conn.prepareStatement(
                    "SELECT id, title, price, cover_image, category " +
                            "FROM books WHERE category = ? AND id <> ? LIMIT 4");
            psRelated.setString(1, rs.getString("category"));
            psRelated.setInt(2, rs.getInt("id"));
            ResultSet rsRelated = psRelated.executeQuery();

            List<Map<String, Object>> relatedBooks = new ArrayList<>();
            while (rsRelated.next()) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", rsRelated.getInt("id"));
                b.put("title", rsRelated.getString("title"));
                b.put("price", rsRelated.getBigDecimal("price"));
                b.put("category", rsRelated.getString("category"));
                b.put("coverImage", rsRelated.getString("cover_image"));
                relatedBooks.add(b);
            }
            req.setAttribute("relatedBooks", relatedBooks);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
        }

        RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
        rd.forward(req, resp);
    }
}
