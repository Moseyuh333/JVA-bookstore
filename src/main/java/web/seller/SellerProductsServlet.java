// package web.seller;

// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import javax.servlet.annotation.WebServlet;
// import utils.JwtUtil;
// import utils.DBUtil;
// import dao.ShopDAO;
// import models.Shop;
// import java.io.IOException;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @WebServlet("/seller/products")

// public class SellerProductsServlet extends HttpServlet {
    
//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//             throws ServletException, IOException {
        
//         // Lấy token từ header hoặc parameter
//         String token = request.getHeader("Authorization");
//         if (token != null && token.startsWith("Bearer ")) {
//             token = token.substring(7);
//         }
        
//         if (token == null || token.isEmpty()) {
//             token = request.getParameter("token");
//         }
        
//         String username = null;
//         if (token != null && !token.isEmpty()) {
//             try {
//                 username = JwtUtil.validateToken(token);
//             } catch (Exception e) {
//                 response.sendRedirect(request.getContextPath() + "/login.jsp");
//                 return;
//             }
//         }
        
//         if (username == null || username.isEmpty()) {
//             response.sendRedirect(request.getContextPath() + "/login.jsp");
//             return;
//         }
        
//         try {
//             String role = DBUtil.getUserRole(username);
//             if (!"seller".equalsIgnoreCase(role)) {
//                 response.sendRedirect(request.getContextPath() + "/home-page.jsp");
//                 return;
//             }
            
//             int userId = DBUtil.getUserIdByUsername(username);
//             int shopId = ShopDAO.getShopIdByUserId(userId);
            
//             if (shopId <= 0) {
//                 request.setAttribute("error", "Không tìm thấy cửa hàng");
//                 response.sendRedirect(request.getContextPath() + "/seller-dashboard");
//                 return;
//             }
            
//             // Lấy thông tin shop
//             Shop shop = ShopDAO.getShopById(shopId);
            
//             // Lấy danh sách sản phẩm
//             List<Map<String, Object>> products = getShopProducts(shopId);
            
//             // Set attributes
//             request.setAttribute("username", username);
//             request.setAttribute("role", role);
//             request.setAttribute("shop", shop);
//             request.setAttribute("shopId", shopId);
//             request.setAttribute("products", products);
//             request.setAttribute("totalProducts", products.size());
            
//             request.getRequestDispatcher("/Seller/sellerProducts.jsp").forward(request, response);
            
//         } catch (SQLException e) {
//             e.printStackTrace();
//             response.sendRedirect(request.getContextPath() + "/error.jsp");
//         }
//     }
    
//     private List<Map<String, Object>> getShopProducts(int shopId) throws SQLException {
//         List<Map<String, Object>> products = new ArrayList<>();
//         String sql = "SELECT b.id, b.title, b.author, b.price, b.stock_quantity, " +
//                      "b.image_url, b.category, b.created_at, " +
//                      "COALESCE(bm.total_sold, 0) as total_sold " +
//                      "FROM books b " +
//                      "LEFT JOIN book_metrics bm ON b.id = bm.book_id " +
//                      "WHERE b.shop_id = ? " +
//                      "ORDER BY b.created_at DESC";
        
//         try (Connection conn = DBUtil.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, shopId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) {
//                     Map<String, Object> product = new HashMap<>();
//                     product.put("id", rs.getInt("id"));
//                     product.put("title", rs.getString("title"));
//                     product.put("author", rs.getString("author"));
//                     product.put("price", rs.getDouble("price"));
//                     product.put("stock", rs.getInt("stock_quantity"));
//                     product.put("imageUrl", rs.getString("image_url"));
//                     product.put("category", rs.getString("category"));
//                     product.put("totalSold", rs.getInt("total_sold"));
//                     product.put("createdAt", rs.getTimestamp("created_at"));
//                     products.add(product);
//                 }
//             }
//         }
//         return products;

//     }
// }


package web.seller;

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

@WebServlet(name = "SellerProductsServlet", urlPatterns = { "/api/seller/products" })
public class SellerProductsServlet extends HttpServlet {

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
        int page = req.getParameter("page") != null ? Integer.parseInt(req.getParameter("page")) : 1;
        int limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : 20;
        int offset = (page - 1) * limit;

        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.title, b.author, b.isbn, b.price, b.stock, b.category, " +
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
        if (search != null && !search.trim().isEmpty()) {
            if ("title".equals(searchType)) {
                sql.append(" AND b.title ILIKE ?");
                countSql.append(" AND b.title ILIKE ?");
            } else if ("author".equals(searchType)) {
                sql.append(" AND b.author ILIKE ?");
                countSql.append(" AND b.author ILIKE ?");
            } else if ("isbn".equals(searchType)) {
                sql.append(" AND b.isbn ILIKE ?");
                countSql.append(" AND b.isbn ILIKE ?");
            } else if ("shop_name".equals(searchType)) {
                sql.append(" AND s.name ILIKE ?");
                countSql.append(" AND s.name ILIKE ?");
            } else {
                // Default "all"
                sql.append(" AND (b.title ILIKE ? OR b.author ILIKE ? OR b.isbn ILIKE ? OR s.name ILIKE ?)");
                countSql.append(" AND (b.title ILIKE ? OR b.author ILIKE ? OR b.isbn ILIKE ? OR s.name ILIKE ?)");
            }
        }
        if ("seller".equalsIgnoreCase(userRole) && ownerId != null) {
            sql.append(" AND s.owner_id = ?");
            countSql.append(" AND s.owner_id = ?");
        }

        sql.append(" ORDER BY b.created_at DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBUtil.getConnection()) {
            int total = 0;
            try (PreparedStatement psCount = conn.prepareStatement(countSql.toString())) {
                int param = 1;
                if (shopId != null && !shopId.trim().isEmpty())
                    psCount.setInt(param++, Integer.parseInt(shopId));
                if (category != null && !category.trim().isEmpty())
                    psCount.setString(param++, "%" + category + "%");
                if (search != null && !search.trim().isEmpty()) {
                    String pattern = "%" + search.trim() + "%";
                    if ("title".equals(searchType) || "author".equals(searchType) || "isbn".equals(searchType) || "shop_name".equals(searchType)) {
                        psCount.setString(param++, pattern);
                    } else {
                        psCount.setString(param++, pattern);
                        psCount.setString(param++, pattern);
                        psCount.setString(param++, pattern);
                        psCount.setString(param++, pattern);
                    }
                }
                if ("seller".equalsIgnoreCase(userRole) && ownerId != null)
                    psCount.setInt(param++, ownerId);

                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }

            // Query data
            StringBuilder json = new StringBuilder("{\"products\":[");
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int param = 1;
                if (shopId != null && !shopId.trim().isEmpty())
                    ps.setInt(param++, Integer.parseInt(shopId));
                if (category != null && !category.trim().isEmpty())
                    ps.setString(param++, "%" + category + "%");
                if (search != null && !search.trim().isEmpty()) {
                    String pattern = "%" + search.trim() + "%";
                    if ("title".equals(searchType) || "author".equals(searchType) || "isbn".equals(searchType) || "shop_name".equals(searchType)) {
                        ps.setString(param++, pattern);
                    } else {
                        ps.setString(param++, pattern);
                        ps.setString(param++, pattern);
                        ps.setString(param++, pattern);
                        ps.setString(param++, pattern);
                    }
                }
                if ("seller".equalsIgnoreCase(userRole) && ownerId != null)
                    ps.setInt(param++, ownerId);
                ps.setInt(param++, limit);
                ps.setInt(param++, offset);

                boolean first = true;
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;
                        json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                            .append("\"author\":\"").append(escapeJson(rs.getString("author"))).append("\",")
                            .append("\"isbn\":\"").append(escapeJson(rs.getString("isbn"))).append("\",")
                            .append("\"price\":").append(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : 0).append(",")
                            .append("\"stock\":").append(rs.getInt("stock")).append(",")
                            .append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",")
                            .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                            .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                            .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                            .append("}");
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

            json.append("],")
            .append("\"total\":").append(total)
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
        String sql = "SELECT b.id, b.title, b.author, b.isbn, b.price, b.description, b.category, " +
                "b.stock, b.image_url, b.created_at, b.updated_at, " +
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
                            + "\"stock\":" + rs.getInt("stock") + ","
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

        if (title == null || title.trim().isEmpty() || priceStr == null || shopIdStr == null) {
            out.write("{\"error\":\"Title, price and shop_id are required\"}");
            return;
        }

        BigDecimal price = new BigDecimal(priceStr);
        int stockQuantity = stockStr != null ? Integer.parseInt(stockStr) : 0;
        int shopId = Integer.parseInt(shopIdStr);

        String sql = "INSERT INTO books (title, author, isbn, price, description, category, stock, image_url, shop_id) "
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
        String stockStr = req.getParameter("stock");
        String imageUrl = req.getParameter("image_url");

        if (idStr == null || title == null || title.trim().isEmpty() || priceStr == null) {
            out.write("{\"error\":\"ID, title and price are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal price = new BigDecimal(priceStr);
        int stockQuantity = stockStr != null ? Integer.parseInt(stockStr) : 0;

        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, price = ?, description = ?, " +
                "category = ?, stock = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP " +
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
