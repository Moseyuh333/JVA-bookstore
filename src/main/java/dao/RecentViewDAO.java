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

public final class RecentViewDAO {

    private RecentViewDAO() {
    }

    public static void recordView(long userId, long bookId) throws SQLException {
        String sql = "INSERT INTO user_recent_views (user_id, book_id, viewed_at) VALUES (?, ?, CURRENT_TIMESTAMP) "
                + "ON CONFLICT (user_id, book_id) DO UPDATE SET viewed_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            stmt.executeUpdate();
        }
    }

    public static List<RecentViewRecord> findRecentViews(long userId, int limit) throws SQLException {
        String sql = "SELECT v.book_id, v.viewed_at, b.title, b.author, b.price, b.image_url FROM user_recent_views v "
                + "INNER JOIN books b ON b.id = v.book_id WHERE v.user_id = ? ORDER BY v.viewed_at DESC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                List<RecentViewRecord> list = new ArrayList<>();
                while (rs.next()) {
                    RecentViewRecord record = new RecentViewRecord();
                    record.bookId = rs.getLong("book_id");
                    record.title = rs.getString("title");
                    record.author = rs.getString("author");
                    record.price = rs.getBigDecimal("price");
                    record.imageUrl = rs.getString("image_url");
                    record.viewedAt = toLocalDateTime(rs.getTimestamp("viewed_at"));
                    list.add(record);
                }
                return list;
            }
        }
    }

    public static class RecentViewRecord {
        public long bookId;
        public String title;
        public String author;
        public java.math.BigDecimal price;
        public String imageUrl;
        public LocalDateTime viewedAt;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
