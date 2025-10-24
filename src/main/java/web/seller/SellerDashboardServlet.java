package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import utils.JwtUtil;
import utils.DBUtil;
import dao.ShopDAO;
import dao.OrderDAO;

@WebServlet("/seller/dashboard")
public class SellerDashboardServlet extends HttpServlet {

    private static final String DASHBOARD_JSP_PATH = "/Seller/sellerDashboard.jsp";

    private boolean setupSellerContext(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        
        String username = (String) request.getSession().getAttribute("username"); 
        
        if (username == null) {
            String token = (String) request.getSession().getAttribute("seller_token");
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

        if (shopId <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Shop not found for this user");
            return false;
        }

        int totalProducts = ShopDAO.countProductsByShop(shopId); 
        int inStockProducts = ShopDAO.countInStockProductsByShop(shopId);
        BigDecimal monthlyRevenue = OrderDAO.getMonthlyRevenue(shopId);
        int newOrders = OrderDAO.countOrdersByStatus(shopId, "new");
        
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        request.setAttribute("shopId", shopId);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("inStockProducts", inStockProducts);
        request.setAttribute("newOrders", newOrders);
        request.setAttribute("monthlyRevenue", monthlyRevenue.toString());
        request.setAttribute("avgRating", "0.0");
        
        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("user_id", userId);
        request.getSession().setAttribute("shop_id", shopId);

        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            if (!setupSellerContext(request, response)) {
                 return;
            }
            
            request.getRequestDispatcher(DASHBOARD_JSP_PATH).forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database initialization error.");
        }
    }
}
