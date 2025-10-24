package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
//import java.math.BigDecimal;
//import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.util.ArrayList;
//import java.util.List;

import utils.JwtUtil;
import utils.DBUtil; 
import dao.ShopDAO; 
//import models.Shop; 
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

// Ánh xạ cả 2 URL để Servlet này có thể phân biệt và xử lý
@WebServlet(urlPatterns = {"/seller/products", "/api/seller/products"})
public class SellerProductsServlet extends HttpServlet {

    // Đường dẫn JSP đích (Đảm bảo đúng đường dẫn file của bạn)
    private static final String PRODUCT_JSP_PATH = "/Seller/SellerProduct.jsp";

    // Hàm tiện ích: Lấy Token từ Request
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // Thêm các cách lấy token khác nếu cần (ví dụ: request.getParameter("token"))
        return null;
    }
    
    // Hàm tiện ích: Thiết lập thông tin Seller Context
    private boolean setupSellerContext(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        // Lấy token từ header (cho API) hoặc từ Session (cho View)
        String token = getTokenFromRequest(request);
        String username = null;
        
        // --- Ưu tiên lấy từ Session/Attribute nếu đang ở View ---
        username = (String) request.getSession().getAttribute("username"); 
        
        // Nếu Session chưa có hoặc là API call, thử xác thực bằng Token
        if (username == null && token != null) {
            username = JwtUtil.validateToken(token);
        }

        if (username == null || username.isEmpty()) {
            // Chỉ redirect nếu đây là yêu cầu trang web chính, không phải API
            if (request.getRequestURI().endsWith("/seller/products")) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            } else {
                 response.setStatus(SC_UNAUTHORIZED);
                 response.getWriter().write("{\"error\":\"Unauthorized: Invalid or missing token\"}");
            }
            return false;
        }

        String role = DBUtil.getUserRole(username);
        int userId = DBUtil.getUserIdByUsername(username);
        int shopId = ShopDAO.getShopIdByUserId(userId);
        
        if (!"seller".equalsIgnoreCase(role) || shopId <= 0) {
            response.setStatus(SC_FORBIDDEN);
            // Redirect hoặc trả JSON lỗi tùy thuộc vào loại request
            if (request.getRequestURI().endsWith("/seller/products")) {
                 response.sendRedirect(request.getContextPath() + "/home-page.jsp");
            } else {
                 response.getWriter().write("{\"error\":\"Forbidden: Not a valid seller or shop not found\"}");
            }
            return false;
        }
        
        // Đặt Attributes cho View (JSP) và Session cho API sau
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        request.setAttribute("shopId", shopId);
        request.setAttribute("userId", userId);
        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("user_id", userId); 
        
        return true;
    }

    // =========================================================================
    // I. Xử lý yêu cầu GET (View Display hoặc API List)
    // =========================================================================
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        try {
            if (!setupSellerContext(req, resp)) {
                return;
            }

            // 1. Phân biệt: Đây là yêu cầu Trang web (View) hay là API?
            if (req.getRequestURI().endsWith("/api/seller/products")) {
                // Đây là yêu cầu API (JSON)
                handleApiRequest(req, resp);
            } else {
                // Đây là yêu cầu View (JSP)
                // Chuyển tiếp (FORWARD) đến trang JSP
                req.getRequestDispatcher(PRODUCT_JSP_PATH).forward(req, resp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    // =========================================================================
    // II. Xử lý API GET Request (JSON)
    // =========================================================================
    
    private void handleApiRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");
        
        try {
            if ("list".equals(action)) {
                // Lấy shopId từ attribute đã được set trong setupSellerContext
                int shopId = (Integer) req.getAttribute("shopId");
                listProducts(req, out, shopId); 
            } else if ("stats".equals(action)) {
                // Lấy stats chỉ cho shop hiện tại
                int shopId = (Integer) req.getAttribute("shopId");
                getProductStats(req, out, shopId);
            } 
            // Thêm các action API khác nếu cần
            else {
                resp.setStatus(SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid API action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Internal error: " + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }

    // =========================================================================
    // III. HÀM RIÊNG TƯ (PRIVATE METHODS) CHO LOGIC CSDL
    // =========================================================================

    private void listProducts(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        // Logic để truy vấn sản phẩm của shopId cụ thể
        
        // ... (Bạn có thể sao chép và chỉnh sửa logic SQL của bạn từ SellerProductsServlet cũ)
        
        StringBuilder json = new StringBuilder("{\"products\":[");
        
        // --- LOGIC TRUY VẤN CSDL (VÍ DỤ CƠ BẢN) ---
        String sql = "SELECT id, title, author, price, stock_quantity, image_url, category FROM books WHERE shop_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            
            boolean first = true;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;
                    
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                        .append("\"author\":\"").append(escapeJson(rs.getString("author"))).append("\",")
                        .append("\"price\":").append(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : 0).append(",")
                        .append("\"stock\":").append(rs.getInt("stock_quantity")).append(",")
                        .append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",")
                        .append("\"shop_name\":\"").append(escapeJson(ShopDAO.getShopById(shopId).getName())).append("\"") // Lấy tên shop
                        .append("}");
                }
            }
        }
        
        // Giả lập thống kê đơn giản (Bạn nên dùng hàm getProductStats thực tế)
        int total = ShopDAO.countProductsByShop(shopId); 

        json.append("],")
            .append("\"total\":").append(total)
            .append(",\"stats\":{")
            .append("\"total_books\":").append(total)
            .append(",\"in_stock\":0,") // Cần tính toán thực tế
            .append("\"out_stock\":0")  // Cần tính toán thực tế
            .append("}")
            .append("}");
        
        out.write(json.toString());
    }

    private void getProductStats(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        // Bạn cần triển khai hàm này trong ShopDAO/ProductDAO để lấy thống kê
        int total = ShopDAO.countProductsByShop(shopId);
        int inStock = ShopDAO.countInStockProductsByShop(shopId);
        int outStock = total - inStock;

        out.write("{\"total\":" + total +
                  ",\"in_stock\":" + inStock +
                  ",\"out_stock\":" + outStock + "}");
    }
    
    // Hàm tiện ích: Escape JSON (giữ nguyên từ code cũ của bạn)
    private String escapeJson(String str) {
        // ... (Giữ nguyên logic escapeJson) ...
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                // ... (Thêm các escape khác)
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}