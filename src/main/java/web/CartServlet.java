package web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dao.CartDAO;
import models.CartItem;
import models.User;
import utils.JwtUtil;

@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    // Get cart items for the logged-in user
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = JwtUtil.getTokenFromRequest(req);
        if (token == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Authentication required\"}");
            return;
        }

        try {
            User user = JwtUtil.getSubject(token);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\":\"Invalid token\"}");
                return;
            }

            List<CartItem> cartItems = CartDAO.getCartItems(user.getId());
            String jsonResponse = gson.toJson(cartItems);
            
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    // Add an item to the cart
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/add")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String token = JwtUtil.getTokenFromRequest(req);
        if (token == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Authentication required. Please log in.\"}");
            return;
        }

        try {
            User user = JwtUtil.getSubject(token);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\":\"Invalid token\"}");
                return;
            }

            CartItemRequest cartRequest = gson.fromJson(req.getReader(), CartItemRequest.class);
            if (cartRequest == null || cartRequest.getBookId() <= 0 || cartRequest.getQuantity() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid book ID or quantity\"}");
                return;
            }

            CartDAO.addToCart(user.getId(), cartRequest.getBookId(), cartRequest.getQuantity());
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\":\"Book added to cart successfully\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    // Update cart item quantity
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/update")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }
        
        String token = JwtUtil.getTokenFromRequest(req);
        if (token == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            User user = JwtUtil.getSubject(token);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            CartItemRequest cartRequest = gson.fromJson(req.getReader(), CartItemRequest.class);
            if (cartRequest == null || cartRequest.getCartItemId() <= 0 || cartRequest.getQuantity() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid cart item ID or quantity\"}");
                return;
            }

            CartDAO.updateCartItemQuantity(cartRequest.getCartItemId(), cartRequest.getQuantity());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\":\"Cart updated successfully\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    // Remove an item from the cart
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/remove/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String token = JwtUtil.getTokenFromRequest(req);
        if (token == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            User user = JwtUtil.getSubject(token);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 3) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cart item ID is missing");
                return;
            }
            int cartItemId = Integer.parseInt(pathParts[2]);

            CartDAO.removeCartItem(cartItemId, user.getId());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\":\"Item removed from cart\"}");

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid cart item ID format");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    // Helper class for JSON deserialization
    private static class CartItemRequest {
        private int bookId;
        private int quantity;
        private int cartItemId;

        public int getBookId() {
            return bookId;
        }
        public int getQuantity() {
            return quantity;
        }
        public int getCartItemId() {
            return cartItemId;
        }
    }
}
