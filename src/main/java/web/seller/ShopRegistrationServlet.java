package web.seller;

import dao.ShopDAO;
import models.Shop;
import utils.DBUtil;
import utils.AuthUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/seller/register-shop")
public class ShopRegistrationServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ShopRegistrationServlet.class.getName());
    private final Gson gson = new Gson();

    private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        try {
            System.out.println("DEBUG ShopRegistrationServlet - doPost called");

            // Try session first, then JWT token
            Integer userId = (Integer) req.getSession().getAttribute("user_id");
            System.out.println("DEBUG ShopRegistrationServlet - userId from session: " + userId);

            if (userId == null) {
                // Fallback to JWT token
                try {
                    userId = AuthUtil.resolveUserId(req).intValue();
                    System.out.println("DEBUG ShopRegistrationServlet - userId from JWT: " + userId);
                } catch (SQLException e) {
                    System.err.println("DEBUG ShopRegistrationServlet - JWT resolution failed: " + e.getMessage());
                    // Ignore, will check below
                }
            }

            if (userId == null) {
                System.out.println("DEBUG ShopRegistrationServlet - No userId found, returning 401");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(Map.of("success", false, "message", "Vui lòng đăng nhập trước")));
                return;
            }

            // Kiểm tra user đã có shop chưa
            int existingShopId = ShopDAO.getShopIdByUserId(userId);
            System.out.println("DEBUG ShopRegistrationServlet - existingShopId: " + existingShopId);
            if (existingShopId > 0) {
                // Check if the shop is pending - if so, allow re-registration
                try {
                    Shop existingShop = ShopDAO.getShopById(existingShopId);
                    if (existingShop != null && "pending".equals(existingShop.getStatus())) {
                        System.out.println("DEBUG ShopRegistrationServlet - User has pending shop, allowing re-registration");
                        // Delete the pending shop to allow new registration
                        try (Connection conn = DBUtil.getConnection();
                             PreparedStatement ps = conn.prepareStatement("DELETE FROM shops WHERE id = ?")) {
                            ps.setInt(1, existingShopId);
                            ps.executeUpdate();
                            System.out.println("DEBUG ShopRegistrationServlet - Deleted pending shop, proceeding with new registration");
                        }
                    } else {
                        System.out.println("DEBUG ShopRegistrationServlet - User has active shop, returning 400");
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write(gson.toJson(Map.of("success", false, "message", "Bạn đã có shop rồi")));
                        return;
                    }
                } catch (SQLException e) {
                    System.err.println("DEBUG ShopRegistrationServlet - Error checking shop status: " + e.getMessage());
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(Map.of("success", false, "message", "Bạn đã có shop rồi")));
                    return;
                }
            }

            String name = req.getParameter("name");
            String address = req.getParameter("address");
            String description = req.getParameter("description");
            System.out.println("DEBUG ShopRegistrationServlet - name: '" + name + "', address: '" + address + "', description: '" + description + "'");

            if (name == null || name.trim().isEmpty()) {
                System.out.println("DEBUG ShopRegistrationServlet - Name is null or empty, returning 400");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Tên shop là bắt buộc")));
                return;
            }

            // Tạo shop mới
            int shopId = ShopDAO.createShop(userId, name.trim(), address, description);

            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(Map.of("success", false, "message", "Không thể tạo shop")));
                return;
            }

            // ✅ Cập nhật role = seller và status = pending
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE users SET role = ?::user_role, status = ?::user_status WHERE id = ?")) {

                ps.setString(1, "seller");
                ps.setString(2, "pending");
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            // Cập nhật lại session
            req.getSession().setAttribute("role", "seller");
            req.getSession().setAttribute("status", "pending");
            req.getSession().setAttribute("shop_id", shopId);

            // ✅ Trả phản hồi JSON về client
            out.write(gson.toJson(Map.of(
                    "success", true,
                    "message", "Yêu cầu đăng ký shop đã được gửi. Vui lòng chờ admin duyệt.",
                    "shopId", shopId
            )));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi SQL trong /api/seller/register-shop", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Lỗi cơ sở dữ liệu: " + e.getMessage())));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi không xác định trong /api/seller/register-shop", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Lỗi hệ thống")));
        } finally {
            out.flush();
        }
    }
}


