package web.Seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.JwtUtil;
import utils.DBUtil;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/seller-dashboard")
public class SellerDashboardServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("DEBUG SellerDashboardServlet - doGet called");
        
        // Lấy token từ header Authorization
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        System.out.println("DEBUG SellerDashboard - Token from header: " + (token != null));
        
        // Validate token và lấy username
        String username = null;
        if (token != null && !token.isEmpty()) {
            try {
                username = JwtUtil.validateToken(token);
                System.out.println("DEBUG SellerDashboard - Username from token: " + username);
            } catch (Exception e) {
                System.out.println("DEBUG SellerDashboard - Invalid token: " + e.getMessage());
            }
        }
        
        // Nếu không có username hợp lệ, redirect về login
        if (username == null || username.isEmpty()) {
            System.out.println("DEBUG SellerDashboard - No valid username, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        // Kiểm tra role của user
        try {
            String role = DBUtil.getUserRole(username);
            System.out.println("DEBUG SellerDashboard - User role: " + role);
            
            // Nếu không phải seller, redirect về trang phù hợp
            if (!"seller".equalsIgnoreCase(role)) {
                System.out.println("DEBUG SellerDashboard - User is not seller, redirecting");
                if ("admin".equalsIgnoreCase(role)) {
                    response.sendRedirect(request.getContextPath() + "/admin-dashboard");
                } else {
                    response.sendRedirect(request.getContextPath() + "/home-page.jsp");
                }
                return;
            }
            
            // Set attributes cho JSP
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            
            System.out.println("DEBUG SellerDashboard - Forwarding to seller dashboard JSP");
            
            // Forward đến trang seller dashboard
            request.getRequestDispatcher("/Seller/sellerDashboard.jsp")
                   .forward(request, response);
            
        } catch (SQLException e) {
            System.err.println("DEBUG SellerDashboard - Database error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }
}