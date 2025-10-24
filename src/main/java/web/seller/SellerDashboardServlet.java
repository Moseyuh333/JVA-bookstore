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
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class SellerDashboardServlet extends HttpServlet {
    
    // @Override
    // protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    //         throws ServletException, IOException {
        
    //     String token = request.getHeader("Authorization");
    //     if (token != null && token.startsWith("Bearer ")) {
    //         token = token.substring(7);
    //     }
        
    //     String username = null;
    //     if (token != null && !token.isEmpty()) {
    //         try {
    //             username = JwtUtil.validateToken(token);
    //         } catch (Exception e) {
    //             response.sendRedirect(request.getContextPath() + "/login.jsp");
    //             return;
    //         }
    //     }
        
    //     if (username == null || username.isEmpty()) {
    //         response.sendRedirect(request.getContextPath() + "/login.jsp");
    //         return;
    //     }

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    // Lấy token từ nhiều nguồn
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
    }
    
    // ✅ Nếu không có trong header, lấy từ parameter
    if (token == null || token.isEmpty()) {
        token = request.getParameter("token");
    }
    
    System.out.println("DEBUG - Token present: " + (token != null));
    
    String username = null;
    if (token != null && !token.isEmpty()) {
        try {
            username = JwtUtil.validateToken(token);
            System.out.println("DEBUG - Username from token: " + username);
        } catch (Exception e) {
            System.out.println("DEBUG - Invalid token: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
    }
    
    if (username == null || username.isEmpty()) {
        System.out.println("DEBUG - No username, redirecting to login");
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    
        
        try {
            String role = DBUtil.getUserRole(username);
            if (!"seller".equalsIgnoreCase(role)) {
                if ("admin".equalsIgnoreCase(role)) {
                    response.sendRedirect(request.getContextPath() + "/admin-dashboard");
                } else {
                    response.sendRedirect(request.getContextPath() + "/home-page.jsp");
                }
                return;
            }
            
            int userId = DBUtil.getUserIdByUsername(username);
            if (userId <= 0) {
                throw new SQLException("Invalid user ID");
            }
            
            // Lấy thông tin shop
            Shop shop = ShopDAO.getShopByOwnerId(userId);
            
            if (shop == null) {
                request.setAttribute("username", username);
                request.setAttribute("role", role);
                request.setAttribute("error", "Không tìm thấy cửa hàng");
                request.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(request, response);
                return;
            }
            
            // Lấy thống kê
            Map<String, Object> stats = ShopDAO.getDashboardStats(shop.getId());
            
            // Format số tiền
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            double revenue = (double) stats.getOrDefault("monthlyRevenue", 0.0);
            String formattedRevenue = currencyFormat.format(revenue) + "đ";
            
            // Set attributes
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            request.setAttribute("shop", shop);
            request.setAttribute("shopId", shop.getId());
            request.setAttribute("totalProducts", stats.getOrDefault("totalProducts", 0));
            request.setAttribute("newOrders", stats.getOrDefault("newOrders", 0));
            request.setAttribute("monthlyRevenue", formattedRevenue);
            request.setAttribute("avgRating", stats.getOrDefault("avgRating", 0.0));
            
            request.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }
}