package controller.seller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import utils.JwtUtil; // Dùng để xác thực token
import utils.DBUtil;  // Dùng để lấy role và userId từ DB
import dao.ShopDAO; // Dùng để lấy shopId từ userId
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
//import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * Lớp cơ sở (Base Controller) cho tất cả các Servlet liên quan đến Seller Dashboard.
 * Đảm bảo xác thực JWT, kiểm tra quyền 'seller' và thiết lập shopId trước khi truy cập View.
 */
public abstract class BaseSellerController extends HttpServlet {

    // Hàm tiện ích: Lấy Token từ Request Header (Bearer token)
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Thực hiện xác thực người dùng, kiểm tra quyền và thiết lập các biến context cần thiết (username, shopId).
     * * @return true nếu xác thực thành công và là Seller hợp lệ; false nếu thất bại (đã gửi redirect/error).
     */
    protected boolean setupSellerContext(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException, ServletException {
        
        String token = getTokenFromRequest(request);
        String username = null;
        
        // 1. Ưu tiên lấy username từ Session (nếu người dùng truy cập lần 2 sau khi login)
        username = (String) request.getSession().getAttribute("username"); 
        
        // 2. Nếu Session trống (hoặc token là API call), xác thực bằng Token
        if (username == null && token != null) {
            username = JwtUtil.validateToken(token);
        }
        
        // --- 3. Xử lý Thất bại Xác thực ---
        if (username == null || username.isEmpty()) {
            // Xóa mọi dấu vết của token cũ/hết hạn và chuyển hướng về Login
            response.sendRedirect(request.getContextPath() + "/logout-clear.jsp"); // Dùng trang clear token để tránh vòng lặp
            return false;
        }

        // --- 4. Tra cứu và Kiểm tra Quyền ---
        String role = DBUtil.getUserRole(username);
        int userId = DBUtil.getUserIdByUsername(username);
        int shopId = ShopDAO.getShopIdByUserId(userId);
        
        if (!"seller".equalsIgnoreCase(role) || shopId <= 0) {
            // Chặn người dùng không có quyền hoặc chưa có Shop
            response.sendError(SC_FORBIDDEN, "Access Denied: Tài khoản không có quyền Seller hoặc chưa liên kết Shop.");
            return false;
        }

        // --- 5. Thiết lập Context (Attributes cho JSP/Session cho API) ---
        
        // Dùng cho JSP:
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        request.setAttribute("shopId", shopId);
        request.setAttribute("userId", userId);
        
        // Dùng cho Session (duy trì trạng thái):
        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("user_id", userId);
        request.getSession().setAttribute("shop_id", shopId); // Quan trọng cho các API call sau
        
        return true;
    }
}