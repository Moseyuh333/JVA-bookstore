package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.OrderDAO;
import dao.ShopDAO;

@WebServlet("/seller/dashboard")
public class SellerDashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerDashboardServlet.class.getName());
    private static final String DASHBOARD_JSP_PATH = "/Seller/sellerDashboard.jsp";

    private boolean prepareDashboard(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        SellerPageHelper.SellerContext context =
                SellerPageHelper.resolveSellerContext(request, response);
        if (context == null) {
            return false;
        }

        int shopId = context.shopId();

        int totalProducts = ShopDAO.countProductsByShop(shopId);
        int inStockProducts = ShopDAO.countInStockProductsByShop(shopId);
        BigDecimal monthlyRevenue = OrderDAO.getMonthlyRevenue(shopId);
        int newOrders = OrderDAO.countOrdersByStatus(shopId, "new");

        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("inStockProducts", inStockProducts);
        request.setAttribute("newOrders", newOrders);
        request.setAttribute("monthlyRevenue", monthlyRevenue.toString());
        request.setAttribute("avgRating", "0.0");

        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            if (!prepareDashboard(request, response)) {
                 return;
            }
            
            request.getRequestDispatcher(DASHBOARD_JSP_PATH).forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error loading seller dashboard", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database initialization error.");
        }
    }
}
