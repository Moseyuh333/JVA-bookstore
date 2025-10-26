package web.seller;

import dao.ShopDAO;
import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Utility helper to resolve the authenticated seller context and share it across seller pages.
 */
final class SellerPageHelper {

    private SellerPageHelper() {
    }

    static SellerContext resolveSellerContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        if (username == null || username.isEmpty()) {
            String token = (String) session.getAttribute("token");
            if (token != null && !token.isBlank()) {
                try {
                    username = JwtUtil.validateToken(token);
                    if (username != null && !username.isBlank()) {
                        session.setAttribute("username", username);
                    }
                } catch (Exception ignored) {
                    username = null;
                }
            }
        }

        if (username == null || username.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return null;
        }

        String role = DBUtil.getUserRole(username);
        if (role == null || !"seller".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Not a Seller");
            return null;
        }

        int userId = DBUtil.getUserIdByUsername(username);
        int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                // Không có shop, đánh dấu vào request để servlet biết lý do (không gửi lỗi trực tiếp)
                request.setAttribute("seller_missing_shop", Boolean.TRUE);
                return null;
            }

    SellerContext context = new SellerContext(username, role, userId, shopId);

        request.setAttribute("username", context.username());
        request.setAttribute("role", context.role());
        request.setAttribute("shopId", context.shopId());

        session.setAttribute("user_id", context.userId());
        session.setAttribute("role", context.role());
        session.setAttribute("shop_id", context.shopId());

        return context;
    }

    static final class SellerContext {
        private final String username;
        private final String role;
        private final int userId;
        private final int shopId;

        SellerContext(String username, String role, int userId, int shopId) {
            this.username = username;
            this.role = role;
            this.userId = userId;
            this.shopId = shopId;
        }

        String username() {
            return username;
        }

        String role() {
            return role;
        }

        int userId() {
            return userId;
        }

        int shopId() {
            return shopId;
        }
    }
}
