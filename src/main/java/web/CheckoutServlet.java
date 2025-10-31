package web;

import com.google.gson.Gson;
import dao.OrderDAO;
import dao.ShipperDAO;
import dao.UserAddressDAO;
import models.Order;
import models.Shipper;
import models.ShippingQuote;
import models.UserAddress;
import utils.AuthUtil;
import utils.DBUtil;
import utils.ShippingCalculator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/api/checkout", "/checkout"})
public class CheckoutServlet extends HttpServlet {

    private final Gson gson = new Gson();
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

            // Kiểm tra status user trước khi cho phép thanh toán
            if (!isUserAllowedToShop(request)) {
                sendForbidden(response, "Tài khoản của bạn đã bị hạn chế quyền mua hàng");
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
            OrderDAO.PaymentDetails paymentDetails;
            try {
                paymentDetails = parsePaymentDetails(paymentMethod, payload.get("paymentDetails"));
            } catch (IllegalArgumentException ex) {
                sendBadRequest(response, ex.getMessage());
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
            UserAddress address = UserAddressDAO.findById(userId, addressId);
            if (address == null) {
                sendBadRequest(response, "�?��<a ch��% giao hA�ng khA'ng h���p l���.");
                return;
            }

            List<Shipper> shippers = ShipperDAO.findActiveShippers();
            ShippingQuote shippingQuote = ShippingCalculator.calculateQuote(address, shippers);
            BigDecimal shipping = shippingQuote != null ? shippingQuote.getFee() : BigDecimal.ZERO;
            if (shipping == null || shipping.compareTo(BigDecimal.ZERO) < 0 || selections.isEmpty()) {
                shipping = BigDecimal.ZERO;
            }

            Order order = OrderDAO.checkout(userId, addressId, paymentMethod, paymentDetails, couponCode, notes, sessionId, selections, mode, shipping, shippingQuote);
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
            @SuppressWarnings("unchecked")
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

    private OrderDAO.PaymentDetails parsePaymentDetails(String paymentMethod, Object rawDetails) {
        if (paymentMethod == null) {
            return null;
        }
        String normalized = paymentMethod.trim().toLowerCase(Locale.ROOT);
        if (!"vnpay".equals(normalized) && !"momo".equals(normalized)) {
            return null;
        }
        Map<String, Object> map = asMap(rawDetails);
        if (map == null) {
            throw new IllegalArgumentException("Vui lòng nhập thông tin ví điện tử.");
        }
        String cardNumber = stringValue(map.get("cardNumber"));
        String expiryMonth = stringValue(map.get("expiryMonth"));
        String expiryYear = stringValue(map.get("expiryYear"));
        if (cardNumber == null || cardNumber.replaceAll("\\D", "").length() < 12) {
            throw new IllegalArgumentException("Số thẻ/ ví không hợp lệ.");
        }
        if (expiryMonth == null || expiryYear == null) {
            throw new IllegalArgumentException("Thiếu thông tin hạn thẻ.");
        }
        return OrderDAO.PaymentDetails.wallet(normalized, cardNumber, expiryMonth, expiryYear);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
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

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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

    private boolean isUserAllowedToShop(HttpServletRequest request) throws SQLException {
        Long userId = AuthUtil.resolveUserId(request);
        if (userId == null) {
            return false; // Không đăng nhập
        }

        String email = AuthUtil.getUserEmail(request);
        if (email == null) {
            return false;
        }

        // Lấy username từ email
        String username = getUsernameFromEmail(email);
        if (username == null) {
            return false;
        }

        String status = DBUtil.getUserStatus(username);
        if (status == null) {
            return true; // Mặc định cho phép nếu không có status
        }

        String normalizedStatus = status.trim().toLowerCase();
        return !"banned".equals(normalizedStatus) && !"inactive".equals(normalizedStatus);
    }

    private String getUsernameFromEmail(String email) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return null;
    }
}
