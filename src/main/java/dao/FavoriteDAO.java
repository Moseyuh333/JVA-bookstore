package dao;

import utils.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class FavoriteDAO {

    private FavoriteDAO() {
    }

    public static void toggleFavorite(long userId, long bookId, boolean favorite) throws SQLException {
        if (favorite) {
            addFavorite(userId, bookId);
        } else {
            removeFavorite(userId, bookId);
        }
    }

    public static void addFavorite(long userId, long bookId) throws SQLException {
        String sql = "INSERT INTO book_favorites (user_id, book_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            stmt.executeUpdate();
        }
    }

    public static void removeFavorite(long userId, long bookId) throws SQLException {
        String sql = "DELETE FROM book_favorites WHERE user_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            stmt.executeUpdate();
        }
    }

    public static boolean isFavorite(long userId, long bookId) throws SQLException {
        String sql = "SELECT 1 FROM book_favorites WHERE user_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<FavoriteRecord> findFavorites(long userId) throws SQLException {
        String sql = "SELECT b.id, b.title, b.author, b.price, b.image_url, b.category, b.stock_quantity, f.created_at "
                + "FROM book_favorites f INNER JOIN books b ON b.id = f.book_id WHERE f.user_id = ? ORDER BY f.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<FavoriteRecord> list = new ArrayList<>();
                while (rs.next()) {
                    FavoriteRecord record = new FavoriteRecord();
                    record.bookId = rs.getLong("id");
                    record.title = rs.getString("title");
                    record.author = rs.getString("author");
                    record.price = rs.getBigDecimal("price");
                    record.imageUrl = rs.getString("image_url");
                    record.category = rs.getString("category");
                    record.stockQuantity = rs.getInt("stock_quantity");
                    record.markedAt = toLocalDateTime(rs.getTimestamp("created_at"));
                    list.add(record);
                }
                return list;
            }
        }
    }

    public static class FavoriteRecord {
        public long bookId;
        public String title;
        public String author;
        public BigDecimal price;
        public String imageUrl;
        public String category;
        public int stockQuantity;
        public LocalDateTime markedAt;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
