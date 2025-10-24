package web.seller;

import dao.OrderDAO;
import dao.ShopDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

@WebServlet("/api/seller/orders")
public class SellerOrdersServlet extends HttpServlet {

    private final Gson gson = new Gson();

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

        try {
            Integer userId = (Integer) req.getSession().getAttribute("user_id");
            String role = (String) req.getSession().getAttribute("role");

            if (userId == null || !"seller".equalsIgnoreCase(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(Map.of("success", false, "message", "Access denied")));
                return;
            }

            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(Map.of("success", false, "message", "Shop not found")));
                return;
            }

            String action = req.getParameter("action");
            
            if ("list".equals(action)) {
                listOrders(req, out, shopId);
            } else if ("stats".equals(action)) {
                getOrderStats(out, shopId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Invalid action")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        try {
            Integer userId = (Integer) req.getSession().getAttribute("user_id");
            String role = (String) req.getSession().getAttribute("role");

            if (userId == null || !"seller".equalsIgnoreCase(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(Map.of("success", false, "message", "Access denied")));
                return;
            }

            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(Map.of("success", false, "message", "Shop not found")));
                return;
            }

            String action = req.getParameter("action");
            
            if ("update_status".equals(action)) {
                updateOrderStatus(req, out, shopId, String.valueOf(userId));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Invalid action")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    private void listOrders(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        String status = req.getParameter("status");
        String keyword = req.getParameter("keyword");
        int limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : 50;

        List<OrderDAO.AdminOrderSummary> orders = OrderDAO.listOrdersForShop(shopId, status, keyword, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("orders", orders);
        response.put("total", orders.size());

        out.write(gson.toJson(response));
    }

    private void getOrderStats(PrintWriter out, int shopId) throws SQLException {
        int totalOrders = OrderDAO.countTotalOrders(shopId);
        int newOrders = OrderDAO.countOrdersByStatus(shopId, "new");
        int confirmedOrders = OrderDAO.countOrdersByStatus(shopId, "confirmed");
        int shippingOrders = OrderDAO.countOrdersByStatus(shopId, "shipping");
        int deliveredOrders = OrderDAO.countOrdersByStatus(shopId, "delivered");
        int cancelledOrders = OrderDAO.countOrdersByStatus(shopId, "cancelled");
        int returnedOrders = OrderDAO.countOrdersByStatus(shopId, "returned");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalOrders);
        stats.put("new", newOrders);
        stats.put("confirmed", confirmedOrders);
        stats.put("shipping", shippingOrders);
        stats.put("delivered", deliveredOrders);
        stats.put("cancelled", cancelledOrders);
        stats.put("returned", returnedOrders);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);

        out.write(gson.toJson(response));
    }

    private void updateOrderStatus(HttpServletRequest req, PrintWriter out, int shopId, String actor) 
            throws SQLException {
        String orderIdStr = req.getParameter("order_id");
        String newStatus = req.getParameter("status");
        String note = req.getParameter("note");

        if (orderIdStr == null || newStatus == null) {
            out.write(gson.toJson(Map.of("success", false, "message", "Missing required parameters")));
            return;
        }

        long orderId = Long.parseLong(orderIdStr);
        
        // Verify order belongs to this shop
        if (!OrderDAO.orderBelongsToShop(orderId, shopId)) {
            out.write(gson.toJson(Map.of("success", false, "message", "Order not found or access denied")));
            return;
        }

        OrderDAO.updateOrderStatus(orderId, newStatus, note, actor);

        out.write(gson.toJson(Map.of("success", true, "message", "Order status updated successfully")));
    }
}