package web.seller;

import dao.ShopDAO;
import models.Shop;
import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
            String role = (String) req.getSession().getAttribute("role");

            if (userId == null || !"seller".equalsIgnoreCase(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(java.util.Map.of("success", false, "message", "Access denied")));
                return;
            }

            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(java.util.Map.of("success", false, "message", "Shop not found")));
                return;
            }

            String action = req.getParameter("action");

            if ("get".equals(action)) {
                getShopProfile(out, shopId);
            } else if ("summary".equals(action)) {
                // For analytics page
                getShopProfile(out, shopId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(java.util.Map.of("success", false, "message", "Invalid action")));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in doGet /api/seller/profile", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Database error: " + e.getMessage())));
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
            String role = (String) req.getSession().getAttribute("role");

            if (userId == null || !"seller".equalsIgnoreCase(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(java.util.Map.of("success", false, "message", "Access denied")));
                return;
            }

            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(java.util.Map.of("success", false, "message", "Shop not found")));
                return;
            }

            String action = req.getParameter("action");

            if ("update".equals(action)) {
                updateShopProfile(req, out, shopId);
            } else if ("update_commission".equals(action)) {
                updateCommissionRate(req, out, shopId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(java.util.Map.of("success", false, "message", "Invalid action")));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in doPost /api/seller/profile", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Database error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    private void getShopProfile(PrintWriter out, int shopId) throws SQLException {
        Shop shop = ShopDAO.getShopById(shopId);

        if (shop == null) {
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Shop not found")));
            return;
        }

        java.util.Map<String, Object> shopData = new java.util.HashMap<>();
        shopData.put("id", shop.getId());
        shopData.put("name", shop.getName());
        shopData.put("address", shop.getAddress());
        shopData.put("description", shop.getDescription());
        double sanitizedRate = normalizeStoredCommissionRate(shop.getCommissionRate());
        shopData.put("commissionRate", sanitizedRate);
        shopData.put("phone", shop.getPhone());
        shopData.put("email", shop.getEmail());
        shopData.put("logoUrl", shop.getLogoUrl());
        shopData.put("status", shop.getStatus());
        shopData.put("slogan", shop.getSlogan());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("shop", shopData);

        out.write(gson.toJson(response));
    }

    private void updateShopProfile(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        String name = req.getParameter("name");
        String address = req.getParameter("address");
        String description = req.getParameter("description");
        String phone = req.getParameter("phone");
        String email = req.getParameter("email");
        String slogan = req.getParameter("slogan");

        if (name == null || name.trim().isEmpty()) {
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Shop name is required")));
            return;
        }

        // Update shop information
        ShopDAO.updateShopProfile(shopId, name.trim(), address != null ? address.trim() : null,
                                 description != null ? description.trim() : null,
                                 phone != null ? phone.trim() : null,
                                 email != null ? email.trim() : null,
                                 slogan != null ? slogan.trim() : null);

        out.write(gson.toJson(java.util.Map.of("success", true, "message", "Shop profile updated successfully")));
    }

    private void updateCommissionRate(HttpServletRequest req, PrintWriter out, int shopId) throws SQLException {
        String rateParam = req.getParameter("commissionRate");

        if (rateParam == null || rateParam.trim().isEmpty()) {
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Commission rate is required")));
            return;
        }

        double ratePercent;
        try {
            ratePercent = Double.parseDouble(rateParam.trim());
        } catch (NumberFormatException ex) {
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Invalid commission rate")));
            return;
        }

        if (ratePercent < 0 || ratePercent > 100) {
            out.write(gson.toJson(java.util.Map.of("success", false, "message", "Commission rate must be between 0 and 100")));
            return;
        }

        double roundedRate = Math.round(ratePercent * 100.0) / 100.0;
        if (roundedRate > 100.0) {
            roundedRate = 100.0;
        }
        ShopDAO.updateCommissionRate(shopId, roundedRate);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "Commission rate updated successfully");
        response.put("commissionRate", roundedRate);
        out.write(gson.toJson(response));
    }

    private static double normalizeStoredCommissionRate(double rawRate) {
        if (Double.isNaN(rawRate)) {
            return 0.0;
        }
        if (rawRate < 0.0) {
            return 0.0;
        }
        if (rawRate <= 1.0) {
            return Math.round(rawRate * 10000.0) / 100.0;
        }
        if (rawRate > 100.0) {
            return 100.0;
        }
        return Math.round(rawRate * 100.0) / 100.0;
    }
}
