package web.seller;

import dao.ShopDAO;
import models.Shop;

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
 * Serves the seller profile management page.
 */
@WebServlet("/seller/profile")
public class SellerProfilePageServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SellerProfilePageServlet.class.getName());
    private static final String PROFILE_JSP = "/Seller/SellerProfile.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            SellerPageHelper.SellerContext context =
                    SellerPageHelper.resolveSellerContext(req, resp);
            if (context == null) {
                // If helper marked that the seller has no shop, redirect to register page.
                Object missing = req.getAttribute("seller_missing_shop");
                if (missing instanceof Boolean && (Boolean) missing) {
                    resp.sendRedirect(req.getContextPath() + "/Seller/register-shop.jsp");
                    return;
                }
                // Otherwise, resolveSellerContext already handled redirect/sendError for auth/role.
                return;
            }

            // Load shop data and pass to JSP
            Shop shop = ShopDAO.getShopByUserId(context.userId());
            if (shop != null) {
                req.setAttribute("shop", shop);
            }

            req.getRequestDispatcher(PROFILE_JSP).forward(req, resp);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load profile page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load profile page");
        }
    }
}
