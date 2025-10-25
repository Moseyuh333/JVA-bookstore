// package web.seller;

// import dao.ShopDAO;
// import utils.DBUtil;

// import javax.servlet.ServletException;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import java.io.PrintWriter;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;
// import java.util.Map;
// import java.util.logging.Level;
// import java.util.logging.Logger;

// import com.google.gson.Gson;

// @WebServlet("/api/seller/register-shop")
// public class ShopRegistrationServlet extends HttpServlet {
//     private static final Logger LOGGER = Logger.getLogger(ShopRegistrationServlet.class.getName());
//     private final Gson gson = new Gson();

//     private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//         req.setCharacterEncoding("UTF-8");
//         resp.setCharacterEncoding("UTF-8");
//         resp.setContentType("application/json; charset=UTF-8");
//     }

//     @Override
//     protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
//             throws ServletException, IOException {
//         setEncoding(req, resp);
//         PrintWriter out = resp.getWriter();

//         try {
//             Integer userId = (Integer) req.getSession().getAttribute("user_id");
//             String role = (String) req.getSession().getAttribute("role");

//             if (userId == null) {
//                 resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                 out.write(gson.toJson(Map.of("success", false, "message", "Please login first")));
//                 return;
//             }

//             // Kiểm tra xem user đã có shop chưa
//             int existingShopId = ShopDAO.getShopIdByUserId(userId);
//             if (existingShopId > 0) {
//                 resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                 out.write(gson.toJson(Map.of("success", false, "message", "You already have a shop")));
//                 return;
//             }

//             String name = req.getParameter("name");
//             String address = req.getParameter("address");
//             String description = req.getParameter("description");

//             if (name == null || name.trim().isEmpty()) {
//                 resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                 out.write(gson.toJson(Map.of("success", false, "message", "Shop name is required")));
//                 return;
//             }

//             int shopId = ShopDAO.createShop(userId, name.trim(), address, description);

//             if (shopId > 0) {
//                 // Cập nhật role thành seller nếu chưa phải
//                 // if (!"seller".equalsIgnoreCase(role)) {
//                 //     DBUtil.updateUserRole(userId, "seller");
//                 //     req.getSession().setAttribute("role", "seller");
//                 // }

                
                
//                 // req.getSession().setAttribute("shop_id", shopId);

//                 // out.write(gson.toJson(Map.of(
//                 //     "success", true, 
//                 //     "message", "Shop created successfully",
//                 //     "shopId", shopId
//                 // )));

//                 if (shopId > 0) {
//                     try (Connection conn = DBUtil.getConnection();
//                         PreparedStatement ps = conn.prepareStatement("UPDATE users SET role = ?, status = ? WHERE id = ?")) {
//                         ps.setString(1, "seller");
//                         ps.setString(2, "pending");
//                         ps.setInt(3, userId);
//                         ps.executeUpdate();
//                     }

//                     req.getSession().setAttribute("role", "seller");
//                     req.getSession().setAttribute("status", "pending");

//                     out.write(gson.toJson(Map.of(
//                         "success", true,
//                         "message", "Yêu cầu đăng ký shop đã được gửi, vui lòng chờ admin duyệt.",
//                         "shopId", shopId
//                     )));
//                 }

//             } else {
//                 resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                 out.write(gson.toJson(Map.of("success", false, "message", "Failed to create shop")));
//             }

//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Database error in /api/seller/register-shop", e);
//             resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//             out.write(gson.toJson(Map.of("success", false, "message", "Database error: " + e.getMessage())));
//         } finally {
//             out.flush();
//         }
//     }
//             }
        



package web.seller;

import dao.ShopDAO;
import utils.DBUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
            Integer userId = (Integer) req.getSession().getAttribute("user_id");

            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(Map.of("success", false, "message", "Vui lòng đăng nhập trước")));
                return;
            }

            // Kiểm tra đã có shop chưa
            int existingShopId = ShopDAO.getShopIdByUserId(userId);
            if (existingShopId > 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Bạn đã có shop rồi.")));
                return;
            }

            String name = req.getParameter("name");
            String address = req.getParameter("address");
            String description = req.getParameter("description");

            if (name == null || name.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Tên shop là bắt buộc")));
                return;
            }

            int shopId = ShopDAO.createShop(userId, name.trim(), address, description);

            if (shopId > 0) {
                // Cập nhật user -> role = 'seller', status = 'pending'
                try (Connection conn = DBUtil.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE users SET role = ?, status = ? WHERE id = ?")) {
                    ps.setString(1, "seller");
                    ps.setString(2, "pending");
                    ps.setInt(3, userId);
                    ps.executeUpdate();
                }

                req.getSession().setAttribute("role", "seller");
                req.getSession().setAttribute("status", "pending");

                out.write(gson.toJson(Map.of(
                        "success", true,
                        "message", "Yêu cầu đăng ký shop đã được gửi. Vui lòng chờ admin duyệt.",
                        "shopId", shopId
                )));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(Map.of("success", false, "message", "Không thể tạo shop")));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in /api/seller/register-shop", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Lỗi cơ sở dữ liệu: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }
}
