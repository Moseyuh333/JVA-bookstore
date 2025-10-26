package web;

import com.google.gson.Gson;
import dao.CouponDAO;
import dao.ShopCouponDAO;
import models.ShopCoupon;
import utils.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

@WebServlet(name = "CouponValidationServlet", urlPatterns = {"/api/coupons/validate"})
public class CouponValidationServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Long userId = AuthUtil.resolveUserId(request);
            if (userId == null) {
                sendUnauthorized(response);
                return;
            }

            String code = request.getParameter("code");
            if (code == null || code.trim().isEmpty()) {
                sendBadRequest(response, "Vui lòng nhập mã giảm giá");
                return;
            }

            String normalizedCode = code.trim().toUpperCase(Locale.ROOT);

            Long shopId = null;
            String shopIdParam = request.getParameter("shopId");
            if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
                try {
                    shopId = Long.parseLong(shopIdParam.trim());
                } catch (NumberFormatException ignored) {
                    shopId = null;
                }
            }

            if (shopId != null && shopId > 0) {
                ShopCoupon shopCoupon = ShopCouponDAO.findActiveForShop(shopId.intValue(), normalizedCode);
                if (isShopCouponValid(shopCoupon)) {
                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("success", true);
                    responseBody.put("message", "Mã giảm giá hợp lệ");

                    Map<String, Object> couponData = new HashMap<>();
                    couponData.put("code", normalizedCode);
                    couponData.put("description", shopCoupon.getDescription());
                    couponData.put("type", shopCoupon.getDiscountType());
                    couponData.put("value", shopCoupon.getDiscountValue());
                    couponData.put("minimumOrder", shopCoupon.getMinimumOrder());
                    couponData.put("startDate", shopCoupon.getStartDate());
                    couponData.put("endDate", shopCoupon.getEndDate());
                    couponData.put("usageLimit", shopCoupon.getUsageLimit());
                    if (shopCoupon.getUsageLimit() != null) {
                        int used = shopCoupon.getUsedCount() != null ? shopCoupon.getUsedCount() : 0;
                        couponData.put("remaining", Math.max(0, shopCoupon.getUsageLimit() - used));
                    }
                    couponData.put("shopId", shopCoupon.getShopId());
                    couponData.put("shopName", shopCoupon.getShopName());
                    couponData.put("scope", "shop");

                    responseBody.put("coupon", couponData);
                    response.getWriter().write(gson.toJson(responseBody));
                    return;
                }
            }

            CouponDAO.CouponValidationResult validationResult = CouponDAO.validateCouponCode(normalizedCode, userId);

            if (!validationResult.isValid) {
                sendBadRequest(response, validationResult.message != null ? validationResult.message : "Mã giảm giá không hợp lệ");
                return;
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Mã giảm giá hợp lệ");
            
            Map<String, Object> couponData = new HashMap<>();
            couponData.put("couponId", validationResult.couponId);
            couponData.put("code", validationResult.code);
            couponData.put("description", validationResult.description);
            couponData.put("type", validationResult.type);
            couponData.put("value", validationResult.value);
            couponData.put("maxDiscount", validationResult.maxDiscount);
            couponData.put("minimumOrder", validationResult.minimumOrder);
            couponData.put("startDate", validationResult.startDate);
            couponData.put("endDate", validationResult.endDate);
            couponData.put("status", validationResult.status);
            
            responseBody.put("coupon", couponData);
            
            response.getWriter().write(gson.toJson(responseBody));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Bạn cần đăng nhập để sử dụng chức năng này");
        response.getWriter().write(gson.toJson(body));
    }

    private boolean isShopCouponValid(ShopCoupon coupon) {
        if (coupon == null) {
            return false;
        }
        if (coupon.getStatus() != null && !"active".equalsIgnoreCase(coupon.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            return false;
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            return false;
        }
        if (coupon.getUsageLimit() != null) {
            int used = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
            if (used >= coupon.getUsageLimit()) {
                return false;
            }
        }
        return true;
    }

    private void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        response.getWriter().write(gson.toJson(body));
    }

    private void handleServerError(HttpServletResponse response, Exception ex) throws IOException {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Lỗi hệ thống");
        response.getWriter().write(gson.toJson(body));
    }
}
