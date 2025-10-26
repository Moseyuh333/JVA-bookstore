package web.seller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import dao.OrderDAO;
import dao.BookDAO;
import dao.ShopDAO;
import static javax.servlet.http.HttpServletResponse.*;

@WebServlet("/api/seller/analytics")
public class SellerAnalyticsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerAnalyticsServlet.class.getName());
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        Integer userId = (Integer) req.getSession().getAttribute("user_id");
        String role = (String) req.getSession().getAttribute("role");
        if (userId == null || role == null || !"seller".equalsIgnoreCase(role)) {
            resp.setStatus(SC_FORBIDDEN);
            out.write(gson.toJson(Map.of("success", false, "message", "Access denied")));
            return;
        }

        int shopId;
        try {
            shopId = resolveShopId(req, userId.intValue());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error resolving shop id", e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
            return;
        }

        if (shopId <= 0) {
            resp.setStatus(SC_FORBIDDEN);
            out.write(gson.toJson(Map.of("success", false, "message", "Shop ID not linked to user.")));
            return;
        }

        String action = req.getParameter("action");
        
        try {
            if ("summary".equals(action)) {
                // Lấy tất cả dữ liệu tổng hợp cho trang Analytics JSP
                getAnalyticsSummary(req, out, shopId);
            } else if ("revenue_data".equals(action)) {
                // Lấy dữ liệu biểu đồ doanh thu (ví dụ: 7 ngày qua)
                getRevenueData(out, shopId);
            } else {
                resp.setStatus(SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Invalid API action.")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in /api/seller/analytics", e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        }
    }

    private int resolveShopId(HttpServletRequest req, int userId) throws SQLException {
        Object cached = req.getSession().getAttribute("shop_id");
        if (cached instanceof Integer) {
            int existing = (Integer) cached;
            if (existing > 0) {
                return existing;
            }
        }
        int resolved = ShopDAO.getShopIdByUserId(userId);
        if (resolved > 0) {
            req.getSession().setAttribute("shop_id", resolved);
        }
        return resolved;
    }
    
    /**
     * Lấy tất cả dữ liệu thống kê cần thiết cho trang Analytics View.
     */
    private void getAnalyticsSummary(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        
        BigDecimal monthlyRevenue = OrderDAO.getMonthlyRevenue(shopId);
        int totalOrders = OrderDAO.countTotalOrders(shopId);
        String bestSeller = BookDAO.findBestSellerTitle(shopId);
        
        List<Map<String, Object>> dailySales = OrderDAO.getDailySalesLast7Days(shopId);
        List<Map<String, Object>> topProducts = BookDAO.getTopSellingProducts(shopId, 5);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("summary", Map.of(
            "monthlyRevenue", monthlyRevenue,
            "totalOrders", totalOrders,
            "bestSellerTitle", bestSeller,
            "dailySales", dailySales,
            "topProducts", topProducts
        ));
        
        out.write(gson.toJson(responseMap));
    }
    
    /**
     * Lấy dữ liệu doanh thu chi tiết (ví dụ: cho biểu đồ)
     */
    private void getRevenueData(PrintWriter out, int shopId) throws SQLException {
        List<Map<String, Object>> revenueData = OrderDAO.getDailySalesLast7Days(shopId);
        out.write(gson.toJson(Map.of("success", true, "data", revenueData)));
    }
}
