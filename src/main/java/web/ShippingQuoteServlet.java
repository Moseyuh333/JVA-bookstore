package web;

import com.google.gson.Gson;
import dao.ShipperDAO;
import dao.UserAddressDAO;
import models.Shipper;
import models.ShippingQuote;
import models.UserAddress;
import utils.AuthUtil;
import utils.ShippingCalculator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ShippingQuoteServlet", urlPatterns = {"/api/shipping/quote"})
public class ShippingQuoteServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        try {
            Long userId = AuthUtil.resolveUserId(req);
            if (userId == null) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "B���n c��n �`��ng nh��-p �`��� xem phA- v��-n chuy���n.");
                return;
            }

            Long addressId = parseId(req.getParameter("addressId"));
            if (addressId == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thi���u �`��<a ch��% giao hA�ng.");
                return;
            }

            UserAddress address = UserAddressDAO.findById(userId, addressId);
            if (address == null) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "�?��<a ch��% giao hA�ng khA'ng h���p l���.");
                return;
            }

            List<Shipper> shippers = ShipperDAO.findActiveShippers();
            ShippingQuote quote = ShippingCalculator.calculateQuote(address, shippers);
            if (quote == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Khong tim thay don vi van chuyen phu hop. Chung toi chi giao hang trong Viet Nam.");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            BigDecimal shippingFee = quote != null && quote.getFee() != null ? quote.getFee() : BigDecimal.ZERO;
            if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                shippingFee = BigDecimal.ZERO;
            }
            payload.put("shippingFee", shippingFee);
            payload.put("addressId", addressId);

            Map<String, Object> shipperInfo = new HashMap<>();
            shipperInfo.put("id", quote.getShipperId());
            shipperInfo.put("name", quote.getShipperName());
            shipperInfo.put("estimatedTime", quote.getEstimatedTime());
            shipperInfo.put("serviceArea", quote.getServiceArea());
            shipperInfo.put("matchLevel", quote.getMatchLevel() != null ? quote.getMatchLevel().name() : null);
            payload.put("shipper", shipperInfo);

            resp.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            ex.printStackTrace();
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "KhA'ng tA*v �'A���c phA- v��-n chuy���n: " + ex.getMessage());
        }
    }

    private Long parseId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            long value = Long.parseLong(raw.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("message", message);
        resp.getWriter().write(gson.toJson(payload));
    }
}

