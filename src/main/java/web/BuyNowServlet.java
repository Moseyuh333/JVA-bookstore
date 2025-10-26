package web;

import com.google.gson.Gson;
import dao.CartDAO;
import models.CartItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "BuyNowServlet", urlPatterns = {"/api/checkout/buy-now"})
public class BuyNowServlet extends HttpServlet {

    public static final String SESSION_KEY = "CHECKOUT_BUY_NOW_ITEMS";
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> payload = readJson(request);
        Long bookId = parseId(payload.get("bookId"));
        int quantity = parseQuantity(payload.get("quantity"), 1);
        if (bookId == null || quantity <= 0) {
            sendBadRequest(response, "Dữ liệu sản phẩm không hợp lệ");
            return;
        }
        try {
            CartItem item = CartDAO.createStandaloneItem(bookId, quantity);
            item.setQuantity(quantity);
            List<BuyNowItem> items = new ArrayList<>();
            items.add(new BuyNowItem(item.getBookId(), item.getQuantity()));
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_KEY, items);
            Map<String, Object> result = buildPreview(items);
            result.put("success", true);
            response.getWriter().write(gson.toJson(result));
        } catch (SQLException ex) {
            sendServerError(response, ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        List<BuyNowItem> items = session != null ? (List<BuyNowItem>) session.getAttribute(SESSION_KEY) : null;
        if (items == null || items.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Không tìm thấy sản phẩm mua ngay");
            response.getWriter().write(gson.toJson(result));
            return;
        }
        try {
            Map<String, Object> result = buildPreview(items);
            result.put("success", true);
            response.getWriter().write(gson.toJson(result));
        } catch (SQLException ex) {
            sendServerError(response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_KEY);
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private Map<String, Object> buildPreview(List<BuyNowItem> items) throws SQLException {
        List<Map<String, Object>> renderedItems = new ArrayList<>();
        long totalQuantity = 0;
        java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
        for (BuyNowItem draft : items) {
            CartItem snapshot = CartDAO.createStandaloneItem(draft.bookId, draft.quantity);
            snapshot.setQuantity(draft.quantity);
            Map<String, Object> node = new HashMap<>();
            node.put("bookId", snapshot.getBookId());
            node.put("title", snapshot.getTitle());
            node.put("author", snapshot.getAuthor());
            node.put("imageUrl", snapshot.getImageUrl());
            node.put("unitPrice", snapshot.getUnitPrice());
            node.put("quantity", snapshot.getQuantity());
            renderedItems.add(node);
            subtotal = subtotal.add(snapshot.getUnitPrice().multiply(java.math.BigDecimal.valueOf(snapshot.getQuantity())));
            totalQuantity += snapshot.getQuantity();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("items", renderedItems);
        result.put("subtotal", subtotal);
        result.put("count", totalQuantity);
        return result;
    }

    private Map<String, Object> readJson(HttpServletRequest request) throws IOException {
        StringBuilder raw = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                raw.append(line);
            }
        }
        if (raw.length() == 0) {
            return new HashMap<>();
        }
        return gson.fromJson(raw.toString(), new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());
    }

    private Long parseId(Object raw) {
        if (raw instanceof Number) {
            long value = ((Number) raw).longValue();
            return value > 0 ? value : null;
        }
        if (raw instanceof String) {
            try {
                long value = Long.parseLong(((String) raw).trim());
                return value > 0 ? value : null;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private int parseQuantity(Object raw, int defaultValue) {
        if (raw instanceof Number) {
            int value = ((Number) raw).intValue();
            return value > 0 ? value : defaultValue;
        }
        if (raw instanceof String) {
            try {
                int value = Integer.parseInt(((String) raw).trim());
                return value > 0 ? value : defaultValue;
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(buildError(message)));
    }

    private void sendServerError(HttpServletResponse response, Exception ex) throws IOException {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(buildError("Không thể xử lý yêu cầu: " + ex.getMessage())));
    }

    private Map<String, Object> buildError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }

    private static final class BuyNowItem implements java.io.Serializable {
        private final long bookId;
        private final int quantity;

        private BuyNowItem(long bookId, int quantity) {
            this.bookId = bookId;
            this.quantity = quantity;
        }
    }
}
