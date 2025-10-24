package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serves the seller product management page.
 */
@WebServlet(urlPatterns = {"/seller/products", "/seller/products/add"})
public class SellerProductsPageServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerProductsPageServlet.class.getName());
    private static final String PRODUCTS_JSP = "/Seller/SellerProduct.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            SellerPageHelper.SellerContext context =
                    SellerPageHelper.resolveSellerContext(req, resp);
            if (context == null) {
                return;
            }

            req.getRequestDispatcher(PRODUCTS_JSP).forward(req, resp);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load products page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load products page");
        }
    }
}
