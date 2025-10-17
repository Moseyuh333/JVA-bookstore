package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.OrderDAO;
import dao.OrderDAO.CheckoutRequest;
import models.Order;
import models.OrderItem;
import models.OrderStatusEntry;
import models.PaymentRecord;
import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "OrderServlet", urlPatterns = {"/api/orders", "/api/orders/*"})
public class OrderServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String path = normalize(req.getPathInfo());
        try {
            Long userId = resolveUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeError(resp, "Vui lòng đăng nhập để xem đơn hàng");
                return;
            }

            if (path == null || path.isEmpty()) {
                handleListOrders(req, resp, userId);
            } else {
                handleGetOrder(req, resp, userId, path);
            }
        } catch (SQLException ex) {
            sendServerError(resp, "Không thể tải dữ liệu đơn hàng", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String path = normalize(req.getPathInfo());
        if (!"/checkout".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeError(resp, "Endpoint không tồn tại");
            return;
        }
        try {
            Long userId = resolveUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeError(resp, "Vui lòng đăng nhập trước khi thanh toán");
                return;
            }
            CheckoutRequest payload = readCheckoutPayload(req);
            validateCheckout(payload);

            String sessionId = ensureSession(req, resp);
            Order order = OrderDAO.checkoutCart(userId, sessionId, payload);
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("order", orderToJson(order));
            resp.getWriter().write(gson.toJson(response));
        } catch (SQLException ex) {
            sendServerError(resp, "Không thể hoàn tất thanh toán", ex);
        } catch (IllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, ex.getMessage());
        }
    }

    private void handleListOrders(HttpServletRequest req, HttpServletResponse resp, long userId) throws SQLException, IOException {
        int limit = parseInt(req.getParameter("limit"), 20);
        int offset = parseInt(req.getParameter("offset"), 0);
        List<Order> orders = OrderDAO.getOrdersForUser(userId, limit, offset);
        JsonObject payload = new JsonObject();
        payload.addProperty("success", true);
        payload.add("orders", ordersToJsonArray(orders));
        resp.getWriter().write(gson.toJson(payload));
    }

    private void handleGetOrder(HttpServletRequest req, HttpServletResponse resp, long userId, String path) throws SQLException, IOException {
        if (!path.startsWith("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Đường dẫn không hợp lệ");
            return;
        }
        try {
            long orderId = Long.parseLong(path.substring(1));
            Order order = OrderDAO.getOrderDetail(userId, orderId);
            if (order == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(resp, "Không tìm thấy đơn hàng");
                return;
            }
            JsonObject payload = new JsonObject();
            payload.addProperty("success", true);
            payload.add("order", orderToJson(order));
            resp.getWriter().write(gson.toJson(payload));
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Mã đơn hàng không hợp lệ");
        }
    }

    private CheckoutRequest readCheckoutPayload(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            throw new IllegalArgumentException("Thiếu dữ liệu thanh toán");
        }
        return gson.fromJson(sb.toString(), CheckoutRequest.class);
    }

    private void validateCheckout(CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thiếu thông tin thanh toán");
        }
        if (isNullOrEmpty(request.fullName)) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên người nhận");
        }
        if (isNullOrEmpty(request.email)) {
            throw new IllegalArgumentException("Vui lòng nhập email");
        }
        if (isNullOrEmpty(request.address)) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ giao hàng");
        }
        if (request.paymentMethod != null) {
            request.paymentMethod = request.paymentMethod.trim();
        }
        if (request.currency != null) {
            request.currency = request.currency.trim().toUpperCase();
        }
    }

    private JsonObject orderToJson(Order order) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", order.getId());
        obj.addProperty("orderNumber", order.getOrderNumber());
        obj.addProperty("status", order.getStatus());
        obj.addProperty("paymentStatus", order.getPaymentStatus());
        obj.addProperty("paymentMethod", order.getPaymentMethod());
        obj.addProperty("paymentReference", order.getPaymentReference());
        obj.addProperty("currency", order.getCurrency());
        obj.addProperty("subtotal", order.getSubtotalAmount().doubleValue());
        obj.addProperty("taxAmount", order.getTaxAmount().doubleValue());
        obj.addProperty("shippingFee", order.getShippingFee().doubleValue());
        obj.addProperty("discountAmount", order.getDiscountAmount().doubleValue());
        obj.addProperty("total", order.getTotalAmount().doubleValue());
        if (order.getCreatedAt() != null) {
            obj.addProperty("createdAt", order.getCreatedAt().toString());
        }
        if (order.getUpdatedAt() != null) {
            obj.addProperty("updatedAt", order.getUpdatedAt().toString());
        }
        JsonObject shipping = new JsonObject();
        shipping.addProperty("fullName", order.getShippingFullName());
        shipping.addProperty("phone", order.getShippingPhone());
        shipping.addProperty("email", order.getShippingEmail());
        shipping.addProperty("address", order.getShippingAddress());
        shipping.addProperty("city", order.getShippingCity());
        shipping.addProperty("postalCode", order.getShippingPostalCode());
        shipping.addProperty("country", order.getShippingCountry());
        shipping.addProperty("notes", order.getShippingNotes());
        obj.add("shipping", shipping);
        if (order.getCustomerMessage() != null) {
            obj.addProperty("customerMessage", order.getCustomerMessage());
        }
        obj.add("items", orderItemsToJson(order.getItems()));
        obj.add("payments", paymentsToJson(order.getPayments()));
        obj.add("statusHistory", statusHistoryToJson(order.getStatusHistory()));
        return obj;
    }

    private com.google.gson.JsonArray ordersToJsonArray(List<Order> orders) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (Order order : orders) {
            array.add(orderToJson(order));
        }
        return array;
    }

    private com.google.gson.JsonArray orderItemsToJson(List<OrderItem> items) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (OrderItem item : items) {
            JsonObject row = new JsonObject();
            row.addProperty("id", item.getId());
            row.addProperty("orderId", item.getOrderId());
            row.addProperty("bookId", item.getBookId());
            row.addProperty("title", item.getBookTitle());
            row.addProperty("author", item.getBookAuthor());
            row.addProperty("imageUrl", item.getBookImageUrl());
            row.addProperty("quantity", item.getQuantity());
            row.addProperty("unitPrice", item.getUnitPrice().doubleValue());
            row.addProperty("totalPrice", item.getTotalPrice().doubleValue());
            if (item.getCreatedAt() != null) {
                row.addProperty("createdAt", item.getCreatedAt().toString());
            }
            array.add(row);
        }
        return array;
    }

    private com.google.gson.JsonArray paymentsToJson(List<PaymentRecord> payments) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (PaymentRecord payment : payments) {
            JsonObject row = new JsonObject();
            row.addProperty("id", payment.getId());
            row.addProperty("orderId", payment.getOrderId());
            row.addProperty("provider", payment.getProvider());
            row.addProperty("method", payment.getMethod());
            row.addProperty("status", payment.getStatus());
            row.addProperty("amount", payment.getAmount().doubleValue());
            row.addProperty("currency", payment.getCurrency());
            row.addProperty("transactionCode", payment.getTransactionCode());
            if (payment.getCreatedAt() != null) {
                row.addProperty("createdAt", payment.getCreatedAt().toString());
            }
            if (payment.getUpdatedAt() != null) {
                row.addProperty("updatedAt", payment.getUpdatedAt().toString());
            }
            array.add(row);
        }
        return array;
    }

    private com.google.gson.JsonArray statusHistoryToJson(List<OrderStatusEntry> history) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (OrderStatusEntry entry : history) {
            JsonObject row = new JsonObject();
            row.addProperty("id", entry.getId());
            row.addProperty("orderId", entry.getOrderId());
            row.addProperty("status", entry.getStatus());
            row.addProperty("note", entry.getNote());
            row.addProperty("createdBy", entry.getCreatedBy());
            if (entry.getCreatedAt() != null) {
                row.addProperty("createdAt", entry.getCreatedAt().toString());
            }
            array.add(row);
        }
        return array;
    }

    private Long resolveUserId(HttpServletRequest req) throws SQLException {
        String token = extractToken(req);
        if (token == null) {
            return null;
        }
        String subject = JwtUtil.validateToken(token);
        if (subject == null) {
            return null;
        }
        return DBUtil.resolveUserId(subject);
    }

    private String extractToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ") && bearer.length() > 7) {
            return bearer.substring(7);
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String normalize(String pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        if (pathInfo.endsWith("/")) {
            return pathInfo.substring(0, pathInfo.length() - 1);
        }
        return pathInfo;
    }

    private void writeError(HttpServletResponse resp, String message) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("success", false);
        payload.addProperty("message", message);
        resp.getWriter().write(gson.toJson(payload));
    }

    private void sendServerError(HttpServletResponse resp, String message, Exception ex) throws IOException {
        ex.printStackTrace();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writeError(resp, message);
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String ensureSession(HttpServletRequest req, HttpServletResponse resp) {
        String sessionId = req.getSession(true).getId();
        Cookie sessionCookie = new Cookie("cart_session", sessionId);
        sessionCookie.setMaxAge(60 * 60 * 24 * 30);
        sessionCookie.setHttpOnly(false);
        sessionCookie.setSecure(req.isSecure());
        sessionCookie.setPath(req.getContextPath() == null || req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        resp.addCookie(sessionCookie);
        return sessionId;
    }
}
