package web.seller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.ShopDAO;
import models.Shop;
import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "SellerProductsServlet", urlPatterns = {"/api/seller/products"})
public class SellerProductsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerProductsServlet.class.getName());
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final Gson gson = new Gson();

    private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        try (PrintWriter out = resp.getWriter()) {
            SellerSessionContext context = requireSellerContext(req, resp, out);
            if (context == null) {
                return;
            }

            String action = normalizeAction(req.getParameter("action"), "list");
            try {
                switch (action) {
                    case "list":
                        listProducts(req, out, context);
                        break;
                    case "get":
                        getProduct(req, resp, out, context);
                        break;
                    case "stats":
                        writeStats(out, context);
                        break;
                    default:
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write(gson.toJson(error("Yêu cầu không hợp lệ")));
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Database error in SellerProductsServlet#doGet", ex);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(error("Lỗi cơ sở dữ liệu")));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        try (PrintWriter out = resp.getWriter()) {
            SellerSessionContext context = requireSellerContext(req, resp, out);
            if (context == null) {
                return;
            }

            String action = normalizeAction(req.getParameter("action"), "");
            try {
                switch (action) {
                    case "create":
                    case "add":
                        createProduct(req, resp, out, context);
                        break;
                    case "update":
                        updateProduct(req, resp, out, context);
                        break;
                    case "delete":
                        deleteProduct(req, resp, out, context);
                        break;
                    default:
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write(gson.toJson(error("Yêu cầu không hợp lệ")));
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Database error in SellerProductsServlet#doPost", ex);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(error("Lỗi cơ sở dữ liệu")));
            }
        }
    }

    private void listProducts(HttpServletRequest req, PrintWriter out, SellerSessionContext context)
            throws SQLException {
        int page = parseInt(req.getParameter("page"), 1);
        int limit = parseInt(req.getParameter("limit"), DEFAULT_LIMIT);
        limit = Math.max(1, Math.min(limit, MAX_LIMIT));
        int offset = Math.max(0, (page - 1) * limit);

        String search = safeString(req.getParameter("search"));
        String searchType = safeString(req.getParameter("searchType"));

        try (Connection conn = DBUtil.getConnection()) {
            List<Object> whereParams = new ArrayList<>();
            StringBuilder whereClause = new StringBuilder(" WHERE b.shop_id = ?");
            whereParams.add(context.shopId);

            if (!search.isEmpty()) {
                String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                switch (searchType) {
                    case "author":
                        whereClause.append(" AND LOWER(COALESCE(b.author, '')) LIKE ?");
                        break;
                    case "isbn":
                        whereClause.append(" AND LOWER(COALESCE(b.isbn, '')) LIKE ?");
                        break;
                    default:
                        whereClause.append(" AND LOWER(COALESCE(b.title, '')) LIKE ?");
                }
                whereParams.add(pattern);
            }

            int total = countProducts(conn, whereClause.toString(), whereParams);
            JsonArray items = fetchProducts(conn, whereClause.toString(), whereParams, limit, offset);
            JsonObject stats = loadInventoryStats(conn, context.shopId);

            JsonObject response = success();
            response.addProperty("total", total);
            response.addProperty("page", page);
            response.addProperty("limit", limit);
            response.add("products", items);
            response.add("stats", stats);

            out.write(gson.toJson(response));
        }
    }

    private JsonArray fetchProducts(Connection conn, String whereClause, List<Object> params, int limit, int offset)
            throws SQLException {
        String sql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.description, b.category, " +
                "b.stock_quantity, b.image_url, b.created_at, b.updated_at " +
                "FROM books b" + whereClause +
                " ORDER BY b.updated_at DESC NULLS LAST, b.id DESC LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                stmt.setObject(index++, param);
            }
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                JsonArray array = new JsonArray();
                while (rs.next()) {
                    JsonObject item = new JsonObject();
                    item.addProperty("id", rs.getInt("id"));
                    item.addProperty("title", rs.getString("title"));
                    item.addProperty("author", rs.getString("author"));
                    item.addProperty("isbn", rs.getString("isbn"));
                    item.addProperty("price", rs.getBigDecimal("price"));
                    item.addProperty("description", rs.getString("description"));
                    item.addProperty("category", rs.getString("category"));
                    item.addProperty("stock_quantity", rs.getInt("stock_quantity"));
                    item.addProperty("image_url", rs.getString("image_url"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (createdAt != null) {
                        item.addProperty("created_at", createdAt.toInstant().toString());
                    }
                    if (updatedAt != null) {
                        item.addProperty("updated_at", updatedAt.toInstant().toString());
                    }
                    array.add(item);
                }
                return array;
            }
        }
    }

    private int countProducts(Connection conn, String whereClause, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books b" + whereClause;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                stmt.setObject(index++, param);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void getProduct(HttpServletRequest req, HttpServletResponse resp, PrintWriter out,
                            SellerSessionContext context) throws SQLException {
        int id = parseInt(req.getParameter("id"), -1);
        if (id <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(error("Thiếu mã sản phẩm")));
            return;
        }

        String sql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.description, b.category, " +
                "b.stock_quantity, b.image_url, b.created_at, b.updated_at " +
                "FROM books b WHERE b.id = ? AND b.shop_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, context.shopId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JsonObject item = new JsonObject();
                    item.addProperty("id", rs.getInt("id"));
                    item.addProperty("title", rs.getString("title"));
                    item.addProperty("author", rs.getString("author"));
                    item.addProperty("isbn", rs.getString("isbn"));
                    item.addProperty("price", rs.getBigDecimal("price"));
                    item.addProperty("description", rs.getString("description"));
                    item.addProperty("category", rs.getString("category"));
                    item.addProperty("stock_quantity", rs.getInt("stock_quantity"));
                    item.addProperty("image_url", rs.getString("image_url"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (createdAt != null) {
                        item.addProperty("created_at", createdAt.toInstant().toString());
                    }
                    if (updatedAt != null) {
                        item.addProperty("updated_at", updatedAt.toInstant().toString());
                    }

                    JsonObject response = success();
                    response.add("product", item);
                    out.write(gson.toJson(response));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(error("Không tìm thấy sản phẩm")));
                }
            }
        }
    }

    private void writeStats(PrintWriter out, SellerSessionContext context) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            JsonObject stats = loadInventoryStats(conn, context.shopId);
            JsonObject response = success();
            response.add("stats", stats);
            out.write(gson.toJson(response));
        }
    }

    private JsonObject loadInventoryStats(Connection conn, int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_books, " +
                "COUNT(*) FILTER (WHERE COALESCE(stock_quantity, 0) > 0) AS in_stock, " +
                "COUNT(*) FILTER (WHERE COALESCE(stock_quantity, 0) <= 0) AS out_stock " +
                "FROM books WHERE shop_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shopId);
            try (ResultSet rs = stmt.executeQuery()) {
                JsonObject stats = new JsonObject();
                if (rs.next()) {
                    stats.addProperty("total_books", rs.getInt("total_books"));
                    stats.addProperty("in_stock", rs.getInt("in_stock"));
                    stats.addProperty("out_stock", rs.getInt("out_stock"));
                } else {
                    stats.addProperty("total_books", 0);
                    stats.addProperty("in_stock", 0);
                    stats.addProperty("out_stock", 0);
                }
                return stats;
            }
        }
    }

    private void createProduct(HttpServletRequest req, HttpServletResponse resp, PrintWriter out,
                               SellerSessionContext context) throws SQLException {
        String title = safeString(req.getParameter("title"));
        String priceRaw = safeString(req.getParameter("price"));
        String stockRaw = safeString(req.getParameter("stock"));

        if (title.isEmpty() || priceRaw.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(error("Tên sản phẩm và giá là bắt buộc")));
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceRaw);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(error("Giá sản phẩm không hợp lệ")));
            return;
        }

        int stock = parseInt(stockRaw, 0);
        String author = safeString(req.getParameter("author"));
        String isbn = safeString(req.getParameter("isbn"));
        String description = safeString(req.getParameter("description"));
        String category = safeString(req.getParameter("category"));
        String imageUrl = safeString(req.getParameter("image_url"));

        Shop shop = ShopDAO.getShopById(context.shopId);
        String shopName = shop != null ? shop.getName() : null;

        String sql = "INSERT INTO books (title, author, isbn, price, description, category, stock_quantity, " +
                "image_url, shop_id, shop_name, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            setNullableString(stmt, 2, author);
            setNullableString(stmt, 3, isbn);
            stmt.setBigDecimal(4, price);
            setNullableString(stmt, 5, description);
            setNullableString(stmt, 6, category);
            stmt.setInt(7, Math.max(0, stock));
            setNullableString(stmt, 8, imageUrl);
            stmt.setInt(9, context.shopId);
            setNullableString(stmt, 10, shopName);
            stmt.executeUpdate();
        }

        out.write(gson.toJson(success("Tạo sản phẩm thành công")));
    }

    private void updateProduct(HttpServletRequest req, HttpServletResponse resp, PrintWriter out,
                               SellerSessionContext context) throws SQLException {
        int id = parseInt(req.getParameter("id"), -1);
        String title = safeString(req.getParameter("title"));
        String priceRaw = safeString(req.getParameter("price"));

        if (id <= 0 || title.isEmpty() || priceRaw.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(error("Thiếu dữ liệu bắt buộc")));
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceRaw);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(error("Giá sản phẩm không hợp lệ")));
            return;
        }

        int stock = parseInt(req.getParameter("stock"), 0);
        String author = safeString(req.getParameter("author"));
        String isbn = safeString(req.getParameter("isbn"));
        String description = safeString(req.getParameter("description"));
        String category = safeString(req.getParameter("category"));
        String imageUrl = safeString(req.getParameter("image_url"));

        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, price = ?, description = ?, " +
                "category = ?, stock_quantity = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND shop_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            setNullableString(stmt, 2, author);
            setNullableString(stmt, 3, isbn);
            stmt.setBigDecimal(4, price);
            setNullableString(stmt, 5, description);
            setNullableString(stmt, 6, category);
            stmt.setInt(7, Math.max(0, stock));
            setNullableString(stmt, 8, imageUrl);
            stmt.setInt(9, id);
            stmt.setInt(10, context.shopId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(error("Không tìm thấy sản phẩm cần cập nhật")));
                return;
            }
        }

        out.write(gson.toJson(success("Cập nhật sản phẩm thành công")));
    }

    private void deleteProduct(HttpServletRequest req, HttpServletResponse resp, PrintWriter out,
                               SellerSessionContext context) throws SQLException {
        int id = parseInt(req.getParameter("id"), -1);
        if (id <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(error("Thiếu mã sản phẩm")));
            return;
        }

        String sql = "DELETE FROM books WHERE id = ? AND shop_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, context.shopId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(error("Không tìm thấy sản phẩm cần xóa")));
                return;
            }
        }

        out.write(gson.toJson(success("Xóa sản phẩm thành công")));
    }

    private SellerSessionContext requireSellerContext(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(error("Bạn chưa đăng nhập")));
            return null;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        if (userId == null || role == null || !"seller".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write(gson.toJson(error("Bạn không có quyền truy cập")));
            return null;
        }

        try {
            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write(gson.toJson(error("Bạn cần đăng ký shop trước khi quản lý sản phẩm")));
                return null;
            }
            return new SellerSessionContext(userId, shopId);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to resolve shop for seller " + userId, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(error("Không thể xác định shop của bạn")));
            return null;
        }
    }

    private JsonObject success() {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", true);
        return obj;
    }

    private JsonObject success(String message) {
        JsonObject obj = success();
        if (message != null && !message.isBlank()) {
            obj.addProperty("message", message);
        }
        return obj;
    }

    private JsonObject error(String message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", false);
        obj.addProperty("message", message);
        return obj;
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String normalizeAction(String action, String defaultValue) {
        if (action == null || action.isBlank()) {
            return defaultValue;
        }
        return action.trim().toLowerCase(Locale.ROOT);
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            stmt.setNull(index, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }

    private static final class SellerSessionContext {
        private final int userId;
        private final int shopId;

        private SellerSessionContext(int userId, int shopId) {
            this.userId = userId;
            this.shopId = shopId;
        }
    }
}
