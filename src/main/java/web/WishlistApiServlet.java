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
import utils.JwtUtil;

@WebServlet(name = "WishlistApiServlet", urlPatterns = {"/api/wishlist/*"})
public class WishlistApiServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    private String getUserIdFromRequest(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
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
            
            Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String pathInfo = req.getPathInfo();
            
            if (pathInfo.equals("/add")) {
                int bookId = json.get("bookId").getAsInt();
                // TODO: Implement wishlist add
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\":\"Added to wishlist\"}");
            } else if (pathInfo.equals("/remove")) {
                int bookId = json.get("bookId").getAsInt();
                // TODO: Implement wishlist remove
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\":\"Removed from wishlist\"}");
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
