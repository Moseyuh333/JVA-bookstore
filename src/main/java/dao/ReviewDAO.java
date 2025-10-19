package dao;

import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReviewDAO {

    private ReviewDAO() {
    }

    public static boolean canReview(long userId, long bookId) throws SQLException {
        String sql = "SELECT 1 FROM orders o INNER JOIN order_items oi ON oi.order_id = o.id "
                + "WHERE o.user_id = ? AND oi.book_id = ? AND o.status IN ('delivered', 'returned') LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void upsertReview(long userId, long bookId, int rating, String title, String content, String mediaUrl, String mediaType) throws SQLException {
        if (!canReview(userId, bookId)) {
            throw new SQLException("Bạn chỉ có thể đánh giá những sản phẩm đã mua");
        }
        String sql = "INSERT INTO book_reviews (user_id, book_id, rating, title, content, media_url, media_type, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'published') "
                + "ON CONFLICT (user_id, book_id) DO UPDATE SET rating = EXCLUDED.rating, title = EXCLUDED.title, content = EXCLUDED.content, "
                + "media_url = EXCLUDED.media_url, media_type = EXCLUDED.media_type, status = 'published', updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            stmt.setInt(3, rating);
            stmt.setString(4, title);
            stmt.setString(5, content);
            stmt.setString(6, mediaUrl);
            stmt.setString(7, mediaType);
            stmt.executeUpdate();
        }
    }

    public static void deleteReview(long userId, long bookId) throws SQLException {
        String sql = "DELETE FROM book_reviews WHERE user_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            stmt.executeUpdate();
        }
    }

    public static List<ReviewRecord> listReviews(long bookId, int limit, int offset) throws SQLException {
        String sql = "SELECT r.id, r.user_id, r.book_id, r.rating, r.title, r.content, r.media_url, r.media_type, r.created_at, r.updated_at, u.full_name "
                + "FROM book_reviews r INNER JOIN users u ON u.id = r.user_id "
                + "WHERE r.book_id = ? AND r.status = 'published' ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReviewRecord> list = new ArrayList<>();
                while (rs.next()) {
                    ReviewRecord record = new ReviewRecord();
                    record.id = rs.getLong("id");
                    record.userId = rs.getLong("user_id");
                    record.bookId = rs.getLong("book_id");
                    record.rating = rs.getInt("rating");
                    record.title = rs.getString("title");
                    record.content = rs.getString("content");
                    record.mediaUrl = rs.getString("media_url");
                    record.mediaType = rs.getString("media_type");
                    record.createdAt = toLocalDateTime(rs.getTimestamp("created_at"));
                    record.updatedAt = toLocalDateTime(rs.getTimestamp("updated_at"));
                    record.reviewerName = rs.getString("full_name");
                    list.add(record);
                }
                return list;
            }
        }
    }

    public static ReviewRecord findUserReview(long userId, long bookId) throws SQLException {
        String sql = "SELECT id, user_id, book_id, rating, title, content, media_url, media_type, created_at, updated_at FROM book_reviews WHERE user_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ReviewRecord record = new ReviewRecord();
                    record.id = rs.getLong("id");
                    record.userId = rs.getLong("user_id");
                    record.bookId = rs.getLong("book_id");
                    record.rating = rs.getInt("rating");
                    record.title = rs.getString("title");
                    record.content = rs.getString("content");
                    record.mediaUrl = rs.getString("media_url");
                    record.mediaType = rs.getString("media_type");
                    record.createdAt = toLocalDateTime(rs.getTimestamp("created_at"));
                    record.updatedAt = toLocalDateTime(rs.getTimestamp("updated_at"));
                    return record;
                }
                return null;
            }
        }
    }

    public static class ReviewRecord {
        public long id;
        public long userId;
        public long bookId;
        public int rating;
        public String title;
        public String content;
        public String mediaUrl;
        public String mediaType;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public String reviewerName;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
