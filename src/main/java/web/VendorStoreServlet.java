package web;

import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import com.google.gson.Gson;

@WebServlet(name = "VendorStoreServlet", urlPatterns = {"/vendor/stores"})
public class VendorStoreServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String username = (String) req.getAttribute("authenticatedUsername");
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Missing authentication\"}");
            return;
        }
        try {
            Long userId = DBUtil.getUserIdByUsername(username);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"User not found\"}");
                return;
            }
            List<java.util.Map<String, Object>> stores = DBUtil.listStoresByOwner(userId);
            out.write(gson.toJson(stores));
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String username = (String) req.getAttribute("authenticatedUsername");
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Missing authentication\"}");
            return;
        }

        String name = req.getParameter("name");
        String slug = req.getParameter("slug");
        String description = req.getParameter("description");
        String avatar = req.getParameter("avatar_url");
        String cover = req.getParameter("cover_url");

        if (name == null || name.isEmpty() || slug == null || slug.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"name and slug required\"}");
            return;
        }

        try {
            Long ownerId = DBUtil.getUserIdByUsername(username);
            if (ownerId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"User not found\"}");
                return;
            }
            long storeId = DBUtil.createStore(ownerId, name, slug, description, avatar, cover);
            out.write("{\"id\":" + storeId + ", \"message\":\"Store created\"}");
        } catch (SQLException ex) {
            resp.setStatus(500);
            out.write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }
}
