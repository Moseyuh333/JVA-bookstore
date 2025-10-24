package web.seller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import utils.DBUtil;

@WebServlet("/seller/profile")
public class SellerProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        
    Object userIdObj = req.getSession().getAttribute("id"); // Giả định bạn lưu là "user_id"
    
    if (userIdObj == null) {
        // Xử lý lỗi: Người dùng chưa đăng nhập hoặc Session đã hết hạn
        resp.sendRedirect(req.getContextPath() + "/login.jsp?error=session_expired");
        return;
    }
    
    int userId = (Integer) userIdObj; // Ép kiểu an toàn từ Integer object
    
    
        //int userId = Integer.parseInt(req.getParameter("userId"));

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM shops WHERE owner_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                req.setAttribute("shopId", rs.getInt("id"));
                req.setAttribute("shopName", rs.getString("name"));
                req.setAttribute("logoUrl", rs.getString("logo_url"));
                req.setAttribute("description", rs.getString("description"));
                req.setAttribute("status", rs.getString("status"));
                req.setAttribute("createdAt", rs.getString("created_at"));
                req.setAttribute("commission", rs.getDouble("commission_rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        RequestDispatcher rd = req.getRequestDispatcher("/Seller/SellerProfile.jsp");
        rd.forward(req, resp);
    }
}
