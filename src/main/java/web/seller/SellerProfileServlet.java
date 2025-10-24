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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

@WebServlet("/api/seller/profile")
public class SellerProfileServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerProfileServlet.class.getName());
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
                "phone", shop.getPhone() != null ? shop.getPhone() : "",
                "email", shop.getEmail() != null ? shop.getEmail() : "",
                "slogan", shop.getSlogan() != null ? shop.getSlogan() : "",
                "commissionRate", shop.getCommissionRate()
            ));

            out.write(gson.toJson(response));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in GET /api/seller/profile", e);
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
            String phone = req.getParameter("phone");
            String email = req.getParameter("email");
            String slogan = req.getParameter("slogan");

            ShopDAO.updateShopProfile(shopId, name, address, description, phone, email, slogan);

            out.write(gson.toJson(Map.of("success", true, "message", "Shop profile updated successfully")));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in POST /api/seller/profile", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }
}
