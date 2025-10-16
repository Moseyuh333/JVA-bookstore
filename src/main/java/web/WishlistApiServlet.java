package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.WishlistDAO;
import models.Book;
import models.User;
import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "WishlistApiServlet", urlPatterns = {"/api/wishlist", "/api/wishlist/*"})
public class WishlistApiServlet extends HttpServlet {
    private final Gson gson = new Gson();

    private User authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = JwtUtil.getTokenFromRequest(req);
        if (token == null || !JwtUtil.validateToken(token)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Authentication required\"}");
            return null;
        }
        User user = JwtUtil.getSubject(token);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Invalid token\"}");
            return null;
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = authenticate(req, resp);
        if (user == null) {
            return;
        }

        List<Book> wishlist = WishlistDAO.getWishlist(user.getId());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(wishlist));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = authenticate(req, resp);
        if (user == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        JsonObject body = readJson(req, resp);
        if (body == null) {
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            switch (pathInfo) {
                case "/add":
                    handleAdd(user.getId(), body, resp, out);
                    break;
                case "/remove":
                    handleRemove(user.getId(), body, resp, out);
                    break;
                case "/clear":
                    handleClear(user.getId(), resp, out);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Unsupported wishlist action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleAdd(int userId, JsonObject body, HttpServletResponse resp, PrintWriter out) {
        if (body == null || !body.has("bookId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Missing bookId\"}");
            return;
        }
        int bookId = body.get("bookId").getAsInt();
        boolean success = WishlistDAO.addToWishlist(userId, bookId);
        if (success) {
            out.write("{\"message\":\"Added to wishlist\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Unable to add to wishlist\"}");
        }
    }

    private void handleRemove(int userId, JsonObject body, HttpServletResponse resp, PrintWriter out) {
        if (body == null || !body.has("bookId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Missing bookId\"}");
            return;
        }
        int bookId = body.get("bookId").getAsInt();
        boolean success = WishlistDAO.removeFromWishlist(userId, bookId);
        if (success) {
            out.write("{\"message\":\"Removed from wishlist\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Unable to remove from wishlist\"}");
        }
    }

    private void handleClear(int userId, HttpServletResponse resp, PrintWriter out) {
        boolean success = WishlistDAO.clearWishlist(userId);
        if (success) {
            out.write("{\"message\":\"Wishlist cleared\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Unable to clear wishlist\"}");
        }
    }

    private JsonObject readJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        }

        if (payload.length() == 0) {
            return new JsonObject();
        }

        return gson.fromJson(payload.toString(), JsonObject.class);
    }
}
