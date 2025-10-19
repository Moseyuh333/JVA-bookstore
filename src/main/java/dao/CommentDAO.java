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
import java.util.Locale;

public final class CommentDAO {

    private CommentDAO() {
    }

    public static void addComment(long userId, long bookId, long orderId, Long orderItemId, String content, String mediaType, String mediaUrl) throws SQLException {
        if (!ReviewDAO.canReview(userId, bookId)) {
            throw new SQLException("Bạn chỉ có thể bình luận sản phẩm đã mua");
        }
        if (content == null || content.trim().length() < 50) {
            throw new SQLException("Nội dung bình luận phải có ít nhất 50 ký tự");
        }
        String normalizedContent = content.trim();
        String normalizedMediaType = null;
        if (mediaType != null && !mediaType.trim().isEmpty()) {
            String lowered = mediaType.trim().toLowerCase(Locale.ROOT);
            if (!"image".equals(lowered) && !"video".equals(lowered)) {
                throw new SQLException("Loại nội dung đính kèm không hợp lệ. Chỉ hỗ trợ image hoặc video");
            }
            normalizedMediaType = lowered;
        }
        String normalizedMediaUrl = mediaUrl != null && !mediaUrl.trim().isEmpty() ? mediaUrl.trim() : null;
        String sql = "INSERT INTO book_comments (user_id, book_id, order_id, order_item_id, content, media_type, media_url, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'published')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            if (orderId <= 0) {
                stmt.setNull(3, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(3, orderId);
            }
            if (orderItemId == null || orderItemId <= 0) {
                stmt.setNull(4, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(4, orderItemId);
            }
            stmt.setString(5, normalizedContent);
            if (normalizedMediaType == null) {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(6, normalizedMediaType);
            }
            if (normalizedMediaUrl == null) {
                stmt.setNull(7, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(7, normalizedMediaUrl);
            }
            stmt.executeUpdate();
        }
    }

    public static List<CommentRecord> listComments(long bookId, int limit, int offset) throws SQLException {
        String sql = "SELECT c.id, c.user_id, c.book_id, c.content, c.media_type, c.media_url, c.created_at, c.updated_at, u.full_name "
                + "FROM book_comments c INNER JOIN users u ON u.id = c.user_id "
                + "WHERE c.book_id = ? AND c.status = 'published' ORDER BY c.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                List<CommentRecord> list = new ArrayList<>();
                while (rs.next()) {
                    CommentRecord record = new CommentRecord();
                    record.id = rs.getLong("id");
                    record.userId = rs.getLong("user_id");
                    record.bookId = rs.getLong("book_id");
                    record.content = rs.getString("content");
                    record.mediaType = rs.getString("media_type");
                    record.mediaUrl = rs.getString("media_url");
                    record.createdAt = toLocalDateTime(rs.getTimestamp("created_at"));
                    record.updatedAt = toLocalDateTime(rs.getTimestamp("updated_at"));
                    record.authorName = rs.getString("full_name");
                    list.add(record);
                }
                return list;
            }
        }
    }

    public static void deleteComment(long userId, long commentId) throws SQLException {
        String sql = "DELETE FROM book_comments WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, commentId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    public static class CommentRecord {
        public long id;
        public long userId;
        public long bookId;
        public String content;
        public String mediaType;
        public String mediaUrl;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public String authorName;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
