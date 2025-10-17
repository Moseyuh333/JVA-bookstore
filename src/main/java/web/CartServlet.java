package web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dao.CartDAO;
import models.Cart;
import models.CartItem;
import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "CartServlet", urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try {
            Cart cart = ensureCart(req, resp);
            writeCartResponse(resp, cart);
        } catch (SQLException ex) {
            sendServerError(resp, "Không thể tải giỏ hàng", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = normalizePath(req.getPathInfo());
        try {
            if (path == null || path.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                writeError(resp, "Endpoint không hỗ trợ POST trực tiếp");
                return;
            }

            switch (path) {
                case "/items":
                    handleAddItem(req, resp);
                    break;
                case "/merge":
                    handleMerge(req, resp);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeError(resp, "Endpoint không tồn tại");
            }
        } catch (JsonParseException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Dữ liệu JSON không hợp lệ");
        } catch (SQLException ex) {
            sendServerError(resp, "Không thể cập nhật giỏ hàng", ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = normalizePath(req.getPathInfo());
        if (path == null || !path.startsWith("/items/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeError(resp, "Endpoint không tồn tại");
            return;
        }

        try {
            long bookId = parseIdSegment(path, "/items/");
            JsonObject body = readJsonBody(req);
            int quantity = body.has("quantity") ? body.get("quantity").getAsInt() : 1;
            if (quantity < 0) {
                quantity = 0;
            }

            Cart cart = ensureCart(req, resp);
            Cart updated = CartDAO.setItemQuantity(cart.getId(), bookId, quantity);
            writeCartResponse(resp, updated);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Book ID không hợp lệ");
        } catch (JsonParseException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Dữ liệu JSON không hợp lệ");
        } catch (SQLException ex) {
            sendServerError(resp, "Không thể cập nhật giỏ hàng", ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = normalizePath(req.getPathInfo());
        try {
            Cart cart = ensureCart(req, resp);
            if (path == null || path.isEmpty() || "/items".equals(path)) {
                Cart cleared = CartDAO.clearCart(cart.getId());
                writeCartResponse(resp, cleared);
                return;
            }

            if (path.startsWith("/items/")) {
                long bookId = parseIdSegment(path, "/items/");
                Cart updated = CartDAO.removeItem(cart.getId(), bookId);
                writeCartResponse(resp, updated);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(resp, "Endpoint không tồn tại");
            }
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Book ID không hợp lệ");
        } catch (SQLException ex) {
            sendServerError(resp, "Không thể cập nhật giỏ hàng", ex);
        }
    }

    private void handleAddItem(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        JsonObject body = readJsonBody(req);
        if (!body.has("bookId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Thiếu bookId");
            return;
        }
        long bookId = body.get("bookId").getAsLong();
        int quantity = body.has("quantity") ? body.get("quantity").getAsInt() : 1;
        if (quantity <= 0) {
            quantity = 1;
        }

        Cart cart = ensureCart(req, resp);
        Cart updated = CartDAO.addOrIncrementItem(cart.getId(), bookId, quantity);
        writeCartResponse(resp, updated);
    }

    private void handleMerge(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Long userId = resolveUserId(req);
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeError(resp, "Yêu cầu đăng nhập để đồng bộ giỏ hàng");
            return;
        }
        String sessionId = resolveSessionId(req, resp);

        Cart merged = CartDAO.mergeGuestCartIntoUserCart(sessionId, userId);
        Cart responseCart = merged != null ? merged : CartDAO.getOrCreateCart(userId, sessionId);
        writeCartResponse(resp, responseCart);
    }

    private Cart ensureCart(HttpServletRequest req, HttpServletResponse resp) throws SQLException {
        Long userId = resolveUserId(req);
        String sessionId = resolveSessionId(req, resp);
        Cart cart = CartDAO.getOrCreateCart(userId, sessionId);
        if (userId != null && cart.getUserId() == null) {
            CartDAO.attachCartToUser(cart.getId(), userId);
            cart = CartDAO.getCartById(cart.getId());
        }
        return cart;
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

    private String resolveSessionId(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(true);
        String sessionId = session.getId();
        Cookie cartCookie = new Cookie("cart_session", sessionId);
        cartCookie.setMaxAge(60 * 60 * 24 * 30);
        cartCookie.setHttpOnly(false);
        cartCookie.setSecure(req.isSecure());
        cartCookie.setPath(req.getContextPath() == null || req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        resp.addCookie(cartCookie);
        return sessionId;
    }

    private JsonObject cartToJson(Cart cart) {
        JsonObject root = new JsonObject();
        if (cart == null) {
            root.addProperty("id", -1);
            root.addProperty("subtotal", 0);
            root.addProperty("total", 0);
            root.addProperty("currency", "VND");
            root.addProperty("totalQuantity", 0);
            root.add("items", new JsonArray());
            root.addProperty("status", "empty");
            return root;
        }

        root.addProperty("id", cart.getId());
        if (cart.getUserId() != null) {
            root.addProperty("userId", cart.getUserId());
        }
        if (cart.getSessionId() != null) {
            root.addProperty("sessionId", cart.getSessionId());
        }
        root.addProperty("status", cart.getStatus());
        root.addProperty("currency", cart.getCurrency());
        root.addProperty("subtotal", cart.getSubtotal().doubleValue());
        root.addProperty("total", cart.getTotal().doubleValue());
        root.addProperty("totalQuantity", cart.getTotalQuantity());

        JsonArray items = new JsonArray();
        for (CartItem item : cart.getItems()) {
            JsonObject row = new JsonObject();
            row.addProperty("id", item.getId());
            row.addProperty("bookId", item.getBookId());
            row.addProperty("bookTitle", item.getBookTitle());
            if (item.getBookAuthor() != null) {
                row.addProperty("bookAuthor", item.getBookAuthor());
            }
            if (item.getBookImageUrl() != null) {
                row.addProperty("bookImageUrl", item.getBookImageUrl());
            }
            row.addProperty("unitPrice", item.getUnitPrice().doubleValue());
            row.addProperty("quantity", item.getQuantity());
            row.addProperty("lineTotal", item.getLineTotal().doubleValue());
            items.add(row);
        }
        root.add("items", items);
        root.addProperty("empty", cart.isEmpty());
        return root;
    }

    private void writeCartResponse(HttpServletResponse resp, Cart cart) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("success", true);
        payload.add("cart", cartToJson(cart));
        resp.getWriter().write(gson.toJson(payload));
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

    private String normalizePath(String pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        if (pathInfo.endsWith("/")) {
            return pathInfo.substring(0, pathInfo.length() - 1);
        }
        return pathInfo;
    }

    private long parseIdSegment(String path, String prefix) {
        String value = path.substring(prefix.length());
        if (value.contains("/")) {
            value = value.substring(0, value.indexOf('/'));
        }
        return Long.parseLong(value);
    }

    private JsonObject readJsonBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String json = sb.toString();
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }
        return gson.fromJson(json, JsonObject.class);
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
}
