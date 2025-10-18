package web;

import com.google.gson.Gson;
import dao.OrderDAO;
import models.Order;
import utils.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/api/checkout"})
public class CheckoutServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(26000);
    private static final String MODE_BUY_NOW = "buy-now";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Long userId = AuthUtil.resolveUserId(request);
            if (userId == null) {
                sendUnauthorized(response);
                return;
            }
            Map<String, Object> payload = readJson(request);
            Long addressId = parseId(payload.get("addressId"));
            String paymentMethod = stringValue(payload.get("paymentMethod"));
            if (addressId == null) {
                sendBadRequest(response, "Vui lòng chọn địa chỉ giao hàng");
                return;
            }
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                sendBadRequest(response, "Vui lòng chọn phương thức thanh toán");
                return;
            }
            String couponCode = stringValue(payload.get("couponCode"));
            String notes = stringValue(payload.get("notes"));
            String mode = stringValue(payload.get("mode"));
            if (mode == null || mode.isEmpty()) {
                mode = "cart";
            }
            List<OrderDAO.ItemSelection> selections = parseItems(payload.get("items"));
            if (selections.isEmpty()) {
                sendBadRequest(response, "Không có sản phẩm nào được chọn để thanh toán");
                return;
            }
            HttpSession session = request.getSession(true);
            String sessionId = session.getId();
            BigDecimal shipping = SHIPPING_FEE;
            if (shipping == null || shipping.compareTo(BigDecimal.ZERO) < 0 || selections.isEmpty()) {
                shipping = BigDecimal.ZERO;
            }
            Order order = OrderDAO.checkout(userId, addressId, paymentMethod, couponCode, notes, sessionId, selections, mode, shipping);
            if (MODE_BUY_NOW.equals(mode)) {
                session.removeAttribute(BuyNowServlet.SESSION_KEY);
            }
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("order", order);
            response.getWriter().write(gson.toJson(responseBody));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private Map<String, Object> readJson(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        if (json.length() == 0) {
            return new HashMap<>();
        }
    return gson.fromJson(json.toString(), new com.google.gson.reflect.TypeToken<Map<String, Object>>() { } .getType());
    }

    private Long parseId(Object value) {
        if (value instanceof Number) {
            long parsed = ((Number) value).longValue();
            return parsed > 0 ? parsed : null;
        }
        if (value instanceof String) {
            try {
                long parsed = Long.parseLong(((String) value).trim());
                return parsed > 0 ? parsed : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() ? null : str;
    }

    private List<OrderDAO.ItemSelection> parseItems(Object raw) {
        List<OrderDAO.ItemSelection> items = new ArrayList<>();
        if (!(raw instanceof List)) {
            return items;
        }
        @SuppressWarnings("unchecked")
        List<Object> nodes = (List<Object>) raw;
        for (Object node : nodes) {
            if (!(node instanceof Map)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) node;
            Long bookId = parseId(map.get("bookId"));
            Integer quantity = parseQuantity(map.get("quantity"));
            if (bookId == null || quantity == null || quantity <= 0) {
                continue;
            }
            items.add(new OrderDAO.ItemSelection(bookId, quantity));
        }
        return items;
    }

    private Integer parseQuantity(Object raw) {
        if (raw instanceof Number) {
            int value = ((Number) raw).intValue();
            return value > 0 ? value : null;
        }
        if (raw instanceof String) {
            try {
                int value = Integer.parseInt(((String) raw).trim());
                return value > 0 ? value : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(buildError("Bạn cần đăng nhập để thanh toán")));
    }

    private void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(buildError(message)));
    }

    private Map<String, Object> buildError(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("message", message);
        return payload;
    }

    private void handleServerError(HttpServletResponse response, Exception ex) throws IOException {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(buildError("Có lỗi xảy ra: " + ex.getMessage())));
    }
}
