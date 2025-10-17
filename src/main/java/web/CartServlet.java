package web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.CartDAO;
import models.Cart;
import utils.AuthUtil;
import utils.DBUtil;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "CartServlet", urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Cart cart = loadCart(request);
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("cart", cart);
            payload.put("summary", buildSummary(cart));
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<String> segments = getPathSegments(request.getPathInfo());
        if (segments.isEmpty()) {
            addCartItem(request, response);
            return;
        }
        if (segments.size() == 1 && "items".equalsIgnoreCase(segments.get(0))) {
            addCartItem(request, response);
            return;
        }
        sendNotFound(response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<String> segments = getPathSegments(request.getPathInfo());
        if (segments.size() == 2 && "items".equalsIgnoreCase(segments.get(0))) {
            Long bookId = parseId(segments.get(1));
            if (bookId == null) {
                sendNotFound(response);
                return;
            }
            updateItemQuantity(request, response, bookId);
            return;
        }
        sendNotFound(response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<String> segments = getPathSegments(request.getPathInfo());
        if (segments.isEmpty()) {
            clearCart(request, response);
            return;
        }
        if (segments.size() == 2 && "items".equalsIgnoreCase(segments.get(0))) {
            Long bookId = parseId(segments.get(1));
            if (bookId == null) {
                sendNotFound(response);
                return;
            }
            removeItem(request, response, bookId);
            return;
        }
        sendNotFound(response);
    }

    private void addCartItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> requestData = readJson(request);
        Object bookIdRaw = requestData.get("bookId");
        Object quantityRaw = requestData.get("quantity");
        Long bookId = parseObjectId(bookIdRaw);
        int quantity = parseQuantity(quantityRaw, 1);
        if (bookId == null || quantity <= 0) {
            sendBadRequest(response, "Invalid payload");
            return;
        }
        try {
            if (!isAvailable(bookId, quantity)) {
                sendBadRequest(response, "Sách đã hết hàng hoặc không đủ số lượng");
                return;
            }
            Cart cart = loadCart(request);
            CartDAO.addOrIncrementItem(cart.getId(), bookId, quantity);
            Cart updated = CartDAO.loadCart(cart.getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("cart", updated);
            payload.put("summary", buildSummary(updated));
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private void updateItemQuantity(HttpServletRequest request, HttpServletResponse response, long bookId) throws IOException {
        Map<String, Object> requestData = readJson(request);
        Object quantityRaw = requestData.get("quantity");
        int quantity = parseQuantity(quantityRaw, -1);
        if (quantity < 0) {
            sendBadRequest(response, "Số lượng không hợp lệ");
            return;
        }
        try {
            Cart cart = loadCart(request);
            if (quantity == 0) {
                CartDAO.removeItem(cart.getId(), bookId);
            } else {
                if (!isAvailable(bookId, quantity)) {
                    sendBadRequest(response, "Sách đã hết hàng hoặc không đủ số lượng");
                    return;
                }
                CartDAO.updateQuantity(cart.getId(), bookId, quantity);
            }
            Cart updated = CartDAO.loadCart(cart.getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("cart", updated);
            payload.put("summary", buildSummary(updated));
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private void removeItem(HttpServletRequest request, HttpServletResponse response, long bookId) throws IOException {
        try {
            Cart cart = loadCart(request);
            CartDAO.removeItem(cart.getId(), bookId);
            Cart updated = CartDAO.loadCart(cart.getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("cart", updated);
            payload.put("summary", buildSummary(updated));
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private void clearCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Cart cart = loadCart(request);
            CartDAO.clearCart(cart.getId());
            Cart updated = CartDAO.loadCart(cart.getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("cart", updated);
            payload.put("summary", buildSummary(updated));
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private Cart loadCart(HttpServletRequest request) throws SQLException {
    Long userId = AuthUtil.resolveUserId(request);
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        return CartDAO.ensureActiveCart(userId, sessionId);
    }

    private Map<String, Object> buildSummary(Cart cart) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("subtotal", cart.getSubtotal());
        summary.put("itemsCount", cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum());
        return summary;
    }

    private boolean isAvailable(long bookId, int quantity) throws SQLException {
        String sql = "SELECT stock_quantity FROM books WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int stock = rs.getInt(1);
                if (rs.wasNull()) {
                    return true;
                }
                // Mot so du lieu hat giong khong co ton kho nen tra ve 0.
                // De khong chan trai nghiem mua thu, cho phep dat khi ton kho <= 0.
                return stock <= 0 || stock >= quantity;
            }
        }
    }

    private Map<String, Object> readJson(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        if (json.length() == 0) {
            return new HashMap<>();
        }
        return gson.fromJson(json.toString(), new TypeToken<Map<String, Object>>(){}.getType());
    }

    private List<String> getPathSegments(String pathInfo) {
        List<String> segments = new java.util.ArrayList<>();
        if (pathInfo == null || pathInfo.isEmpty()) {
            return segments;
        }
        String[] tokens = pathInfo.split("/");
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                segments.add(token);
            }
        }
        return segments;
    }

    private Long parseId(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            long value = Long.parseLong(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parseObjectId(Object raw) {
        if (raw instanceof Number) {
            long value = ((Number) raw).longValue();
            return value > 0 ? value : null;
        }
        if (raw instanceof String) {
            return parseId((String) raw);
        }
        return null;
    }

    private int parseQuantity(Object raw, int defaultValue) {
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        if (raw instanceof String) {
            try {
                return Integer.parseInt(((String) raw).trim());
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Map<String, Object> buildError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }

    private void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(buildError(message)));
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write(gson.toJson(buildError("Endpoint not found")));
    }

    private void handleServerError(HttpServletResponse response, Exception ex) throws IOException {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(buildError("Server error: " + ex.getMessage())));
    }
}
