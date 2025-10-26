package web.seller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.ShopCouponDAO;
import dao.ShopDAO;
import models.ShopCoupon;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "SellerCouponServlet", urlPatterns = {"/api/seller/coupons"})
public class SellerCouponServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SellerCouponServlet.class.getName());
    private final Gson gson = new Gson();

    private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setEncoding(req, resp);
        try (PrintWriter out = resp.getWriter()) {
            SellerContext context = requireSellerContext(req, resp, out);
            if (context == null) {
                return;
            }

            String action = normalizeAction(req.getParameter("action"), "list");
            try {
                if ("list".equals(action)) {
                    listCoupons(out, context.shopId());
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(Map.of("success", false, "message", "Invalid action")));
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to load coupons", ex);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(Map.of("success", false, "message", "Database error")));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setEncoding(req, resp);
        try (PrintWriter out = resp.getWriter()) {
            SellerContext context = requireSellerContext(req, resp, out);
            if (context == null) {
                return;
            }

            String action = normalizeAction(req.getParameter("action"), "");
            try {
                switch (action) {
                    case "create":
                        createCoupon(req, resp, out, context.shopId());
                        break;
                    case "delete":
                        deleteCoupon(req, resp, out, context.shopId());
                        break;
                    default:
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write(gson.toJson(Map.of("success", false, "message", "Invalid action")));
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to process coupon action", ex);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(Map.of("success", false, "message", "Database error")));
            }
        }
    }

    private void listCoupons(PrintWriter out, int shopId) throws SQLException {
        var coupons = ShopCouponDAO.listByShop(shopId);
        JsonArray items = new JsonArray();
        for (ShopCoupon coupon : coupons) {
            items.add(toJson(coupon));
        }
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.add("coupons", items);
        out.write(gson.toJson(response));
    }

    private void createCoupon(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, int shopId)
            throws SQLException, IOException {
        String code = safeString(req.getParameter("code")).toUpperCase(Locale.ROOT);
        String discountType = normalizeAction(req.getParameter("discountType"), "percentage");
        String discountValueRaw = safeString(req.getParameter("discountValue"));
        String minOrderRaw = safeString(req.getParameter("minimumOrder"));
        String usageLimitRaw = safeString(req.getParameter("usageLimit"));
        String startDateRaw = safeString(req.getParameter("startDate"));
        String endDateRaw = safeString(req.getParameter("endDate"));
        String description = safeString(req.getParameter("description"));

        if (code.isEmpty() || discountValueRaw.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Mã giảm giá và giá trị là bắt buộc")));
            return;
        }

        if (!"percentage".equals(discountType) && !"fixed".equals(discountType)) {
            discountType = "percentage";
        }

        BigDecimal discountValue;
        try {
            discountValue = new BigDecimal(discountValueRaw);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Giá trị giảm giá không hợp lệ")));
            return;
        }

        if ("percentage".equals(discountType)) {
            if (discountValue.compareTo(BigDecimal.ZERO) <= 0 || discountValue.compareTo(new BigDecimal("100")) > 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Phần trăm giảm phải trong khoảng 0 - 100")));
                return;
            }
        } else if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Giá trị giảm phải lớn hơn 0")));
            return;
        }

        BigDecimal minimumOrder = BigDecimal.ZERO;
        if (!minOrderRaw.isEmpty()) {
            try {
                minimumOrder = new BigDecimal(minOrderRaw);
            } catch (NumberFormatException ex) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Giá trị đơn hàng tối thiểu không hợp lệ")));
                return;
            }
        }

        Integer usageLimit = null;
        if (!usageLimitRaw.isEmpty()) {
            try {
                int parsed = Integer.parseInt(usageLimitRaw);
                if (parsed < 1) {
                    throw new NumberFormatException("usage limit < 1");
                }
                usageLimit = parsed;
            } catch (NumberFormatException ex) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("success", false, "message", "Giới hạn sử dụng không hợp lệ")));
                return;
            }
        }

        LocalDateTime startDate = parseDate(startDateRaw, true);
        LocalDateTime endDate = parseDate(endDateRaw, false);
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Ngày kết thúc phải sau ngày bắt đầu")));
            return;
        }

        ShopCoupon coupon = new ShopCoupon();
        coupon.setShopId(shopId);
        coupon.setCode(code);
        coupon.setDescription(description);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setMinimumOrder(minimumOrder);
        coupon.setUsageLimit(usageLimit);
        coupon.setStartDate(startDate);
        coupon.setEndDate(endDate);

        try {
            ShopCoupon created = ShopCouponDAO.createCoupon(coupon);
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("coupon", toJson(created));
            out.write(gson.toJson(response));
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase(Locale.ROOT).contains("uq_shop_coupons_code")) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write(gson.toJson(Map.of("success", false, "message", "Mã giảm giá đã tồn tại")));
            } else {
                throw ex;
            }
        }
    }

    private void deleteCoupon(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, int shopId)
            throws SQLException {
        String idParam = req.getParameter("couponId");
        if (idParam == null || idParam.isBlank()) {
            idParam = req.getParameter("id");
        }
        if (idParam == null || idParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Thiếu mã coupon")));
            return;
        }

        int couponId;
        try {
            couponId = Integer.parseInt(idParam);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(Map.of("success", false, "message", "Mã coupon không hợp lệ")));
            return;
        }

        boolean deleted = ShopCouponDAO.deleteCoupon(shopId, couponId);
        if (deleted) {
            out.write(gson.toJson(Map.of("success", true, "message", "Đã xoá mã giảm giá")));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(Map.of("success", false, "message", "Không tìm thấy mã giảm giá")));
        }
    }

    private JsonObject toJson(ShopCoupon coupon) {
        JsonObject json = new JsonObject();
        json.addProperty("id", coupon.getId());
        json.addProperty("shopId", coupon.getShopId());
        json.addProperty("code", coupon.getCode());
        json.addProperty("description", coupon.getDescription());
        json.addProperty("discountType", coupon.getDiscountType());
        json.addProperty("discountValue", coupon.getDiscountValue());
        json.addProperty("minimumOrder", coupon.getMinimumOrder());
        if (coupon.getUsageLimit() != null) {
            json.addProperty("usageLimit", coupon.getUsageLimit());
        } else {
            json.add("usageLimit", null);
        }
        json.addProperty("usedCount", coupon.getUsedCount() != null ? coupon.getUsedCount() : 0);
        json.addProperty("status", coupon.getStatus());
        if (coupon.getStartDate() != null) {
            json.addProperty("startDate", coupon.getStartDate().toString());
        }
        if (coupon.getEndDate() != null) {
            json.addProperty("endDate", coupon.getEndDate().toString());
        }
        if (coupon.getCreatedAt() != null) {
            json.addProperty("createdAt", coupon.getCreatedAt().toString());
        }
        if (coupon.getUpdatedAt() != null) {
            json.addProperty("updatedAt", coupon.getUpdatedAt().toString());
        }
        return json;
    }

    private SellerContext requireSellerContext(HttpServletRequest req, HttpServletResponse resp, PrintWriter out)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(Map.of("success", false, "message", "Bạn chưa đăng nhập")));
            return null;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        if (userId == null || role == null || !"seller".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write(gson.toJson(Map.of("success", false, "message", "Bạn không có quyền truy cập")));
            return null;
        }

        try {
            int shopId = ShopDAO.getShopIdByUserId(userId);
            if (shopId <= 0) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write(gson.toJson(Map.of("success", false, "message", "Bạn cần đăng ký shop trước khi tiếp tục")));
                return null;
            }
            return new SellerContext(userId, shopId);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to resolve shop for seller " + userId, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("success", false, "message", "Không thể xác định shop")));
            return null;
        }
    }

    private String normalizeAction(String action, String fallback) {
        if (action == null || action.isBlank()) {
            return fallback;
        }
        return action.trim().toLowerCase(Locale.ROOT);
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDateTime parseDate(String raw, boolean isStart) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(raw);
            return isStart ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
        } catch (Exception ex) {
            return null;
        }
    }

    private static final class SellerContext {
        private final int userId;
        private final int shopId;

        private SellerContext(int userId, int shopId) {
            this.userId = userId;
            this.shopId = shopId;
        }

        int userId() {
            return userId;
        }

        int shopId() {
            return shopId;
        }
    }
}
