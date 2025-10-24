// package web.seller;

// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import utils.JwtUtil;
// import utils.DBUtil;
// import dao.ShopDAO;
// import models.Shop;
// import java.io.IOException;
// import java.sql.SQLException;
// import java.text.NumberFormat;
// import java.util.Locale;
// import java.util.Map;

// public class SellerDashboardServlet extends HttpServlet {
    
    

//     @Override
// protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//         throws ServletException, IOException {
    
//     // Lấy token từ nhiều nguồn
//     String token = request.getHeader("Authorization");
//     if (token != null && token.startsWith("Bearer ")) {
//         token = token.substring(7);
//     }
    
//     // ✅ Nếu không có trong header, lấy từ parameter
//     if (token == null || token.isEmpty()) {
//         token = request.getParameter("token");
//     }
    
//     System.out.println("DEBUG - Token present: " + (token != null));
    
//     String username = null;
//     if (token != null && !token.isEmpty()) {
//         try {
//             username = JwtUtil.validateToken(token);
//             System.out.println("DEBUG - Username from token: " + username);
//         } catch (Exception e) {
//             System.out.println("DEBUG - Invalid token: " + e.getMessage());
//             response.sendRedirect(request.getContextPath() + "/login.jsp");
//             return;
//         }
//     }
    
//     if (username == null || username.isEmpty()) {
//         System.out.println("DEBUG - No username, redirecting to login");
//         response.sendRedirect(request.getContextPath() + "/login.jsp");
//         return;
//     }
    
    
        
//         try {
//             String role = DBUtil.getUserRole(username);
//             if (!"seller".equalsIgnoreCase(role)) {
//                 if ("admin".equalsIgnoreCase(role)) {
//                     response.sendRedirect(request.getContextPath() + "/admin-dashboard");
//                 } else {
//                     response.sendRedirect(request.getContextPath() + "/home-page.jsp");
//                 }
//                 return;
//             }
            
//             int userId = DBUtil.getUserIdByUsername(username);
//             if (userId <= 0) {
//                 throw new SQLException("Invalid user ID");
//             }
            
//             // Lấy thông tin shop
//             Shop shop = ShopDAO.getShopByOwnerId(userId);
            
//             if (shop == null) {
//                 request.setAttribute("username", username);
//                 request.setAttribute("role", role);
//                 request.setAttribute("error", "Không tìm thấy cửa hàng");
//                 request.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(request, response);
//                 return;
//             }
            
//             // Lấy thống kê
//             Map<String, Object> stats = ShopDAO.getDashboardStats(shop.getId());
            
//             // Format số tiền
//             NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
//             double revenue = (double) stats.getOrDefault("monthlyRevenue", 0.0);
//             String formattedRevenue = currencyFormat.format(revenue) + "đ";
            
//             // Set attributes
//             request.setAttribute("username", username);
//             request.setAttribute("role", role);
//             request.setAttribute("shop", shop);
//             request.setAttribute("shopId", shop.getId());
//             request.setAttribute("totalProducts", stats.getOrDefault("totalProducts", 0));
//             request.setAttribute("newOrders", stats.getOrDefault("newOrders", 0));
//             request.setAttribute("monthlyRevenue", formattedRevenue);
//             request.setAttribute("avgRating", stats.getOrDefault("avgRating", 0.0));
            
//             request.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(request, response);
            
//         } catch (SQLException e) {
//             e.printStackTrace();
//             response.sendRedirect(request.getContextPath() + "/error.jsp");
//         }
//     }
// }


package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
//import models.Shop;

import utils.JwtUtil;
import utils.DBUtil;
import dao.ShopDAO; // Cần thiết để lấy shop ID
//import models.Shop;  // Cần thiết để lấy thông tin shop

@WebServlet("/seller/dashboard") // PHẢI MATCH VỚI HREF TRÊN DASHBOARD
public class SellerDashboardServlet extends HttpServlet {

    private static final String DASHBOARD_JSP_PATH = "/Seller/sellerDashboard.jsp";

    // Hàm tiện ích: Thiết lập thông tin Seller Context (Tương tự SellerProductsServlet)
    private boolean setupSellerContext(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        
        // 1. Lấy token (Ưu tiên từ Session nếu đã có, hoặc từ Cookie/Header)
        String username = (String) request.getSession().getAttribute("username"); 
        
        // GIẢ ĐỊNH: Nếu username không có, bạn cần lấy từ token và xác thực
        if (username == null) {
            String token = (String) request.getSession().getAttribute("seller_token"); // Hoặc lấy từ Cookie
            // Nếu có JWT Filter, nó đã xử lý token. Nếu không, bạn phải tự xử lý:
            if (token != null) {
                username = JwtUtil.validateToken(token);
            }
        }
        
        if (username == null || username.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return false;
        }

        String role = DBUtil.getUserRole(username);
        int userId = DBUtil.getUserIdByUsername(username);
        int shopId = ShopDAO.getShopIdByUserId(userId);
        
        if (!"seller".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Not a Seller");
            return false;
        }

        // 2. Lấy thông tin Dashboard Stats (Giả định)
        // Bạn cần triển khai các hàm này trong DAO (ví dụ: OrderDAO, ProductDAO)
        // int totalProducts = ShopDAO.countProductsByShop(shopId); 
        // double monthlyRevenue = OrderDAO.getMonthlyRevenue(shopId);
        
        // 3. Set Attributes (Truyền dữ liệu và Context cho JSP)
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        request.setAttribute("shopId", shopId);
        
        // Đặt dữ liệu thống kê giả định để JSP không bị lỗi
        request.setAttribute("totalProducts", 0); // Thay bằng giá trị thực
        request.setAttribute("newOrders", 0);     // Thay bằng giá trị thực
        request.setAttribute("monthlyRevenue", "0đ"); // Thay bằng giá trị thực
        request.setAttribute("avgRating", "0.0"); // Thay bằng giá trị thực
        
        // 4. Cập nhật Session (Quan trọng cho các request sau)
        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("user_id", userId);
        request.getSession().setAttribute("shop_id", shopId);

        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // 1. Xác thực và thiết lập Context
            if (!setupSellerContext(request, response)) {
                 return; // Đã redirect đến login
            }
            
            // 2. Chuyển tiếp (FORWARD) đến trang Dashboard JSP
            request.getRequestDispatcher(DASHBOARD_JSP_PATH).forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database initialization error.");
        }
    }
}