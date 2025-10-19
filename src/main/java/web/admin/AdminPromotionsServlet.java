package web.admin;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AdminPromotionsServlet", urlPatterns = {"/api/admin/promotions"})
public class AdminPromotionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listPromotions(out);
            } else if ("get".equals(action)) {
                getPromotion(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                createPromotion(req, out);
            } else if ("update".equals(action)) {
                updatePromotion(req, out);
            } else if ("delete".equals(action)) {
                deletePromotion(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private void listPromotions(PrintWriter out) throws SQLException {
        String sql = "SELECT id, code, description, type, discount_value, max_discount, min_order, " +
                     "usage_limit, used_count, start_at, end_at, active, apply_to, created_at, updated_at " +
                     "FROM coupons ORDER BY created_at DESC";

        StringBuilder json = new StringBuilder();
        json.append("{\"promotions\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                    .append("\"discount_value\":").append(rs.getBigDecimal("discount_value")).append(",")
                    .append("\"max_discount\":").append(rs.getBigDecimal("max_discount") != null ? rs.getBigDecimal("max_discount") : "null").append(",")
                    .append("\"min_order\":").append(rs.getBigDecimal("min_order") != null ? rs.getBigDecimal("min_order") : "null").append(",")
                    .append("\"usage_limit\":").append(rs.getInt("usage_limit") != 0 ? rs.getInt("usage_limit") : "null").append(",")
                    .append("\"used_count\":").append(rs.getInt("used_count")).append(",")
                    .append("\"start_at\":\"").append(rs.getTimestamp("start_at")).append("\",")
                    .append("\"end_at\":\"").append(rs.getTimestamp("end_at")).append("\",")
                    .append("\"active\":").append(rs.getBoolean("active")).append(",")
                    .append("\"apply_to\":\"").append(escapeJson(rs.getString("apply_to"))).append("\",")
                    .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                    .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                    .append("}");
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    private void getPromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "SELECT id, code, description, type, discount_value, max_discount, min_order, " +
                     "usage_limit, used_count, start_at, end_at, active, apply_to, created_at, updated_at " +
                     "FROM coupons WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = "{"
                        + "\"id\":" + rs.getInt("id") + ","
                        + "\"code\":\"" + escapeJson(rs.getString("code")) + "\","
                        + "\"description\":\"" + escapeJson(rs.getString("description")) + "\","
                        + "\"type\":\"" + escapeJson(rs.getString("type")) + "\","
                        + "\"discount_value\":" + rs.getBigDecimal("discount_value") + ","
                        + "\"max_discount\":" + (rs.getBigDecimal("max_discount") != null ? rs.getBigDecimal("max_discount") : "null") + ","
                        + "\"min_order\":" + (rs.getBigDecimal("min_order") != null ? rs.getBigDecimal("min_order") : "null") + ","
                        + "\"usage_limit\":" + (rs.getInt("usage_limit") != 0 ? rs.getInt("usage_limit") : "null") + ","
                        + "\"used_count\":" + rs.getInt("used_count") + ","
                        + "\"start_at\":\"" + rs.getTimestamp("start_at") + "\","
                        + "\"end_at\":\"" + rs.getTimestamp("end_at") + "\","
                        + "\"active\":" + rs.getBoolean("active") + ","
                        + "\"apply_to\":\"" + escapeJson(rs.getString("apply_to")) + "\","
                        + "\"created_at\":\"" + rs.getTimestamp("created_at") + "\","
                        + "\"updated_at\":\"" + rs.getTimestamp("updated_at") + "\""
                        + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Promotion not found\"}");
                }
            }
        }
    }

    private void createPromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String discountValueStr = req.getParameter("discount_value");
        String maxDiscountStr = req.getParameter("max_discount");
        String minOrderStr = req.getParameter("min_order");
        String usageLimitStr = req.getParameter("usage_limit");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");
        String applyTo = req.getParameter("apply_to");

        if (code == null || code.trim().isEmpty() || type == null || discountValueStr == null) {
            out.write("{\"error\":\"Code, type and discount value are required\"}");
            return;
        }

        BigDecimal discountValue = new BigDecimal(discountValueStr);
        BigDecimal maxDiscount = maxDiscountStr != null && !maxDiscountStr.trim().isEmpty() ? new BigDecimal(maxDiscountStr) : null;
        BigDecimal minOrder = minOrderStr != null && !minOrderStr.trim().isEmpty() ? new BigDecimal(minOrderStr) : null;
        Integer usageLimit = usageLimitStr != null && !usageLimitStr.trim().isEmpty() ? Integer.parseInt(usageLimitStr) : null;
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;

        String sql = "INSERT INTO coupons (code, description, type, discount_value, max_discount, min_order, " +
                     "usage_limit, start_at, end_at, active, apply_to) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code.trim().toUpperCase());
            pstmt.setString(2, description != null ? description.trim() : null);
            pstmt.setString(3, type);
            pstmt.setBigDecimal(4, discountValue);
            if (maxDiscount != null) {
                pstmt.setBigDecimal(5, maxDiscount);
            } else {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
            }
            if (minOrder != null) {
                pstmt.setBigDecimal(6, minOrder);
            } else {
                pstmt.setNull(6, java.sql.Types.DECIMAL);
            }
            if (usageLimit != null) {
                pstmt.setInt(7, usageLimit);
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            pstmt.setString(8, startAt != null ? startAt : "NOW()");
            pstmt.setString(9, endAt);
            pstmt.setBoolean(10, active);
            pstmt.setString(11, applyTo != null ? applyTo : "product");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Promotion created successfully\"}");
            } else {
                out.write("{\"error\":\"Failed to create promotion\"}");
            }
        }
    }

    private void updatePromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String discountValueStr = req.getParameter("discount_value");
        String maxDiscountStr = req.getParameter("max_discount");
        String minOrderStr = req.getParameter("min_order");
        String usageLimitStr = req.getParameter("usage_limit");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");
        String applyTo = req.getParameter("apply_to");

        if (idStr == null || code == null || code.trim().isEmpty() || type == null || discountValueStr == null) {
            out.write("{\"error\":\"ID, code, type and discount value are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal discountValue = new BigDecimal(discountValueStr);
        BigDecimal maxDiscount = maxDiscountStr != null && !maxDiscountStr.trim().isEmpty() ? new BigDecimal(maxDiscountStr) : null;
        BigDecimal minOrder = minOrderStr != null && !minOrderStr.trim().isEmpty() ? new BigDecimal(minOrderStr) : null;
        Integer usageLimit = usageLimitStr != null && !usageLimitStr.trim().isEmpty() ? Integer.parseInt(usageLimitStr) : null;
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;

        String sql = "UPDATE coupons SET code = ?, description = ?, type = ?, discount_value = ?, " +
                     "max_discount = ?, min_order = ?, usage_limit = ?, start_at = ?, end_at = ?, " +
                     "active = ?, apply_to = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code.trim().toUpperCase());
            pstmt.setString(2, description != null ? description.trim() : null);
            pstmt.setString(3, type);
            pstmt.setBigDecimal(4, discountValue);
            if (maxDiscount != null) {
                pstmt.setBigDecimal(5, maxDiscount);
            } else {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
            }
            if (minOrder != null) {
                pstmt.setBigDecimal(6, minOrder);
            } else {
                pstmt.setNull(6, java.sql.Types.DECIMAL);
            }
            if (usageLimit != null) {
                pstmt.setInt(7, usageLimit);
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            pstmt.setString(8, startAt);
            pstmt.setString(9, endAt);
            pstmt.setBoolean(10, active);
            pstmt.setString(11, applyTo != null ? applyTo : "product");
            pstmt.setInt(12, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Promotion updated successfully\"}");
            } else {
                out.write("{\"error\":\"Promotion not found\"}");
            }
        }
    }

    private void deletePromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM coupons WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Promotion deleted successfully\"}");
            } else {
                out.write("{\"error\":\"Promotion not found\"}");
            }
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
