package web.seller;

import dao.OrderDAO;
import dao.ShopDAO;
import models.Order;
import models.OrderItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

@WebServlet("/api/seller/orders")
public class SellerOrdersServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerOrdersServlet.class.getName());
    private final Gson gson = new Gson();
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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
            } else if ("detail".equals(action)) {
                getOrderDetail(req, resp, out, shopId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Invalid action")));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in doGet /api/seller/orders", e);
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
            LOGGER.log(Level.SEVERE, "Database error in doPost /api/seller/orders", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    private void listOrders(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        String status = req.getParameter("status");
        String keyword = req.getParameter("keyword");
        String searchType = req.getParameter("searchType");
        int limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : 50;

        if (searchType != null && keyword != null && !keyword.trim().isEmpty()) {
            String trimmed = keyword.trim();
            switch (searchType) {
                case "status":
                    status = trimmed;
                    keyword = null;
                    break;
                case "orderId":
                case "customerName":
                case "all":
                default:
                    keyword = trimmed;
                    break;
            }
        }

        if (status != null && status.trim().isEmpty()) {
            status = null;
        }
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        List<OrderDAO.AdminOrderSummary> orders = OrderDAO.listOrdersForShop(shopId, status, keyword, limit);
        List<Map<String, Object>> serialized = new ArrayList<>(orders.size());

        for (OrderDAO.AdminOrderSummary summary : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", summary.id);
            item.put("code", summary.code);
            item.put("status", summary.status);
            item.put("paymentStatus", summary.paymentStatus);
            item.put("paymentMethod", summary.paymentMethod);
            item.put("totalAmount", summary.totalAmount);
            item.put("shippingFee", summary.shippingFee);
            final String orderDate = summary.orderDate != null ? summary.orderDate.format(ISO_DATE_TIME) : null;
            item.put("orderDate", orderDate);
            item.put("createdAt", orderDate);
            item.put("updatedAt", summary.updatedAt != null ? summary.updatedAt.format(ISO_DATE_TIME) : null);
            item.put("customerName", summary.customerName);
            item.put("customerEmail", summary.customerEmail);
            serialized.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("orders", serialized);
        response.put("total", serialized.size());

        out.write(gson.toJson(response));
    }

    private void getOrderStats(PrintWriter out, int shopId) throws SQLException {
        int totalOrders = OrderDAO.countTotalOrders(shopId);
        int newOrders = OrderDAO.countOrdersByStatus(shopId, "new");
        int confirmedOrders = OrderDAO.countOrdersByStatus(shopId, "confirmed");
        int shippingOrders = OrderDAO.countOrdersByStatus(shopId, "shipping");
        int deliveredOrders = OrderDAO.countOrdersByStatus(shopId, "delivered");
        int cancelledOrders = OrderDAO.countOrdersByStatus(shopId, "cancelled");
        int failedOrders = OrderDAO.countOrdersByStatus(shopId, "failed");
        int returnedOrders = OrderDAO.countOrdersByStatus(shopId, "returned");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalOrders);
        stats.put("new", newOrders);
        stats.put("confirmed", confirmedOrders);
        stats.put("shipping", shippingOrders);
        stats.put("delivered", deliveredOrders);
        stats.put("cancelled", cancelledOrders);
        stats.put("failed", failedOrders);
        stats.put("returned", returnedOrders);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);

        out.write(gson.toJson(response));
    }

    private void getOrderDetail(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, int shopId)
            throws SQLException {
        String orderIdParam = req.getParameter("order_id");
        if (orderIdParam == null || orderIdParam.isBlank()) {
            orderIdParam = req.getParameter("id");
        }

        if (orderIdParam == null || orderIdParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Missing order id")));
            return;
        }

        long orderId;
        try {
            orderId = Long.parseLong(orderIdParam);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Invalid order id")));
            return;
        }

        if (!OrderDAO.orderBelongsToShop(orderId, shopId)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(Map.of("success", false, "message", "Order not found or access denied")));
            return;
        }

        Order order = OrderDAO.fetchOrderForAdmin(orderId);
        if (order == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(Map.of("success", false, "message", "Order not found")));
            return;
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", order.getId());
        orderData.put("code", order.getCode());
        orderData.put("status", order.getStatus());
        orderData.put("paymentStatus", order.getPaymentStatus());
        orderData.put("paymentMethod", order.getPaymentMethod());
        orderData.put("paymentProvider", order.getPaymentProvider());
        orderData.put("itemsSubtotal", order.getItemsSubtotal());
        orderData.put("discountAmount", order.getDiscountAmount());
        orderData.put("shippingFee", order.getShippingFee());
        orderData.put("totalAmount", order.getTotalAmount());
        orderData.put("currency", order.getCurrency());
        orderData.put("couponCode", order.getCouponCode());
        orderData.put("customerName", order.getCustomerName());
        orderData.put("customerEmail", order.getCustomerEmail());
        orderData.put("notes", order.getNotes());
        orderData.put("shippingAddress", order.getShippingAddress());
        orderData.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        orderData.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        orderData.put("updatedAt", order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null);

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", item.getId());
            itemData.put("bookId", item.getBookId());
            itemData.put("title", item.getTitle());
            itemData.put("author", item.getAuthor());
            itemData.put("quantity", item.getQuantity());
            itemData.put("unitPrice", item.getUnitPrice());
            itemData.put("totalPrice", item.getTotalPrice());
            itemData.put("imageUrl", item.getImageUrl());
            items.add(itemData);
        }
        orderData.put("items", items);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("order", orderData);
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
