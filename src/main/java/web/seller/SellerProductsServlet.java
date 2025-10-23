package web.seller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.JwtUtil;
import utils.DBUtil;
import dao.ShopDAO;
import models.Shop;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerProductsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Lấy token từ header hoặc parameter
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        
        String username = null;
        if (token != null && !token.isEmpty()) {
            try {
                username = JwtUtil.validateToken(token);
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
        }
        
        if (username == null || username.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        try {
            String role = DBUtil.getUserRole(username);
            if (!"seller".equalsIgnoreCase(role)) {
                response.sendRedirect(request.getContextPath() + "/home-page.jsp");
                return;
            }
            
            int userId = DBUtil.getUserIdByUsername(username);
            int shopId = ShopDAO.getShopIdByUserId(userId);
            
            if (shopId <= 0) {
                request.setAttribute("error", "Không tìm thấy cửa hàng");
                response.sendRedirect(request.getContextPath() + "/seller-dashboard");
                return;
            }
            
            // Lấy thông tin shop
            Shop shop = ShopDAO.getShopById(shopId);
            
            // Lấy danh sách sản phẩm
            List<Map<String, Object>> products = getShopProducts(shopId);
            
            // Set attributes
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            request.setAttribute("shop", shop);
            request.setAttribute("shopId", shopId);
            request.setAttribute("products", products);
            request.setAttribute("totalProducts", products.size());
            
            request.getRequestDispatcher("/Seller/sellerProducts.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }
    
    private List<Map<String, Object>> getShopProducts(int shopId) throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, b.price, b.stock_quantity, " +
                     "b.image_url, b.category, b.created_at, " +
                     "COALESCE(bm.total_sold, 0) as total_sold " +
                     "FROM books b " +
                     "LEFT JOIN book_metrics bm ON b.id = bm.book_id " +
                     "WHERE b.shop_id = ? " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("title", rs.getString("title"));
                    product.put("author", rs.getString("author"));
                    product.put("price", rs.getDouble("price"));
                    product.put("stock", rs.getInt("stock_quantity"));
                    product.put("imageUrl", rs.getString("image_url"));
                    product.put("category", rs.getString("category"));
                    product.put("totalSold", rs.getInt("total_sold"));
                    product.put("createdAt", rs.getTimestamp("created_at"));
                    products.add(product);
                }
            }
        }
        return products;
    }
}