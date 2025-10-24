package web.seller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves the seller shop registration page.
 */
@WebServlet("/seller/register-shop")
public class RegisterShopPageServlet extends HttpServlet {

    private static final String REGISTER_JSP = "/Seller/register-shop.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(REGISTER_JSP).forward(req, resp);
    }
}
