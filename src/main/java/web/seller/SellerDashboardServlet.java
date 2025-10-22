package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Giả sử bạn có model User trong hệ thống để kiểm tra vai trò
// import models.User; 

/**
 * Servlet xử lý yêu cầu hiển thị trang Seller Dashboard.
 * Ánh xạ URL: /seller-dashboard
 * Chú ý: Vì dùng @WebServlet, phải xóa ánh xạ /seller-dashboard trong web.xml
 */
@WebServlet("/seller-dashboard") 
public class SellerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 1. KIỂM TRA XÁC THỰC VÀ VAI TRÒ (AUTHORIZATION)
        Object userObj = req.getSession().getAttribute("currentUser");
        String username = null;
        String role = "User"; // Mặc định là User nếu không tìm thấy
        if (userObj == null) {
            // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        /* // Logic mẫu để lấy Username và Role từ User Object (Cần import models.User)
        if (userObj instanceof User) {
            User user = (User) userObj;
            username = user.getUsername(); // Hoặc getEmail()
            role = user.getRole();
            
            if (!"seller".equalsIgnoreCase(role)) {
                // Nếu đã đăng nhập nhưng không phải vai trò seller, chuyển về trang chủ
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
        }
        */

        
        if (username == null) {
            // Trong môi trường thực tế, bạn cần lấy User Object từ Session/Token
            username = "Seller_Account"; 
            role = "Seller"; 
}

        // Đặt thuộc tính vào request để JSP có thể đọc được (ví dụ: ${username}, ${role})
        req.setAttribute("username", username); 
        req.setAttribute("role", role); 

        // 2. CHUYỂN TIẾP (FORWARD) TỚI GIAO DIỆN JSP
        // Đường dẫn: /Seller/sellerDashboard.jsp
        req.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(req, resp);
        
    }
    
    // Xóa các hàm thống kê cũ
}
