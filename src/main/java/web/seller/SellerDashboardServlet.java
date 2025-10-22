package web.seller;

// Import các thư viện cần thiết cho Servlet và xử lý request/response
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Giả sử bạn có class User (model) và nó có phương thức getRole()
// import models.User; 

/**
 * Servlet xử lý yêu cầu hiển thị trang Seller Dashboard.
 * Ánh xạ URL: /seller-dashboard
 */
@WebServlet("/seller-dashboard") 
public class SellerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // ***************************************************************
        // BƯỚC 1: KIỂM TRA XÁC THỰC VÀ VAI TRÒ (AUTHORIZATION CHECK)
        // ***************************************************************
        
        // Lấy thông tin user từ session (AuthServlet đã lưu)
        Object userObj = req.getSession().getAttribute("currentUser");
        
        // Nếu userObj là null (chưa đăng nhập), chuyển hướng về trang đăng nhập
        if (userObj == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        /* // Nếu bạn muốn kiểm tra vai trò cụ thể:
        if (userObj instanceof User) {
            User user = (User) userObj;
            if (!"seller".equalsIgnoreCase(user.getRole())) {
                // Nếu đã đăng nhập nhưng không phải vai trò seller, chuyển về trang chủ
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
        }
        */

        // ***************************************************************
        // BƯỚC 2: CHUYỂN TIẾP (FORWARD) TỚI GIAO DIỆN JSP
        // ***************************************************************

        // Forward request tới file JSP. 
        // Đường dẫn phải khớp với vị trí file JSP của bạn: /Seller/sellerDashboard.jsp
        // (Sử dụng tên file sellerDashboard.jsp khớp với file bạn cung cấp)
        req.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(req, resp);
    }
    
    // ***************************************************************
    // CÁC HÀM GET DATA BỊ XÓA/COMMENT ĐỂ TRÁNH LỖI KHI CHƯA DÙNG
    // Nếu muốn dùng lại, bạn cần chuyển các hàm này thành API riêng và gọi bằng AJAX
    // ***************************************************************
    /*
    private JsonObject getDashboardStats() throws SQLException { ... }
    private JsonObject getRevenueData() throws SQLException { ... }
    private JsonObject getOrderStatusData() throws SQLException { ... }
    private JsonObject getTopSellers() throws SQLException { ... }
    private JsonArray toJsonArray(List<?> items) { ... }
    */
}