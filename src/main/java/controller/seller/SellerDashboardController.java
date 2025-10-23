// package controller.seller;

// import javax.servlet.ServletException;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.*;
// import java.io.IOException;

// @WebServlet({"/seller/dashboard", "/seller/products", "/seller/orders", "/seller/analytics", "/seller/profile", "/seller/settings"})
// public class SellerDashboardServlet extends HttpServlet {

//     @Override
//     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
//         String path = req.getServletPath();

        
//         Object role = req.getSession().getAttribute("role");
//         if (role == null || !role.equals("seller")) {
//             resp.sendRedirect(req.getContextPath() + "/login.jsp");
//             return;
//         }

        
//         String page = "/WEB-INF/views/seller/dashboard.jsp"; 
//         switch (path) {
//             case "/seller/products":
//                 page = "/WEB-INF/views/seller/products.jsp";
//                 break;
//             case "/seller/orders":
//                 page = "/WEB-INF/views/seller/orders.jsp";
//                 break;
//             case "/seller/analytics":
//                 page = "/WEB-INF/views/seller/analytics.jsp";
//                 break;
//             case "/seller/profile":
//                 page = "/WEB-INF/views/seller/profile.jsp";
//                 break;
//             case "/seller/settings":
//                 page = "/WEB-INF/views/seller/settings.jsp";
//                 break;
//         }

//         req.getRequestDispatcher(page).forward(req, resp);
//     }
// }



package controller.seller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class AdDashboardController
 */
@WebServlet("/seller-dashboard")
public class SellerDashboardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		req.setCharacterEncoding("UTF-8");

	req.getRequestDispatcher("/Seller/sellerDashboard.jsp").forward(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

}

