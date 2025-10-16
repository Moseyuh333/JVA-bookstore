package web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.CartDAO;
import utils.JwtUtil;

@WebServlet(name = "CartApiServlet", urlPatterns = {"/api/cart/*"})
public class CartApiServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    private String getUserIdFromRequest(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token); // Returns username
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String userId = getUserIdFromRequest(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Unauthorized\"}");
                out.flush();
                return;
            }
            
            String pathInfo = req.getPathInfo();
            
            // Get user ID from username - for demo we'll use username as cartId
            // In production, you'd lookup the user ID from DB
            int userIdAsInt = userId.hashCode() & 0x7FFFFFFF; // Convert to positive int
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get cart items
                int cartId = CartDAO.getOrCreateCart(userIdAsInt);
                out.write(gson.toJson(CartDAO.getCartItems(cartId)));
            } else if (pathInfo.equals("/count")) {
                // Get cart item count
                int cartId = CartDAO.getOrCreateCart(userIdAsInt);
                int count = CartDAO.getCartItemCount(cartId);
                out.write("{\"count\":" + count + "}");
            }
            
            resp.setStatus(HttpServletResponse.SC_OK);
            out.flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String userId = getUserIdFromRequest(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Unauthorized\"}");
                out.flush();
                return;
            }
            
            // Read JSON body
            Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String pathInfo = req.getPathInfo();
            int userIdAsInt = userId.hashCode() & 0x7FFFFFFF;
            int cartId = CartDAO.getOrCreateCart(userIdAsInt);
            
            if (pathInfo.equals("/add")) {
                int bookId = json.get("bookId").getAsInt();
                int quantity = json.has("quantity") ? json.get("quantity").getAsInt() : 1;
                
                CartDAO.addToCart(cartId, bookId, quantity);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\":\"Added to cart\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid endpoint\"}");
            }
            
            out.flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String userId = getUserIdFromRequest(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Unauthorized\"}");
                out.flush();
                return;
            }
            
            // Read JSON body
            Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String pathInfo = req.getPathInfo();
            
            if (pathInfo.equals("/update")) {
                int itemId = json.get("itemId").getAsInt();
                int quantity = json.get("quantity").getAsInt();
                
                CartDAO.updateCartItem(itemId, quantity);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\":\"Updated cart item\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid endpoint\"}");
            }
            
            out.flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String userId = getUserIdFromRequest(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Unauthorized\"}");
                out.flush();
                return;
            }
            
            // Read JSON body
            Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String pathInfo = req.getPathInfo();
            
            if (pathInfo.equals("/remove")) {
                int itemId = json.get("itemId").getAsInt();
                
                CartDAO.removeCartItem(itemId);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\":\"Removed from cart\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid endpoint\"}");
            }
            
            out.flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
}
