package web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.BookDAO;
import dao.BookDAO.SortType;
import models.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "BooksApiServlet", urlPatterns = {"/api/books", "/api/books/*"})
public class BooksApiServlet extends HttpServlet {

    private static class SectionSpec {
        private final String id;
        private final String label;
        private final SortType sortType;

        private SectionSpec(String id, String label, SortType sortType) {
            this.id = id;
            this.label = label;
            this.sortType = sortType;
        }
    }

    private static final List<SectionSpec> HOME_SECTIONS = Arrays.asList(
        new SectionSpec("new", "Sách mới", SortType.NEWEST),
        new SectionSpec("best", "Bán chạy", SortType.BEST_SELLING),
        new SectionSpec("rated", "Được đánh giá cao", SortType.TOP_RATED),
        new SectionSpec("favorite", "Được yêu thích", SortType.MOST_FAVORITED)
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        try {
            switch (path) {
                case "/sections":
                    handleSections(req, resp);
                    break;
                case "/categories":
                    handleCategories(resp);
                    break;
                case "/search":
                    handleSearch(req, resp);
                    break;
                case "/":
                default:
                    handleCatalogList(req, resp);
                    break;
            }
        } catch (SQLException ex) {
            sendServerError(resp, ex);
        }
    }

    private void handleSections(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        int limit = parsePositiveInt(req.getParameter("limit"), 20, 100);

        JsonArray sectionsJson = new JsonArray();

        for (SectionSpec spec : HOME_SECTIONS) {
            List<Book> books = BookDAO.getTopBooks(spec.sortType, limit);
            JsonObject section = new JsonObject();
            section.addProperty("id", spec.id);
            section.addProperty("title", spec.label);
            section.addProperty("sort", spec.sortType.getParam());
            section.add("books", toBookJsonArray(books));
            sectionsJson.add(section);
        }

        JsonObject payload = new JsonObject();
        payload.add("sections", sectionsJson);
        payload.add("categories", toStringJsonArray(BookDAO.getAllCategories()));

        writeJson(resp, payload);
    }

    private void handleCategories(HttpServletResponse resp) throws IOException, SQLException {
        List<String> categories = BookDAO.getAllCategories();
    JsonObject payload = new JsonObject();
    payload.add("data", toStringJsonArray(categories));
        payload.addProperty("count", categories.size());
        writeJson(resp, payload);
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String keyword = trimToNull(req.getParameter("q"));
        int limit = parsePositiveInt(req.getParameter("limit"), 10, 50);

        JsonObject payload = new JsonObject();
        payload.addProperty("query", keyword == null ? "" : keyword);

        if (keyword == null || keyword.length() < 2) {
            payload.add("data", new JsonArray());
            payload.addProperty("count", 0);
            payload.addProperty("message", "Nhập tối thiểu 2 ký tự để tìm kiếm sách");
            writeJson(resp, payload);
            return;
        }

        List<Book> books = BookDAO.searchBooks(keyword, limit);
        payload.add("data", toBookJsonArray(books));
        payload.addProperty("count", books.size());
        writeJson(resp, payload);
    }

    private void handleCatalogList(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String category = trimToNull(req.getParameter("category"));
        String sortParam = req.getParameter("sort");
        if (!BookDAO.isValidSortParam(sortParam)) {
            sendBadRequest(resp, "Invalid sort parameter");
            return;
        }

        SortType sortType = BookDAO.SortType.fromParam(sortParam);
        int pageSize = parsePositiveInt(req.getParameter("size"), 20, 60);
        int page = parsePositiveInt(req.getParameter("page"), 1, 200);
        int offset = (page - 1) * pageSize;

        List<Book> books = BookDAO.findBooks(category, sortType, pageSize, offset);
        int totalItems = BookDAO.countBooks(category);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);

        JsonObject payload = new JsonObject();
    payload.add("data", toBookJsonArray(books));
        payload.addProperty("page", page);
        payload.addProperty("pageSize", pageSize);
        payload.addProperty("totalItems", totalItems);
        payload.addProperty("totalPages", totalPages);
        payload.addProperty("hasNext", page < totalPages);
        payload.addProperty("hasPrevious", page > 1 && page <= totalPages);

        writeJson(resp, payload);
    }

    private JsonArray toBookJsonArray(List<Book> books) {
        JsonArray array = new JsonArray();
        for (Book book : books) {
            JsonObject obj = toJson(book);
            array.add(obj);
        }
        return array;
    }

    private JsonArray toStringJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private JsonObject toJson(Book book) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", book.getId());
        obj.addProperty("title", book.getTitle());
        obj.addProperty("author", book.getAuthor());
        obj.addProperty("isbn", book.getIsbn());
        if (book.getPrice() != null) {
            obj.addProperty("price", book.getPrice().doubleValue());
        }
        obj.addProperty("description", book.getDescription());
        obj.addProperty("category", book.getCategory());
        obj.addProperty("stockQuantity", book.getStockQuantity());
        obj.addProperty("imageUrl", book.getImageUrl());
        if (book.getCreatedAt() != null) {
            obj.addProperty("createdAt", book.getCreatedAt().toString());
        }
        if (book.getUpdatedAt() != null) {
            obj.addProperty("updatedAt", book.getUpdatedAt().toString());
        }
        obj.addProperty("totalSold", book.getTotalSold());
        obj.addProperty("averageRating", round(book.getAverageRating(), 2));
        obj.addProperty("ratingCount", book.getRatingCount());
        obj.addProperty("favoriteCount", book.getFavoriteCount());
        return obj;
    }

    private double round(double value, int places) {
        if (places < 0) {
            return value;
        }
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    private void writeJson(HttpServletResponse resp, JsonObject payload) throws IOException {
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(payload.toString());
        }
    }

    private void sendServerError(HttpServletResponse resp, Exception ex) throws IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        JsonObject obj = new JsonObject();
        obj.addProperty("error", "SERVER_ERROR");
        obj.addProperty("message", ex.getMessage());
        writeJson(resp, obj);
    }

    private void sendBadRequest(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject obj = new JsonObject();
        obj.addProperty("error", "BAD_REQUEST");
        obj.addProperty("message", message);
        writeJson(resp, obj);
    }

    private int parsePositiveInt(String raw, int defaultValue, int max) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value <= 0) {
                return defaultValue;
            }
            return Math.min(value, max);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String trimToNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
