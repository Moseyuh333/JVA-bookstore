package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Serves the seller order management page.
 */
@WebServlet("/seller/orders")
public class SellerOrdersPageServlet extends HttpServlet {

    private static final String ORDERS_JSP = "/Seller/SellerOrders.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            SellerPageHelper.SellerContext context =
                    SellerPageHelper.resolveSellerContext(req, resp);
            if (context == null) {
                return;
            }

            req.getRequestDispatcher(ORDERS_JSP).forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load orders page");
        }
    }
}
