package web.admin;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.sql.*;

@WebServlet(name = "AdminProductsServlet", urlPatterns = { "/api/admin/products" })
public class AdminProductsServlet extends HttpServlet {

    // ========= COMMON UTF-8 =========
    private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listProducts(req, out);
            } else if ("get".equals(action)) {
                getProduct(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                createProduct(req, out);
            } else if ("update".equals(action)) {
                updateProduct(req, out);
            } else if ("delete".equals(action)) {
                deleteProduct(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }

    private void listProducts(HttpServletRequest req, PrintWriter out) throws SQLException {
        String userRole = (String) req.getSession().getAttribute("role");
        Integer ownerId = (Integer) req.getSession().getAttribute("user_id");
        String shopId = req.getParameter("shop_id");
        String search = req.getParameter("search");
        String category = req.getParameter("category");
        String pageStr = req.getParameter("page");
        String limitStr = req.getParameter("limit");

        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int limit = limitStr != null ? Integer.parseInt(limitStr) : 20;
        int offset = (page - 1) * limit;

        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.title, b.author, b.price, b.stock_quantity, b.category, " +
            "b.description, b.image_url, b.created_at, b.updated_at, " +
            "COALESCE(s.name, 'Unknown Shop') AS shop_name, s.commission_rate " +
            "FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE 1=1"
        );

        if (shopId != null && !shopId.trim().isEmpty())
            sql.append(" AND b.shop_id = ?");
        if (search != null && !search.trim().isEmpty())
            sql.append(" AND (b.title ILIKE ? OR b.author ILIKE ? OR b.isbn ILIKE ?)");
        if (category != null && !category.trim().isEmpty())
            sql.append(" AND b.category = ?");

        sql.append(" ORDER BY b.created_at DESC LIMIT ? OFFSET ?");

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM books b WHERE 1=1");
        if (shopId != null && !shopId.trim().isEmpty())
            countSql.append(" AND b.shop_id = ?");
        if (search != null && !search.trim().isEmpty())
            countSql.append(" AND (b.title ILIKE ? OR b.author ILIKE ? OR b.isbn ILIKE ?)");
        if (category != null && !category.trim().isEmpty())
            countSql.append(" AND b.category = ?");

        if ("seller".equalsIgnoreCase(userRole) && ownerId != null) {
            sql.append(" AND s.owner_id = ?");
            countSql.append(" AND s.owner_id = ?");
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Count total
            int total = 0;
            try (PreparedStatement countStmt = conn.prepareStatement(countSql.toString())) {
                int param = 1;
                if (shopId != null && !shopId.trim().isEmpty())
                    countStmt.setInt(param++, Integer.parseInt(shopId));
                if (search != null && !search.trim().isEmpty()) {
                    String pattern = "%" + search.trim() + "%";
                    countStmt.setString(param++, pattern);
                    countStmt.setString(param++, pattern);
                    countStmt.setString(param++, pattern);
                }
                if (category != null && !category.trim().isEmpty())
                    countStmt.setString(param++, category.trim());
                if ("seller".equalsIgnoreCase(userRole) && ownerId != null) {
                    countStmt.setInt(param++, ownerId);
                }
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next())
                        total = rs.getInt(1);
                }
            }

            // Query data
            StringBuilder json = new StringBuilder("{\"products\":[");
            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                int param = 1;
                if (shopId != null && !shopId.trim().isEmpty())
                    pstmt.setInt(param++, Integer.parseInt(shopId));
                if (search != null && !search.trim().isEmpty()) {
                    String pattern = "%" + search.trim() + "%";
                    pstmt.setString(param++, pattern);
                    pstmt.setString(param++, pattern);
                    pstmt.setString(param++, pattern);
                }
                if (category != null && !category.trim().isEmpty())
                    pstmt.setString(param++, category.trim());
                if ("seller".equalsIgnoreCase(userRole) && ownerId != null) {
                    pstmt.setInt(param++, ownerId);
                }
                pstmt.setInt(param++, limit);
                pstmt.setInt(param++, offset);

                boolean first = true;
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        if (!first)
                            json.append(",");
                        first = false;
                        json.append("{")
                                .append("\"id\":").append(rs.getInt("id")).append(",")
                                .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                                .append("\"author\":\"").append(escapeJson(rs.getString("author"))).append("\",")
                                .append("\"isbn\":\"").append(escapeJson(rs.getString("isbn"))).append("\",")
                                .append("\"price\":")
                                .append(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : "0").append(",")
                                .append("\"description\":\"").append(escapeJson(rs.getString("description")))
                                .append("\",")
                                .append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",")
                                .append("\"stock_quantity\":").append(rs.getInt("stock_quantity")).append(",")
                                .append("\"image_url\":\"").append(escapeJson(rs.getString("image_url"))).append("\",")
                                .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                                .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                                .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                                .append("}");
                    }
                }
            }
            json.append("],\"total\":").append(total)
                    .append(",\"page\":").append(page)
                    .append(",\"limit\":").append(limit)
                    .append("}");
            out.write(json.toString());
        }
    }

    private void getProduct(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.description, b.category, " +
                "b.stock_quantity, b.image_url, b.created_at, b.updated_at, " +
                "COALESCE(s.name, 'Unknown Shop') as shop_name " +
                "FROM books b " +
                "LEFT JOIN shops s ON b.shop_id = s.id " +
                "WHERE b.id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    String json = "{"
                            + "\"id\":" + rs.getInt("id") + ","
                            + "\"title\":\"" + escapeJson(rs.getString("title")) + "\","
                            + "\"author\":\"" + escapeJson(rs.getString("author")) + "\","
                            + "\"isbn\":\"" + escapeJson(rs.getString("isbn")) + "\","
                            + "\"price\":" + rs.getBigDecimal("price") + ","
                            + "\"description\":\"" + escapeJson(rs.getString("description")) + "\","
                            + "\"category\":\"" + escapeJson(rs.getString("category")) + "\","
                            + "\"stock_quantity\":" + rs.getInt("stock_quantity") + ","
                            + "\"image_url\":\"" + escapeJson(rs.getString("image_url")) + "\","
                            + "\"shop_name\":\"" + escapeJson(rs.getString("shop_name")) + "\","
                            + "\"created_at\":\""
                            + (rs.getTimestamp("created_at") != null ? sdf.format(rs.getTimestamp("created_at")) : "")
                            + "\","
                            + "\"updated_at\":\""
                            + (rs.getTimestamp("updated_at") != null ? sdf.format(rs.getTimestamp("updated_at")) : "")
                            + "\""
                            + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Product not found\"}");
                }
            }
        }
    }

    private void createProduct(HttpServletRequest req, PrintWriter out) throws SQLException {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String isbn = req.getParameter("isbn");
        String priceStr = req.getParameter("price");
        String description = req.getParameter("description");
        String category = req.getParameter("category");
        String stockStr = req.getParameter("stock_quantity");
        String imageUrl = req.getParameter("image_url");
        String shopIdStr = req.getParameter("shop_id");

        if (title == null || title.trim().isEmpty() || priceStr == null || shopIdStr == null) {
            out.write("{\"error\":\"Title, price and shop_id are required\"}");
            return;
        }

        BigDecimal price = new BigDecimal(priceStr);
        int stockQuantity = stockStr != null ? Integer.parseInt(stockStr) : 0;
        int shopId = Integer.parseInt(shopIdStr);

        String sql = "INSERT INTO books (title, author, isbn, price, description, category, stock_quantity, image_url, shop_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title.trim());
            pstmt.setString(2, author != null ? author.trim() : null);
            pstmt.setString(3, isbn != null ? isbn.trim() : null);
            pstmt.setBigDecimal(4, price);
            pstmt.setString(5, description != null ? description.trim() : null);
            pstmt.setString(6, category != null ? category.trim() : null);
            pstmt.setInt(7, stockQuantity);
            pstmt.setString(8, imageUrl != null ? imageUrl.trim() : null);
            pstmt.setInt(9, shopId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Product created successfully\"}");
            } else {
                out.write("{\"error\":\"Failed to create product\"}");
            }
        }
    }

    private void updateProduct(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String isbn = req.getParameter("isbn");
        String priceStr = req.getParameter("price");
        String description = req.getParameter("description");
        String category = req.getParameter("category");
        String stockStr = req.getParameter("stock_quantity");
        String imageUrl = req.getParameter("image_url");

        if (idStr == null || title == null || title.trim().isEmpty() || priceStr == null) {
            out.write("{\"error\":\"ID, title and price are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal price = new BigDecimal(priceStr);
        int stockQuantity = stockStr != null ? Integer.parseInt(stockStr) : 0;

        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, price = ?, description = ?, " +
                "category = ?, stock_quantity = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title.trim());
            pstmt.setString(2, author != null ? author.trim() : null);
            pstmt.setString(3, isbn != null ? isbn.trim() : null);
            pstmt.setBigDecimal(4, price);
            pstmt.setString(5, description != null ? description.trim() : null);
            pstmt.setString(6, category != null ? category.trim() : null);
            pstmt.setInt(7, stockQuantity);
            pstmt.setString(8, imageUrl != null ? imageUrl.trim() : null);
            pstmt.setInt(9, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Product updated successfully\"}");
            } else {
                out.write("{\"error\":\"Product not found\"}");
            }
        }
    }

    private void deleteProduct(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Product deleted successfully\"}");
            } else {
                out.write("{\"error\":\"Product not found\"}");
            }
        }
    }

    // ========= Escape JSON safely =========
    private String escapeJson(String str) {
        if (str == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20)
                        sb.append(String.format("\\u%04x", (int) c));
                    else
                        sb.append(c);
            }
        }
        return sb.toString();
    }

}
