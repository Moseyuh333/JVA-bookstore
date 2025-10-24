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

import com.google.gson.Gson;
// Import các DAO liên quan đến Business Logic
import dao.ShopDAO;       // Để lấy thông tin shop
import dao.OrderDAO;      // Để lấy dữ liệu đơn hàng/doanh thu
import dao.BookDAO;    // Để lấy sản phẩm bán chạy
import static javax.servlet.http.HttpServletResponse.*;

@WebServlet("/api/seller/analytics")
public class SellerAnalyticsServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private int getShopIdFromSession(HttpServletRequest req) {
        Integer shopId = (Integer) req.getSession().getAttribute("shop_id");
        return shopId != null ? shopId : 0;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        
        int shopId = getShopIdFromSession(req);
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
            e.printStackTrace();
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        }
    }
    
    /**
     * Lấy tất cả dữ liệu thống kê cần thiết cho trang Analytics View.
     */
    private void getAnalyticsSummary(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        // ⚠️ Cần triển khai các hàm DAO:
        // totalOrders, getMonthlyRevenue, findTopSellingProducts, getDailySales, v.v.
        
        // --- 1. Tổng quan số liệu (Summary) ---
        BigDecimal monthlyRevenue = OrderDAO.getMonthlyRevenue(shopId); // Cần hàm này
        int totalOrders = OrderDAO.countTotalOrders(shopId);           // Cần hàm này
        String bestSeller = BookDAO.findBestSellerTitle(shopId);     // Cần hàm này
        
        // --- 2. Dữ liệu biểu đồ (Mô phỏng 7 ngày) ---
        List<Map<String, Object>> dailySales = generateMockSalesData(); // Giả định hàm generate/get
        List<Map<String, Object>> topProducts = generateMockTopProducts(); // Giả định hàm generate/get
        
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
        // Logic: OrderDAO.getDailyRevenueByShop(shopId, 7);
        // ...
        
        out.write(gson.toJson(Map.of("success", true, "data", generateMockSalesData())));
    }
    
    // ====================================================================
    // MOCK DATA (Thay thế bằng logic DAO thực tế)
    // ====================================================================
    
    private List<Map<String, Object>> generateMockSalesData() {
        return List.of(
            Map.of("date", "T2", "revenue", 500000),
            Map.of("date", "T3", "revenue", 650000),
            Map.of("date", "T4", "revenue", 400000),
            Map.of("date", "T5", "revenue", 720000),
            Map.of("date", "T6", "revenue", 810000),
            Map.of("date", "T7", "revenue", 950000),
            Map.of("date", "CN", "revenue", 1200000)
        );
    }
    
    private List<Map<String, Object>> generateMockTopProducts() {
        return List.of(
            Map.of("title", "Sách Toán", "sold", 150),
            Map.of("title", "Văn Học", "sold", 120),
            Map.of("title", "Lịch Sử VN", "sold", 85)
        );
    }
}