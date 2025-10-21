package web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.OrderDAO;
import models.Order;
import models.OrderStatusHistory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@WebServlet(name = "AdminOrdersServlet", urlPatterns = {"/api/admin/orders"})
public class AdminOrdersServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private static final Set<String> ALLOWED_PAYMENT_METHODS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("cod", "vnpay", "momo")));
    private static final Set<String> ALLOWED_PAYMENT_STATUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("unpaid", "processing", "paid", "failed", "refunded")));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (!isAuthorized(req, resp)) {
            return;
        }
        String action = valueOrDefault(req.getParameter("action"), "list");
        try {
            switch (action.toLowerCase(Locale.US)) {
                case "detail":
                    handleDetail(req, resp);
                    break;
                case "timeline":
                    handleTimeline(req, resp);
                    break;
                case "list":
                default:
                    handleList(req, resp);
                    break;
            }
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Database error: " + ex.getMessage());
            resp.getWriter().write(gson.toJson(body));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (!isAuthorized(req, resp)) {
            return;
        }
        Map<String, Object> payload = readJsonBody(req);
        String action = valueOrDefault(req.getParameter("action"), "");
        if (payload.containsKey("action")) {
            Object candidate = payload.get("action");
            if (candidate instanceof String) {
                action = (String) candidate;
            }
        }
        action = action == null ? "" : action.trim().toLowerCase(Locale.US);
        try {
            if ("update-status".equals(action)) {
                handleUpdateStatus(req, resp, payload);
            } else if ("update-info".equals(action)) {
                handleUpdateInfo(req, resp, payload);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> body = new HashMap<>();
                body.put("success", false);
                body.put("message", "Unsupported action");
                resp.getWriter().write(gson.toJson(body));
            }
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", ex.getMessage());
            resp.getWriter().write(gson.toJson(body));
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String status = req.getParameter("status");
        String keyword = req.getParameter("q");
        int limit = parsePositiveInt(req.getParameter("limit"), 50, 200);
        List<OrderDAO.AdminOrderSummary> orders = OrderDAO.listOrdersForAdmin(status, keyword, limit);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("orders", orders);
        body.put("count", orders.size());
        resp.getWriter().write(gson.toJson(body));
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Long orderId = extractOrderId(req, null);
        if (orderId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Missing order id");
            resp.getWriter().write(gson.toJson(body));
            return;
        }
        Order order = OrderDAO.fetchOrderForAdmin(orderId);
        List<OrderStatusHistory> timeline = OrderDAO.findStatusTimelineForAdmin(orderId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("order", order);
        body.put("timeline", timeline);
        resp.getWriter().write(gson.toJson(body));
    }

    private void handleTimeline(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Long orderId = extractOrderId(req, null);
        if (orderId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Missing order id");
            resp.getWriter().write(gson.toJson(body));
            return;
        }
        List<OrderStatusHistory> timeline = OrderDAO.findStatusTimelineForAdmin(orderId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("timeline", timeline);
        resp.getWriter().write(gson.toJson(body));
    }

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> payload) throws IOException, SQLException {
        Long orderId = extractOrderId(req, payload);
        if (orderId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Missing order id");
            resp.getWriter().write(gson.toJson(body));
            return;
        }
        String status = firstNonBlank(stringValue(payload.get("status")), req.getParameter("status"));
        if (status == null || status.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Missing status value");
            resp.getWriter().write(gson.toJson(body));
            return;
        }
        String note = firstNonBlank(stringValue(payload.get("note")), req.getParameter("note"));
        OrderDAO.updateOrderStatus(orderId, status, note, "admin-panel");
        Order order = OrderDAO.fetchOrderForAdmin(orderId);
        List<OrderStatusHistory> timeline = OrderDAO.findStatusTimelineForAdmin(orderId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("order", order);
        body.put("timeline", timeline);
        resp.getWriter().write(gson.toJson(body));
    }

    private void handleUpdateInfo(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> payload) throws IOException, SQLException {
        Long orderId = extractOrderId(req, payload);
        if (orderId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Missing order id");
            resp.getWriter().write(gson.toJson(body));
            return;
        }

        OrderDAO.OrderUpdateCommand command = new OrderDAO.OrderUpdateCommand();
        boolean hasUpdates = false;

        if (hasPayloadKey(payload, "paymentMethod", "payment_method") || hasParameter(req, "paymentMethod", "payment_method")) {
            String raw = valueFrom(payload, req, "paymentMethod", "payment_method");
            String normalized = trimToNull(raw);
            if (normalized != null) {
                normalized = normalized.toLowerCase(Locale.US);
                if (!ALLOWED_PAYMENT_METHODS.contains(normalized)) {
                    throw new SQLException("Phương thức thanh toán không hợp lệ");
                }
            }
            command.withPaymentMethod(normalized);
            hasUpdates = true;
        }

        if (hasPayloadKey(payload, "paymentStatus", "payment_status") || hasParameter(req, "paymentStatus", "payment_status")) {
            String raw = valueFrom(payload, req, "paymentStatus", "payment_status");
            String normalized = trimToNull(raw);
            if (normalized != null) {
                normalized = normalized.toLowerCase(Locale.US);
                if (!ALLOWED_PAYMENT_STATUSES.contains(normalized)) {
                    throw new SQLException("Trạng thái thanh toán không hợp lệ");
                }
            }
            command.withPaymentStatus(normalized);
            hasUpdates = true;
        }

        if (hasPayloadKey(payload, "paymentProvider", "payment_provider") || hasParameter(req, "paymentProvider", "payment_provider")) {
            String raw = valueFrom(payload, req, "paymentProvider", "payment_provider");
            String normalized = trimToNull(raw);
            command.withPaymentProvider(normalized);
            hasUpdates = true;
        }

        if (hasPayloadKey(payload, "shippingAddress", "shipping_address") || hasParameter(req, "shippingAddress", "shipping_address")) {
            String raw = valueFrom(payload, req, "shippingAddress", "shipping_address");
            String normalized = raw == null ? null : raw.trim();
            if (normalized != null && normalized.isEmpty()) {
                normalized = null;
            }
            if (normalized != null && normalized.length() > 1000) {
                normalized = normalized.substring(0, 1000);
            }
            command.withShippingAddress(normalized);
            hasUpdates = true;
        }

        if (hasPayloadKey(payload, "shippingFee", "shipping_fee") || hasParameter(req, "shippingFee", "shipping_fee")) {
            String raw = valueFrom(payload, req, "shippingFee", "shipping_fee");
            String normalized = raw == null ? "" : raw.trim();
            BigDecimal shippingFee;
            if (normalized.isEmpty()) {
                shippingFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                try {
                    shippingFee = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
                } catch (NumberFormatException ex) {
                    throw new SQLException("Phí vận chuyển phải là số hợp lệ");
                }
                if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Phí vận chuyển không thể âm");
                }
            }
            command.withShippingFee(shippingFee);
            hasUpdates = true;
        }

        if (hasPayloadKey(payload, "notes", "note") || hasParameter(req, "notes", "note")) {
            String raw = valueFrom(payload, req, "notes", "note");
            String normalized = trimToNull(raw);
            if (normalized != null && normalized.length() > 2000) {
                normalized = normalized.substring(0, 2000);
            }
            command.withNotes(normalized);
            hasUpdates = true;
        }

        if (hasPayloadKey(payload, "couponCode", "coupon_code") || hasParameter(req, "couponCode", "coupon_code")) {
            String raw = valueFrom(payload, req, "couponCode", "coupon_code");
            String normalized = trimToNull(raw);
            if (normalized != null) {
                normalized = normalized.toUpperCase(Locale.US);
                if (normalized.length() > 50) {
                    normalized = normalized.substring(0, 50);
                }
            }
            command.withCouponCode(normalized);
            hasUpdates = true;
        }

        if (!hasUpdates) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Không có dữ liệu để cập nhật");
            resp.getWriter().write(gson.toJson(body));
            return;
        }

        Order order = OrderDAO.updateOrderDetails(orderId, command);
        List<OrderStatusHistory> timeline = OrderDAO.findStatusTimelineForAdmin(orderId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("order", order);
        body.put("timeline", timeline);
        body.put("message", "Cập nhật thông tin đơn hàng thành công");
        resp.getWriter().write(gson.toJson(body));
    }

    private int parsePositiveInt(String raw, int defaultValue, int maxValue) {
        if (raw == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value <= 0) {
                return defaultValue;
            }
            return Math.min(value, maxValue);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Map<String, Object> readJsonBody(HttpServletRequest req) throws IOException {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.US).contains("application/json")) {
            return new HashMap<>();
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, Object>>() { }.getType();
        Map<String, Object> parsed = gson.fromJson(sb.toString(), type);
        return parsed != null ? parsed : new HashMap<>();
    }

    private Long extractOrderId(HttpServletRequest req, Map<String, Object> payload) {
        if (payload != null) {
            Long fromPayload = extractLong(payload.get("orderId"));
            if (fromPayload == null) {
                fromPayload = extractLong(payload.get("id"));
            }
            if (fromPayload != null) {
                return fromPayload;
            }
        }
        return extractLong(req.getParameter("orderId"), req.getParameter("id"));
    }

    private Long extractLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String trimmed = ((String) value).trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private Long extractLong(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            Long parsed = extractLong((Object) candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number) {
            Number number = (Number) value;
            if (number instanceof Double || number instanceof Float) {
                return BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros().toPlainString();
            }
            return number.toString();
        }
        return null;
    }

    private boolean hasPayloadKey(Map<String, Object> payload, String... keys) {
        if (payload == null || keys == null) {
            return false;
        }
        for (String key : keys) {
            if (key != null && payload.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasParameter(HttpServletRequest req, String... keys) {
        if (req == null || keys == null) {
            return false;
        }
        Map<String, String[]> params = req.getParameterMap();
        for (String key : keys) {
            if (key != null && params.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private String valueFrom(Map<String, Object> payload, HttpServletRequest req, String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            if (payload != null && payload.containsKey(key)) {
                String value = stringValue(payload.get(key));
                if (value != null || payload.get(key) == null) {
                    return value;
                }
            }
            String param = req.getParameter(key);
            if (param != null) {
                return param;
            }
        }
        return null;
    }

    private String firstNonBlank(String first, String fallback) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback.trim();
        }
        return null;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    private boolean isAuthorized(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isLocalhost(req)) {
            return true;
        }
        String expected = getAdminSecret();
        String paramSecret = trimToNull(req.getParameter("secret"));
        String headerSecret = trimToNull(req.getHeader("X-Admin-Secret"));
        if (expected.equals(paramSecret) || expected.equals(headerSecret)) {
            return true;
        }
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Forbidden");
        resp.getWriter().write(gson.toJson(body));
        return false;
    }

    private boolean isLocalhost(HttpServletRequest req) {
        String remote = req.getRemoteAddr();
        return "127.0.0.1".equals(remote) || "0:0:0:0:0:0:0:1".equals(remote) || "::1".equals(remote);
    }

    private String getAdminSecret() {
        String env = System.getenv("ADMIN_PANEL_SECRET");
        if (env != null) {
            env = env.trim();
            if (!env.isEmpty()) {
                return env;
            }
        }
        return "dev-secret-key-change-me";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
