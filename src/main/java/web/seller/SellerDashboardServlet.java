package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet({"/seller/dashboard", "/seller/products", "/seller/orders", "/seller/analytics", "/seller/profile", "/seller/settings"})
public class SellerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy đường dẫn người dùng đang truy cập
        String path = req.getServletPath();

        // Kiểm tra đăng nhập và role
        Object role = req.getSession().getAttribute("role");
        if (role == null || !role.equals("seller")) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // Chuyển hướng tương ứng JSP
        String page = "/WEB-INF/views/seller/dashboard.jsp"; // mặc định
        switch (path) {
            case "/seller/products":
                page = "/WEB-INF/views/seller/products.jsp";
                break;
            case "/seller/orders":
                page = "/WEB-INF/views/seller/orders.jsp";
                break;
            case "/seller/analytics":
                page = "/WEB-INF/views/seller/analytics.jsp";
                break;
            case "/seller/profile":
                page = "/WEB-INF/views/seller/profile.jsp";
                break;
            case "/seller/settings":
                page = "/WEB-INF/views/seller/settings.jsp";
                break;
        }

        req.getRequestDispatcher(page).forward(req, resp);
    }
}
