package web.seller;

import utils.JwtUtil;
import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet({"/seller/dashboard", "/seller/products"})
public class SellerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            token = req.getParameter("token");
        }

        String username = null;
        if (token != null && !token.isEmpty()) {
            try {
                username = JwtUtil.validateToken(token);
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
                return;
            }
        }

        if (username == null || username.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            String role = DBUtil.getUserRole(username);

            // 🔹 5. Nếu không phải seller thì chặn
            if (!"seller".equalsIgnoreCase(role)) {
                if ("admin".equalsIgnoreCase(role)) {
                    resp.sendRedirect(req.getContextPath() + "/admin-dashboard");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/home-page.jsp");
                }
                return;
            }

            String path = req.getServletPath();
            String page;

            if ("/seller/products".equals(path)) {
                page = "/Seller/sellerProducts.jsp";
            } else {
                page = "/Seller/sellerDashboard.jsp";
            }

            req.setAttribute("username", username);
            req.setAttribute("role", role);

            req.getRequestDispatcher(page).forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/error.jsp");
        }
    }
}
