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
            } else if ("stats".equals(action)) {
                getProductStats(req, out);  
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

        String search = req.getParameter("search");
        String searchType = req.getParameter("searchType");
        String category = req.getParameter("category");
        String shopId = req.getParameter("shop_id");
        String status = req.getParameter("status");
        int page = req.getParameter("page") != null ? Integer.parseInt(req.getParameter("page")) : 1;
        int limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : 20;
        int offset = (page - 1) * limit;

        boolean hasStatusFilter = status != null && !status.trim().isEmpty() && !"all".equals(status.trim());

        if ("status".equals(searchType) && search != null) {
            String s = search.trim().toLowerCase();
            if (s.matches(".*(ho|động|hoạt).*")) search = "active";
            else if (s.matches(".*(ch|duy|chờ|đợi).*")) search = "pending";
            else if (s.matches(".*(kh|ngưng|ngh).*")) search = "inactive";
            else if (s.matches(".*(từ|chối|bị).*")) search = "rejected";
        }


        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.title, b.author, b.isbn, b.price, b.stock, b.category, b.status, " +
            "b.description, b.image_url, b.created_at, b.updated_at, " +
            "COALESCE(s.name, 'Unknown Shop') AS shop_name, s.commission_rate " +
            "FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE 1=1"
        );

        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE 1=1"
        );

        // Điều kiện lọc
        if (shopId != null && !shopId.trim().isEmpty()) {
            sql.append(" AND b.shop_id = ?");
            countSql.append(" AND b.shop_id = ?");
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND b.category ILIKE ?");
            countSql.append(" AND b.category ILIKE ?");
        }
        if (hasStatusFilter) {
            String normalizedStatus = normalizeStatus(status);
            if (normalizedStatus != null) {
                sql.append(" AND b.status = ?");
                countSql.append(" AND b.status = ?");
            }
        }
        if (search != null && !search.trim().isEmpty()) {
            if ("id".equals(searchType)) {
                sql.append(" AND b.id = ?");
                countSql.append(" AND b.id = ?");
            } else if ("title".equals(searchType)) {
                sql.append(" AND b.title ILIKE ?");
                countSql.append(" AND b.title ILIKE ?");
            } else if ("author".equals(searchType)) {
                sql.append(" AND b.author ILIKE ?");
                countSql.append(" AND b.author ILIKE ?");
            } else if ("category".equals(searchType)) {
                sql.append(" AND b.category ILIKE ?");
                countSql.append(" AND b.category ILIKE ?");
            } else if ("shop_name".equals(searchType)) {
                sql.append(" AND s.name ILIKE ?");
                countSql.append(" AND s.name ILIKE ?");
            } else if ("status".equals(searchType)) {
                sql.append(" AND b.status ILIKE ?");
                countSql.append(" AND b.status ILIKE ?");
            } else {
                // Default "all"
                sql.append(" AND (b.title ILIKE ? OR b.author ILIKE ? OR b.category ILIKE ? OR s.name ILIKE ? OR CAST(b.id AS TEXT) ILIKE ?)");
                countSql.append(" AND (b.title ILIKE ? OR b.author ILIKE ? OR b.category ILIKE ? OR s.name ILIKE ? OR CAST(b.id AS TEXT) ILIKE ?)");
            }
        }
        if ("seller".equalsIgnoreCase(userRole) && ownerId != null) {
            sql.append(" AND s.owner_id = ?");
            countSql.append(" AND s.owner_id = ?");
        }

        sql.append(" ORDER BY b.id ASC LIMIT ? OFFSET ?");

        try (Connection conn = DBUtil.getConnection()) {
            int total = 0;
            try (PreparedStatement psCount = conn.prepareStatement(countSql.toString())) {
                int paramCount = 1;
                if (shopId != null && !shopId.trim().isEmpty())
                    psCount.setInt(paramCount++, Integer.parseInt(shopId));
                if (category != null && !category.trim().isEmpty())
                    psCount.setString(paramCount++, "%" + category + "%");
                if (hasStatusFilter) {
                    String normalizedStatus = normalizeStatus(status);
                    if (normalizedStatus != null) {
                        psCount.setString(paramCount++, normalizedStatus);
                    }
                }
                if (search != null && !search.trim().isEmpty()) {
                    if ("id".equals(searchType)) {
                        psCount.setInt(paramCount++, Integer.parseInt(search.trim()));
                    } else {
                        String pattern = "%" + search.trim() + "%";
                        if ("title".equals(searchType) || "author".equals(searchType) || "category".equals(searchType) 
                            || "shop_name".equals(searchType) || "status".equals(searchType)) {
                            psCount.setString(paramCount++, "%" + search.trim() + "%");
                        } else {
                            psCount.setString(paramCount++, pattern);
                            psCount.setString(paramCount++, pattern);
                            psCount.setString(paramCount++, pattern);
                            psCount.setString(paramCount++, pattern);
                            psCount.setString(paramCount++, pattern);
                        }
                    }
                }
                if ("seller".equalsIgnoreCase(userRole) && ownerId != null)
                    psCount.setInt(paramCount++, ownerId);

                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }

            // Query data
            StringBuilder json = new StringBuilder("{\"products\":");
            boolean hasProducts = false;
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (shopId != null && !shopId.trim().isEmpty())
                    ps.setInt(paramIndex++, Integer.parseInt(shopId));
                if (category != null && !category.trim().isEmpty())
                    ps.setString(paramIndex++, "%" + category + "%");
                if (hasStatusFilter) {
                    String normalizedStatus = normalizeStatus(status);
                    if (normalizedStatus != null) {
                        ps.setString(paramIndex++, normalizedStatus);
                    }
                }
                if (search != null && !search.trim().isEmpty()) {
                    if ("id".equals(searchType)) {
                        ps.setInt(paramIndex++, Integer.parseInt(search.trim()));
                    } else {
                        String pattern = "%" + search.trim() + "%";
                        if ("title".equals(searchType) || "author".equals(searchType) 
                            || "category".equals(searchType) || "shop_name".equals(searchType) 
                            || "status".equals(searchType)) {
                            ps.setString(paramIndex++, "%" + search.trim() + "%");
                        } else {
                            ps.setString(paramIndex++, "%" + search.trim() + "%");
                            ps.setString(paramIndex++, "%" + search.trim() + "%");
                            ps.setString(paramIndex++, "%" + search.trim() + "%");
                            ps.setString(paramIndex++, "%" + search.trim() + "%");
                            ps.setString(paramIndex++, "%" + search.trim() + "%");
                        }
                    }
                }
                if ("seller".equalsIgnoreCase(userRole) && ownerId != null)
                    ps.setInt(paramIndex++, ownerId);
                ps.setInt(paramIndex++, limit);
                ps.setInt(paramIndex++, offset);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasProducts = true;
                        json.append("[{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                            .append("\"author\":\"").append(escapeJson(rs.getString("author"))).append("\",")
                            .append("\"isbn\":\"").append(escapeJson(rs.getString("isbn"))).append("\",")
                            .append("\"price\":").append(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : 0).append(",")
                            .append("\"stock\":").append(rs.getInt("stock")).append(",")
                            .append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",")
                            .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                            .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                            .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                            .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                            .append("}");
                        while (rs.next()) {
                            json.append(",{")
                                .append("\"id\":").append(rs.getInt("id")).append(",")
                                .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                                .append("\"author\":\"").append(escapeJson(rs.getString("author"))).append("\",")
                                .append("\"isbn\":\"").append(escapeJson(rs.getString("isbn"))).append("\",")
                                .append("\"price\":").append(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : 0).append(",")
                                .append("\"stock\":").append(rs.getInt("stock")).append(",")
                                .append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",")
                                .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                                .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                                .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                                .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                                .append("}");
                        }
                        json.append("]");
                    } else {
                        json.append("[]");
                    }
                }
            }

            // === Thống kê tồn kho toàn DB ===
            int totalBooks = 0;
            int inStock = 0;
            int outStock = 0;

            try (PreparedStatement psStat = conn.prepareStatement(
                "SELECT " +
                "COUNT(*) AS total, " +
                "COUNT(*) FILTER (WHERE COALESCE(stock, 0) > 0) AS in_stock, " +
                "COUNT(*) FILTER (WHERE COALESCE(stock, 0) = 0) AS out_stock " +
                "FROM books"
            );
                ResultSet rsStat = psStat.executeQuery()) {
                if (rsStat.next()) {
                    totalBooks = rsStat.getInt("total");
                    inStock = rsStat.getInt("in_stock");
                    outStock = rsStat.getInt("out_stock");
                }
            }

            json.append(",\"total\":").append(total)
            .append(",\"page\":").append(page)
            .append(",\"limit\":").append(limit)
            .append(",\"stats\":{")
            .append("\"total_books\":").append(totalBooks).append(",")
            .append("\"in_stock\":").append(inStock).append(",")
            .append("\"out_stock\":").append(outStock)
            .append("}")
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
        // Use the same join and fields as listProducts for consistency
        String sql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.stock, b.category, b.status, " +
                "b.description, b.image_url, b.shop_id, b.created_at, b.updated_at, " +
                "COALESCE(s.name, 'Unknown Shop') as shop_name, s.commission_rate " +
                "FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE b.id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    StringBuilder j = new StringBuilder();
                    j.append("{");
                    j.append("\"id\":").append(rs.getInt("id")).append(",");
                    j.append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",");
                    j.append("\"author\":\"").append(escapeJson(rs.getString("author"))).append("\",");
                    j.append("\"isbn\":\"").append(escapeJson(rs.getString("isbn"))).append("\",");
                    j.append("\"price\":").append(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : 0).append(",");
                    j.append("\"stock\":").append(rs.getInt("stock")).append(",");
                    j.append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",");
                    j.append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",");
                    j.append("\"image_url\":\"").append(escapeJson(rs.getString("image_url"))).append("\",");
                    j.append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",");
                    Object shopObj = rs.getObject("shop_id");
                    j.append("\"shop_id\":"); if (shopObj != null) j.append(rs.getInt("shop_id")); else j.append("null"); j.append(",");
                    j.append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",");
                    j.append("\"commission_rate\":").append(rs.getObject("commission_rate") != null ? rs.getObject("commission_rate") : "null").append(",");
                    j.append("\"created_at\":\"")
                            .append(rs.getTimestamp("created_at") != null ? sdf.format(rs.getTimestamp("created_at")) : "")
                            .append("\",");
                    j.append("\"updated_at\":\"")
                            .append(rs.getTimestamp("updated_at") != null ? sdf.format(rs.getTimestamp("updated_at")) : "")
                            .append("\"");
                    j.append("}");
                    out.write(j.toString());
                } else {
                    out.write("{\"error\":\"Product not found\"}");
                }
            }
        }
    }

    // ========= Thống kê sản phẩm =========
    private void getProductStats(HttpServletRequest req, PrintWriter out) throws SQLException {
        String userRole = (String) req.getSession().getAttribute("role");
        Integer ownerId = (Integer) req.getSession().getAttribute("user_id");

        String sql = "SELECT " +
                "COUNT(*) AS total, " +
                "COUNT(*) FILTER (WHERE COALESCE(b.stock, 0) > 0) AS in_stock, " +
                "COUNT(*) FILTER (WHERE COALESCE(b.stock, 0) <= 0) AS out_stock " +
                "FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE 1=1";

        if ("seller".equalsIgnoreCase(userRole) && ownerId != null) {
            sql += " AND s.owner_id = " + ownerId;
        }

        try (Connection conn = DBUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                int inStock = rs.getInt("in_stock");
                int outStock = rs.getInt("out_stock");

                out.write("{\"total\":" + total +
                        ",\"in_stock\":" + inStock +
                        ",\"out_stock\":" + outStock + "}");
            } else {
                out.write("{\"total\":0,\"in_stock\":0,\"out_stock\":0}");
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
        String stockStr = req.getParameter("stock");
        String imageUrl = req.getParameter("image_url");
        String shopIdStr = req.getParameter("shop_id");
        String status = req.getParameter("status");

        if (title == null || title.trim().isEmpty() || priceStr == null || shopIdStr == null) {
            out.write("{\"error\":\"Title, price and shop_id are required\"}");
            return;
        }

        BigDecimal price = new BigDecimal(priceStr);
        int stockQuantity = stockStr != null ? Integer.parseInt(stockStr) : 0;

        // Validate stock quantity - cannot be negative
        if (stockQuantity < 0) {
            out.write("{\"error\":\"Số lượng tồn kho không được âm\"}");
            return;
        }

        int shopId = Integer.parseInt(shopIdStr);

        String insertSql = "INSERT INTO books (title, author, isbn, price, description, category, stock, image_url, shop_id, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, title.trim());
            pstmt.setString(2, author != null ? author.trim() : null);
            pstmt.setString(3, isbn != null ? isbn.trim() : null);
            pstmt.setBigDecimal(4, price);
            pstmt.setString(5, description != null ? description.trim() : null);
            pstmt.setString(6, category != null ? category.trim() : null);
            pstmt.setInt(7, stockQuantity);
            pstmt.setString(8, imageUrl != null ? imageUrl.trim() : null);
            pstmt.setInt(9, shopId);
            pstmt.setString(10, status != null ? status.trim() : null);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt("id");
                    // fetch the product row with the same join used by listProducts
                    String fetchSql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.stock, b.category, b.status, " +
                            "b.description, b.image_url, b.shop_id, b.created_at, b.updated_at, " +
                            "COALESCE(s.name, 'Unknown Shop') AS shop_name, s.commission_rate " +
                            "FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE b.id = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(fetchSql)) {
                        ps2.setInt(1, newId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                StringBuilder j = new StringBuilder();
                                j.append("{");
                                j.append("\"id\":").append(rs2.getInt("id")).append(",");
                                j.append("\"title\":\"").append(escapeJson(rs2.getString("title"))).append("\",");
                                j.append("\"author\":\"").append(escapeJson(rs2.getString("author"))).append("\",");
                                j.append("\"isbn\":\"").append(escapeJson(rs2.getString("isbn"))).append("\",");
                                j.append("\"price\":").append(rs2.getBigDecimal("price") != null ? rs2.getBigDecimal("price") : 0).append(",");
                                j.append("\"stock\":").append(rs2.getInt("stock")).append(",");
                                j.append("\"category\":\"").append(escapeJson(rs2.getString("category"))).append("\",");
                                j.append("\"description\":\"").append(escapeJson(rs2.getString("description"))).append("\",");
                                j.append("\"image_url\":\"").append(escapeJson(rs2.getString("image_url"))).append("\",");
                                Object shopObj = rs2.getObject("shop_id");
                                j.append("\"shop_id\":"); if (shopObj != null) j.append(rs2.getInt("shop_id")); else j.append("null"); j.append(",");
                                j.append("\"shop_name\":\"").append(escapeJson(rs2.getString("shop_name"))).append("\",");
                                j.append("\"commission_rate\":").append(rs2.getObject("commission_rate") != null ? rs2.getObject("commission_rate") : "null").append(",");
                                j.append("\"created_at\":\"")
                                        .append(rs2.getTimestamp("created_at") != null ? sdf.format(rs2.getTimestamp("created_at")) : "")
                                        .append("\",");
                                j.append("\"updated_at\":\"")
                                        .append(rs2.getTimestamp("updated_at") != null ? sdf.format(rs2.getTimestamp("updated_at")) : "")
                                        .append("\"");
                                j.append("}");
                                out.write(j.toString());
                            } else {
                                out.write("{\"error\":\"Failed to fetch created product\"}");
                            }
                        }
                    }
                } else {
                    out.write("{\"error\":\"Failed to create product\"}");
                }
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
        String stockStr = req.getParameter("stock");
        String imageUrl = req.getParameter("image_url");
        String status = req.getParameter("status");

        if (idStr == null || title == null || title.trim().isEmpty() || priceStr == null) {
            out.write("{\"error\":\"ID, title and price are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal price = new BigDecimal(priceStr);
        int stockQuantity = stockStr != null ? Integer.parseInt(stockStr) : 0;

        // Validate stock quantity - cannot be negative
        if (stockQuantity < 0) {
            out.write("{\"error\":\"Số lượng tồn kho không được âm\"}");
            return;
        }

    String shopIdStr = req.getParameter("shop_id");
    boolean updateShop = shopIdStr != null && !shopIdStr.trim().isEmpty();

    String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, price = ?, description = ?, " +
        "category = ?, stock = ?, image_url = ?, status = ?" + (updateShop ? ", shop_id = ?" : "") + ", updated_at = CURRENT_TIMESTAMP " +
        "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idx = 1;
            pstmt.setString(idx++, title.trim());
            pstmt.setString(idx++, author != null ? author.trim() : null);
            pstmt.setString(idx++, isbn != null ? isbn.trim() : null);
            pstmt.setBigDecimal(idx++, price);
            pstmt.setString(idx++, description != null ? description.trim() : null);
            pstmt.setString(idx++, category != null ? category.trim() : null);
            pstmt.setInt(idx++, stockQuantity);
            pstmt.setString(idx++, imageUrl != null ? imageUrl.trim() : null);
            pstmt.setString(idx++, status != null ? status.trim() : null);
            if (updateShop) {
                pstmt.setInt(idx++, Integer.parseInt(shopIdStr));
            }
            pstmt.setInt(idx++, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // fetch and return updated row using same join as listProducts
                String fetchSql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.stock, b.category, b.status, " +
                        "b.description, b.image_url, b.shop_id, b.created_at, b.updated_at, " +
                        "COALESCE(s.name, 'Unknown Shop') AS shop_name, s.commission_rate " +
                        "FROM books b LEFT JOIN shops s ON b.shop_id = s.id WHERE b.id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(fetchSql)) {
                    ps2.setInt(1, id);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            StringBuilder j = new StringBuilder();
                            j.append("{");
                            j.append("\"id\":").append(rs2.getInt("id")).append(",");
                            j.append("\"title\":\"").append(escapeJson(rs2.getString("title"))).append("\",");
                            j.append("\"author\":\"").append(escapeJson(rs2.getString("author"))).append("\",");
                            j.append("\"isbn\":\"").append(escapeJson(rs2.getString("isbn"))).append("\",");
                            j.append("\"price\":").append(rs2.getBigDecimal("price") != null ? rs2.getBigDecimal("price") : 0).append(",");
                            j.append("\"stock\":").append(rs2.getInt("stock")).append(",");
                            j.append("\"category\":\"").append(escapeJson(rs2.getString("category"))).append("\",");
                            j.append("\"description\":\"").append(escapeJson(rs2.getString("description"))).append("\",");
                            j.append("\"image_url\":\"").append(escapeJson(rs2.getString("image_url"))).append("\",");
                            Object shopObj2 = rs2.getObject("shop_id");
                            j.append("\"shop_id\":"); if (shopObj2 != null) j.append(rs2.getInt("shop_id")); else j.append("null"); j.append(",");
                            j.append("\"shop_name\":\"").append(escapeJson(rs2.getString("shop_name"))).append("\",");
                            j.append("\"commission_rate\":").append(rs2.getObject("commission_rate") != null ? rs2.getObject("commission_rate") : "null").append(",");
                            j.append("\"created_at\":\"")
                                    .append(rs2.getTimestamp("created_at") != null ? sdf.format(rs2.getTimestamp("created_at")) : "")
                                    .append("\",");
                            j.append("\"updated_at\":\"")
                                    .append(rs2.getTimestamp("updated_at") != null ? sdf.format(rs2.getTimestamp("updated_at")) : "")
                                    .append("\"");
                            j.append("}");
                            out.write(j.toString());
                        } else {
                            out.write("{\"error\":\"Product not found after update\"}");
                        }
                    }
                }
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

    // ========= Normalize status for filtering =========
    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        String s = status.trim().toLowerCase();
        if ("active".equals(s) || s.matches(".*(ho|động|hoạt).*")) return "active";
        if ("pending".equals(s) || s.matches(".*(ch|duy|chờ|đợi).*")) return "pending";
        if ("inactive".equals(s) || s.matches(".*(kh|ngưng|ngh).*")) return "inactive";
        if ("rejected".equals(s) || s.matches(".*(từ|chối|bị).*")) return "rejected";
        return s; // fallback to original if no match
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
