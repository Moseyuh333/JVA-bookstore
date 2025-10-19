package web;

import dao.ReviewDAO;
import utils.AuthUtil;
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

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String id = req.getParameter("id");
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String highlightParam = req.getParameter("highlightReview");
        if (highlightParam != null) {
            String trimmed = highlightParam.trim();
            if (trimmed.matches("\\d+")) {
                req.setAttribute("highlightReviewDomId", "review-" + trimmed);
            } else if ("mine".equalsIgnoreCase(trimmed)) {
                req.setAttribute("highlightReviewDomId", "mine");
            }
        }

        long bookId;
        try {
            bookId = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Đảm bảo PostgreSQL dùng UTF-8
            try (Statement st = conn.createStatement()) {
                st.execute("SET client_encoding TO 'UTF8'");
            }

            req.setAttribute("soldCount", 0L);

            // --- Lấy chi tiết sách ---
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, author, price, original_price, discount, " +
                            "rating_avg, review_count, stock, publisher, category, cover_image, image_url, " +
                            "shop_name, book_url, highlights, specifications, description, reviews " +
                            "FROM books WHERE id = ?")) {
                ps.setLong(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        req.setAttribute("error", "Không tìm thấy sách!");
                        RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
                        rd.forward(req, resp);
                        return;
                    }

                    // --- Gán thông tin sách ---
                    req.setAttribute("bookId", rs.getLong("id"));
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
                    String coverImage = safeGet(rs, "cover_image");
                    if (coverImage == null || coverImage.trim().isEmpty()) {
                        coverImage = safeGet(rs, "image_url");
                    }
                    req.setAttribute("bookImage", resolveImageUrl(req, coverImage));
                    req.setAttribute("bookShop", rs.getString("shop_name"));
                    req.setAttribute("bookUrl", rs.getString("book_url"));
                    req.setAttribute("bookHighlights", rs.getString("highlights"));
                    req.setAttribute("bookSpecifications", rs.getString("specifications"));
                    req.setAttribute("bookDescription", rs.getString("description"));

                    String rawReviews = rs.getString("reviews");
                    Long currentUserId = null;
                    try {
                        currentUserId = AuthUtil.resolveUserId(req);
                    } catch (SQLException authEx) {
                        authEx.printStackTrace();
                    }

                    loadDynamicReviews(req, bookId, rawReviews, rs.getDouble("rating_avg"), rs.getInt("review_count"), currentUserId);

                    try (PreparedStatement psRelated = conn.prepareStatement(
                            "SELECT id, title, price, cover_image, image_url, category " +
                                    "FROM books WHERE category = ? AND id <> ? LIMIT 4")) {
                        psRelated.setString(1, rs.getString("category"));
                        psRelated.setLong(2, rs.getLong("id"));
                        try (ResultSet rsRelated = psRelated.executeQuery()) {
                            List<Map<String, Object>> relatedBooks = new ArrayList<>();
                            while (rsRelated.next()) {
                                Map<String, Object> b = new HashMap<>();
                                b.put("id", rsRelated.getLong("id"));
                                b.put("title", rsRelated.getString("title"));
                                b.put("price", rsRelated.getBigDecimal("price"));
                                b.put("category", rsRelated.getString("category"));
                                String relatedImage = safeGet(rsRelated, "cover_image");
                                if (relatedImage == null || relatedImage.trim().isEmpty()) {
                                    relatedImage = safeGet(rsRelated, "image_url");
                                }
                                b.put("coverImage", resolveImageUrl(req, relatedImage));
                                relatedBooks.add(b);
                            }
                            req.setAttribute("relatedBooks", relatedBooks);
                        }
                    }
                }
            }

            req.setAttribute("soldCount", fetchDeliveredQuantity(conn, bookId));

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
        }

        RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
        rd.forward(req, resp);
    }

    private String safeGet(ResultSet rs, String column) {
        if (rs == null || column == null) {
            return null;
        }
        try {
            return rs.getString(column);
        } catch (SQLException ignored) {
            return null;
        }
    }

    private String resolveImageUrl(HttpServletRequest req, String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("//")) {
            return "https:" + trimmed;
        }
        String contextPath = req.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }
        if (!contextPath.isEmpty() && !contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        if (trimmed.startsWith(contextPath + "/")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return contextPath.isEmpty() || "/".equals(contextPath) ? trimmed : contextPath + trimmed;
        }
        return (contextPath.endsWith("/")) ? contextPath + trimmed : contextPath + "/" + trimmed;
    }

    private void loadDynamicReviews(HttpServletRequest req, long bookId, String fallbackRawReviews, double fallbackAvg,
                                    int fallbackCount, Long currentUserId) {
        try {
            List<ReviewDAO.ReviewRecord> records = ReviewDAO.listReviews(bookId, 200, 0);
            if (records != null && !records.isEmpty()) {
                List<Map<String, Object>> displayReviews = new ArrayList<>();
                Map<Integer, Integer> ratingCount = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    ratingCount.put(i, 0);
                }
                int total = 0;
                double sum = 0;
                String ownerDomId = null;
                for (ReviewDAO.ReviewRecord record : records) {
                    int rating = record.rating;
                    if (rating >= 1 && rating <= 5) {
                        ratingCount.put(rating, ratingCount.get(rating) + 1);
                        total++;
                        sum += rating;
                    }
                    Map<String, Object> view = new HashMap<>();
                    view.put("authorName", record.reviewerName != null && !record.reviewerName.isEmpty() ? record.reviewerName : "Ẩn danh");
                    view.put("rating", record.rating);
                    view.put("comment", record.content != null ? record.content : "");
                    view.put("createdAt", record.createdAt);
                    view.put("id", record.id);
                    view.put("domId", "review-" + record.id);
                    if (record.mediaUrl != null && !record.mediaUrl.trim().isEmpty()) {
                        view.put("mediaUrl", resolveImageUrl(req, record.mediaUrl));
                        view.put("mediaType", record.mediaType);
                    }
                    boolean isOwner = currentUserId != null && record.userId == currentUserId;
                    view.put("isOwner", isOwner);
                    if (isOwner) {
                        ownerDomId = (String) view.get("domId");
                    }
                    displayReviews.add(view);
                }
                double avg = total > 0 ? sum / total : fallbackAvg;
                Map<Integer, Integer> reviewStats = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    int count = ratingCount.get(i);
                    int percent = total > 0 ? (int) Math.round((count * 100.0) / total) : 0;
                    reviewStats.put(i, percent);
                }
                req.setAttribute("reviews", displayReviews);
                req.setAttribute("reviewStats", reviewStats);
                req.setAttribute("reviewCount", total);
                req.setAttribute("bookRating", avg);
                if (ownerDomId != null) {
                    req.setAttribute("userReviewDomId", ownerDomId);
                    req.setAttribute("userHasReview", Boolean.TRUE);
                    Object highlight = req.getAttribute("highlightReviewDomId");
                    if (highlight != null && "mine".equals(highlight)) {
                        req.setAttribute("highlightReviewDomId", ownerDomId);
                    }
                }
                return;
            }
        } catch (SQLException ex) {
            // Ghi log và fallback về dữ liệu cũ
            ex.printStackTrace();
        }

        if (fallbackRawReviews != null && !fallbackRawReviews.trim().isEmpty()) {
            List<Map<String, Object>> reviews = new ArrayList<>();
            String[] parts = fallbackRawReviews.split("\\|");
            int index = 0;
            for (String part : parts) {
                if (part == null || part.trim().isEmpty()) {
                    continue;
                }
                String s = part.trim();
                s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC)
                        .replace('\u00A0', ' ')
                        .replace("\uFE0F", "")
                        .trim();
                int start = s.indexOf('(');
                int end = s.indexOf(')');
                int colon = s.indexOf(':');
                String name = "";
                int rating = 0;
                String comment = "";
                if (start != -1 && end != -1 && end > start && colon > end) {
                    name = s.substring(0, start).trim();
                    try {
                        String ratingPart = s.substring(start + 1, end).replaceAll("[^0-9]", "");
                        rating = Integer.parseInt(ratingPart);
                    } catch (Exception ignored) {
                    }
                    comment = s.substring(colon + 1).trim();
                } else {
                    name = s;
                }
                if (!name.isEmpty() || !comment.isEmpty()) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("authorName", name.isEmpty() ? "Ẩn danh" : name);
                    r.put("rating", rating);
                    r.put("comment", comment);
                    r.put("domId", "legacy-review-" + index);
                    r.put("isOwner", Boolean.FALSE);
                    reviews.add(r);
                }
                index++;
            }
            if (!reviews.isEmpty()) {
                req.setAttribute("reviews", reviews);
                Map<Integer, Integer> ratingCount = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    ratingCount.put(i, 0);
                }
                int total = 0;
                double sum = 0;
                for (Map<String, Object> r : reviews) {
                    Object ratingObj = r.get("rating");
                    if (ratingObj instanceof Number) {
                        int rating = ((Number) ratingObj).intValue();
                        if (rating >= 1 && rating <= 5) {
                            ratingCount.put(rating, ratingCount.get(rating) + 1);
                            sum += rating;
                            total++;
                        }
                    }
                }
                double avg = total > 0 ? sum / total : fallbackAvg;
                Map<Integer, Integer> reviewStats = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    int count = ratingCount.get(i);
                    int percent = total > 0 ? (int) Math.round((count * 100.0) / total) : 0;
                    reviewStats.put(i, percent);
                }
                req.setAttribute("reviewStats", reviewStats);
                req.setAttribute("reviewCount", total > 0 ? total : fallbackCount);
                req.setAttribute("bookRating", avg);
            } else {
                req.setAttribute("reviewCount", fallbackCount);
            }
        } else {
            req.setAttribute("reviewCount", fallbackCount);
        }
    }

    private long fetchDeliveredQuantity(Connection conn, long bookId) {
        String sql = "SELECT COALESCE(SUM(oi.quantity), 0) AS sold FROM order_items oi " +
                "INNER JOIN orders o ON o.id = oi.order_id " +
                "WHERE oi.book_id = ? AND LOWER(o.status) = 'delivered'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("sold");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0L;
    }
}
