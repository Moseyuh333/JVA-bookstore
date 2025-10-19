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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "AdminOrdersServlet", urlPatterns = {"/api/admin/orders"})
public class AdminOrdersServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
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
            return String.valueOf(((Number) value).longValue());
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
}
