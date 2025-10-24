package web.seller;

import dao.ShopDAO;
import models.Shop;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

@WebServlet("/api/seller/profile")
public class SellerProfileServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        try {
            Integer userId = (Integer) req.getSession().getAttribute("user_id");
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(Map.of("success", false, "message", "Unauthorized")));
                return;
            }

            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(Map.of("success", false, "message", "Shop not found")));
                return;
            }

            Shop shop = ShopDAO.getShopById(shopId);
            if (shop == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(Map.of("success", false, "message", "Shop details not found")));
                return;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shop", Map.of(
                "id", shop.getId(),
                "name", shop.getName() != null ? shop.getName() : "",
                "address", shop.getAddress() != null ? shop.getAddress() : "",
                "description", shop.getDescription() != null ? shop.getDescription() : "",
                "commissionRate", shop.getCommissionRate()
            ));

            out.write(gson.toJson(response));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        try {
            Integer userId = (Integer) req.getSession().getAttribute("user_id");
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(Map.of("success", false, "message", "Unauthorized")));
                return;
            }

            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(Map.of("success", false, "message", "Shop not found")));
                return;
            }

            String name = req.getParameter("name");
            String address = req.getParameter("address");
            String description = req.getParameter("description");

            ShopDAO.updateShopProfile(shopId, name, address, description);

            out.write(gson.toJson(Map.of("success", true, "message", "Shop profile updated successfully")));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }
}
