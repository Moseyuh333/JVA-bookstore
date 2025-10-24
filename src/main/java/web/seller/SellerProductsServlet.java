

// package web.seller;

// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import javax.servlet.annotation.WebServlet;
// import java.io.IOException;
// import java.sql.SQLException;

// import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

// // Ánh xạ đến URL /seller/products (Controller View)
// @WebServlet("/seller/products")
// public class SellerProductsServlet extends BaseSellerController {

//     private static final String PRODUCT_JSP_PATH = "/Seller/sellerProduct.jsp";
    
//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//                          throws ServletException, IOException {
        
//         try {
//             // 1. Kiểm tra xác thực và quyền Seller (Hàm này nằm trong BaseSellerController)
//             if (!setupSellerContext(request, response)) {
//                 return; // Đã xử lý lỗi (redirect/error)
//             }
            
//             // 2. Lấy shopId đã được thiết lập trong setupSellerContext
//             // Dùng Integer.valueOf để đảm bảo an toàn nếu attribute là null (dù đã kiểm tra trong BaseController)
//             Integer shopId = (Integer) request.getAttribute("shopId"); 
            
//             if (shopId == null || shopId <= 0) {
//                  response.sendError(HttpServletResponse.SC_FORBIDDEN, "Shop ID not linked to user.");
//                  return;
//             }
            
//             // 3. Tiếp tục logic nghiệp vụ và FORWARD đến JSP
//             request.getRequestDispatcher(PRODUCT_JSP_PATH).forward(request, response);
            
//         } catch (SQLException e) {
//             e.printStackTrace();
//             response.sendError(SC_INTERNAL_SERVER_ERROR, "Database error during product loading.");
//         }
//     }
    
//     // Lưu ý: Các hàm API JSON (như listProducts, createProduct) nên được đặt trong
//     // một Servlet khác ánh xạ tới /api/seller/products. 
//     // Nếu bạn muốn đặt chung, bạn cần thêm logic phân biệt URL tại đây.
// }