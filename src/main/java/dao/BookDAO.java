package dao;

import models.Book;
import utils.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class BookDAO {

    public enum SortType {
        NEWEST("new"),
        BEST_SELLING("best"),
        TOP_RATED("rated"),
        MOST_FAVORITED("favorite");

        private final String param;

        SortType(String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }

        public static SortType fromParam(String value) {
            if (value == null || value.isEmpty()) {
                return NEWEST;
            }
            String normalized = value.trim().toLowerCase(Locale.US);
            for (SortType type : values()) {
                if (type.param.equals(normalized)) {
                    return type;
                }
            }
            switch (normalized) {
                case "newest":
                case "latest":
                    return NEWEST;
                case "bestseller":
                case "bestselling":
                    return BEST_SELLING;
                case "rating":
                case "ratings":
                    return TOP_RATED;
                case "favorites":
                case "favourite":
                case "favourites":
                case "liked":
                    return MOST_FAVORITED;
                default:
                    return NEWEST;
            }
        }
    }

    private static final String BASE_SELECT =
            "SELECT b.id, b.title, b.author, b.isbn, b.price, b.description, b.category, b.stock_quantity, b.image_url, " +
            "b.created_at, b.updated_at, b.status, " +
            "b.shop_id, b.shop_name, " +
            "COALESCE(sales.total_sold, metrics.total_sold, 0) AS total_sold, " +
            "COALESCE(reviews.avg_rating, metrics.avg_rating, 0) AS average_rating, " +
            "COALESCE(reviews.rating_count, metrics.rating_count, 0) AS rating_count, " +
            "COALESCE(favorites.favorite_count, metrics.favorite_count, 0) AS favorite_count " +
            "FROM books b " +
            "LEFT JOIN book_metrics metrics ON metrics.book_id = b.id " +
            "LEFT JOIN (" +
            "    SELECT book_id, SUM(quantity) AS total_sold " +
            "    FROM order_items GROUP BY book_id" +
            ") sales ON sales.book_id = b.id " +
            "LEFT JOIN (" +
            "    SELECT book_id, AVG(rating)::FLOAT AS avg_rating, COUNT(*) AS rating_count " +
            "    FROM book_reviews WHERE status = 'published' GROUP BY book_id" +
            ") reviews ON reviews.book_id = b.id " +
            "LEFT JOIN (" +
            "    SELECT book_id, COUNT(*) AS favorite_count " +
            "    FROM book_favorites GROUP BY book_id" +
            ") favorites ON favorites.book_id = b.id";

    public static List<Book> getTopBooks(SortType sortType, int limit) throws SQLException {
        return findBooks(null, sortType, limit, 0);
    }

    public static List<Book> findBooks(String category, SortType sortType, int limit, int offset) throws SQLException {
        if (sortType == null) {
            sortType = SortType.NEWEST;
        }
        if (limit <= 0) {
            limit = 20;
        }
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();
        sql.append(" WHERE b.status = 'active'");
        if (category != null && !category.isEmpty()) {
            sql.append(" AND b.category = ?");
            params.add(category);
        }
        sql.append(" ");
        sql.append(orderClause(sortType));
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        String finalSql = sql.toString();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = prepare(connection, finalSql, params)) {
            try (ResultSet rs = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
                return books;
            }
        } catch (SQLException ex) {
            System.err.println("BookDAO - Query failed: " + finalSql + " | params=" + params + " | msg=" + ex.getMessage());
            throw ex;
        }
    }

    public static int countBooks(String category) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE status = 'active'";
        List<Object> params = Collections.emptyList();
        if (category != null && !category.isEmpty()) {
            sql += " AND category = ?";
            params = Collections.singletonList(category);
        }
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            System.err.println("BookDAO - Count query failed: " + sql + " | params=" + params + " | msg=" + ex.getMessage());
            throw ex;
        }
    }

    public static List<String> getAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM books WHERE status = 'active' AND category IS NOT NULL AND TRIM(category) <> '' ORDER BY category";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<String> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(rs.getString(1));
            }
            return categories;
        } catch (SQLException ex) {
            System.err.println("BookDAO - Category query failed: " + sql + " | msg=" + ex.getMessage());
            throw ex;
        }
    }

    public static List<Book> searchBooks(String keyword, int limit) throws SQLException {
        if (keyword == null) {
            return Collections.emptyList();
        }
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        if (limit <= 0) {
            limit = 10;
        }
        limit = Math.min(limit, 50);

        String escaped = escapeLikePattern(trimmed);
        String pattern = "%" + escaped + "%";

        String sql = BASE_SELECT +
                " WHERE b.status = 'active' AND (b.title ILIKE ? ESCAPE '\\' " +
                "OR b.author ILIKE ? ESCAPE '\\' " +
                "OR b.isbn ILIKE ? ESCAPE '\\') " +
                "ORDER BY rating_count DESC, total_sold DESC, b.title ASC LIMIT ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setInt(4, limit);
            try (ResultSet rs = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
                return books;
            }
        } catch (SQLException ex) {
            System.err.println("BookDAO - Search failed: " + sql + " | keyword=" + trimmed + " | msg=" + ex.getMessage());
            throw ex;
        }
    }

    public static boolean isValidSortParam(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return true;
        }
        for (SortType type : EnumSet.allOf(SortType.class)) {
            if (type.getParam().equals(normalized)) {
                return true;
            }
        }
        switch (normalized) {
            case "newest":
            case "latest":
            case "bestseller":
            case "bestselling":
            case "rating":
            case "ratings":
            case "favorites":
            case "favourite":
            case "favourites":
            case "liked":
                return true;
            default:
                return false;
        }
    }

    private static String escapeLikePattern(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static PreparedStatement prepare(Connection connection, String sql, List<Object> params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        if (params != null) {
            int index = 1;
            for (Object param : params) {
                statement.setObject(index++, param);
            }
        }
        return statement;
    }

    private static String orderClause(SortType sortType) {
        switch (sortType) {
            case BEST_SELLING:
                return "ORDER BY total_sold DESC, b.created_at DESC, b.id DESC";
            case TOP_RATED:
                return "ORDER BY average_rating DESC, rating_count DESC, b.created_at DESC, b.id DESC";
            case MOST_FAVORITED:
                return "ORDER BY favorite_count DESC, b.created_at DESC, b.id DESC";
            case NEWEST:
            default:
                return "ORDER BY b.created_at DESC, b.id DESC";
        }
    }

    private static Book mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String isbn = rs.getString("isbn");
        BigDecimal price = rs.getBigDecimal("price");
        String description = rs.getString("description");
        String category = rs.getString("category");
        int stockQuantity = rs.getInt("stock_quantity");
        String imageUrl = rs.getString("image_url");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
        LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
        int totalSold = rs.getInt("total_sold");
        double averageRating = rs.getDouble("average_rating");
        if (rs.wasNull()) {
            averageRating = 0.0;
        }
        int ratingCount = rs.getInt("rating_count");
        int favoriteCount = rs.getInt("favorite_count");
        String status = rs.getString("status");
        
        // Lấy thông tin shop
        Integer shopId = null;
        int shopIdValue = rs.getInt("shop_id");
        if (!rs.wasNull()) {
            shopId = shopIdValue;
        }
        String shopName = rs.getString("shop_name");
        
        return new Book(id, title, author, isbn, price, description, category, stockQuantity, imageUrl,
                createdAt, updatedAt, totalSold, averageRating, ratingCount, favoriteCount,status, shopId, shopName);
    }
}
