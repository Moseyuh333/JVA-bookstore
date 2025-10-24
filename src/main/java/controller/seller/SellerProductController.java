package controller.seller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/seller/products")
public class SellerProductController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/Seller/SellerProduct.jsp");
        dispatcher.forward(request, response);
    }
}

// package controller.seller;

// import dao.BookDAO;
// import models.Book;
// import models.UserAddress;
// import utils.DBUtil;

// import javax.servlet.*;
// import javax.servlet.http.*;
// import javax.servlet.annotation.*;
// import java.io.IOException;
// import java.util.List;

// @WebServlet("/seller/products")
// public class SellerProductController extends HttpServlet {
//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {

//         HttpSession session = request.getSession(false);
//         DBUtil user = (DBUtil) session.getAttribute("user");

//         // Chỉ cho phép seller
//         if (user == null || !"seller".equals(user.getRole())) {
//             response.sendRedirect(request.getContextPath() + "/login.jsp");
//             return;
//         }

//         int shopId = user.getId(); // Giả sử User có field shopId
//         List<Book> books = BookDAO.getBooksByShopId(shopId);

//         request.setAttribute("books", books);
//         RequestDispatcher rd = request.getRequestDispatcher("/Seller/SellerProduct.jsp");
//         rd.forward(request, response);
//     }
// }
