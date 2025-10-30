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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "AdminPromotionsServlet", urlPatterns = {"/api/admin/promotions"})
public class AdminPromotionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listPromotions(req, out);
            } else if ("get".equals(action)) {
                getPromotion(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
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
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            out.flush();
        }
    }

    private void listPromotions(HttpServletRequest req, PrintWriter out) throws SQLException {
        StringBuilder json = new StringBuilder();
        json.append("{\"promotions\":[");

        int totalPromotions = 0;
        int activePromotions = 0;

        String sql =
        "SELECT " +
        "    p.id, " +
        "    p.name, " +
        "    p.code, " +
        "    p.description, " +
        "    p.discount_scope AS kind, " +
        "    p.discount_type AS type, " +
        "    p.discount_value, " +
        "    p.max_discount_value, " +
        "    p.min_order_value, " +
        "    p.start_date AS start_at, " +
        "    p.end_date AS end_at, " +
        "    p.status AS active, " +
        "    NULL AS shop_name, " +
        "    'system' AS source " +
        "FROM promotions p " +
        "UNION ALL " +
        "SELECT " +
        "    sc.id, " +
        "    sc.code AS name, " +
        "    sc.code, " +
        "    sc.description, " +
        "    'product' AS kind, " +
        "    sc.discount_type AS type, " +
        "    sc.discount_value, " +
        "    NULL AS max_discount_value, " +
        "    sc.minimum_order AS min_order_value, " +
        "    sc.start_date AS start_at, " +
        "    sc.end_date AS end_at, " +
        "    (CASE WHEN sc.status = 'active' THEN true ELSE false END) AS active, " +
        "    s.name AS shop_name, " +
        "    'shop' AS source " +
        "FROM shop_coupons sc " +
        "LEFT JOIN shops s ON sc.shop_id = s.id " +
        "ORDER BY start_at DESC;";


        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                totalPromotions++;
                if (rs.getBoolean("active")) {
                    activePromotions++;
                }

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                    .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"kind\":\"").append(escapeJson(rs.getString("kind"))).append("\",")
                    .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                    .append("\"discount_value\":").append(rs.getBigDecimal("discount_value") != null ? rs.getBigDecimal("discount_value") : 0).append(",")
                    .append("\"max_discount_value\":").append(rs.getBigDecimal("max_discount_value") != null ? rs.getBigDecimal("max_discount_value") : 0).append(",")
                    .append("\"min_order_value\":").append(rs.getBigDecimal("min_order_value") != null ? rs.getBigDecimal("min_order_value") : 0).append(",")
                    .append("\"start_at\":\"").append(rs.getTimestamp("start_at") != null ? rs.getTimestamp("start_at").toString() : "").append("\",")
                    .append("\"end_at\":\"").append(rs.getTimestamp("end_at") != null ? rs.getTimestamp("end_at").toString() : "").append("\",")
                    .append("\"active\":").append(rs.getBoolean("active")).append(",")
                    .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                    .append("\"source\":\"").append(escapeJson(rs.getString("source"))).append("\"")
                    .append("}");
            }
        }

        json.append("],\"total\":").append(totalPromotions)
            .append(",\"active\":").append(activePromotions)
            .append("}");
        out.write(json.toString());
    }


    private void getPromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
    String sql =
        "SELECT p.id, p.name, p.code, p.description, " +
        "p.discount_type AS type, " +
        "p.discount_scope AS kind, " +
        "p.discount_value, " +
        "p.start_date AS start_at, " +
        "p.end_date AS end_at, " +
        "p.status AS active, " +
        "p.shop_id, " +
        "s.name AS shop_name " +
        "FROM promotions p " +
        "LEFT JOIN shops s ON p.shop_id = s.id " +
        "WHERE p.id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = "{"
                        + "\"id\":" + rs.getInt("id") + ","
                        + "\"name\":\"" + escapeJson(rs.getString("name")) + "\","
                        + "\"code\":\"" + escapeJson(rs.getString("code")) + "\","
                        + "\"description\":\"" + escapeJson(rs.getString("description")) + "\","
                        + "\"type\":\"" + escapeJson(rs.getString("type")) + "\","
                        + "\"kind\":\"" + escapeJson(rs.getString("kind")) + "\","
                        + "\"discount_value\":" + rs.getBigDecimal("discount_value") + ","
                        + "\"start_at\":\"" + (rs.getTimestamp("start_at") != null ? rs.getTimestamp("start_at").toString() : "") + "\","
                        + "\"end_at\":\"" + (rs.getTimestamp("end_at") != null ? rs.getTimestamp("end_at").toString() : "") + "\","
                        + "\"active\":" + rs.getBoolean("active") + ","
                        + "\"shop_id\":" + rs.getInt("shop_id") + ""
                        + ",\"shop_name\":\"" + escapeJson(rs.getString("shop_name")) + "\""
                        + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Promotion not found\"}");
                }
            }
        }
    }

    private void createPromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String kind = req.getParameter("kind");
        String discountValueStr = req.getParameter("discount_value");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");
        String shopIdStr = req.getParameter("shop_id");

        if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || type == null || discountValueStr == null) {
            out.write("{\"error\":\"Name, code, type and discount value are required\"}");
            return;
        }

        BigDecimal discountValue = new BigDecimal(discountValueStr);
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;
        Integer shopId = shopIdStr != null && !shopIdStr.trim().isEmpty() ? Integer.parseInt(shopIdStr) : null;

    String sql = "INSERT INTO promotions (name, code, description, discount_type, discount_scope, discount_value, start_date, end_date, status, shop_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, start_date AS created_at";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, code.trim().toUpperCase());
            pstmt.setString(3, description != null ? description.trim() : null);
            pstmt.setString(4, type);
            pstmt.setString(5, kind);
            pstmt.setBigDecimal(6, discountValue);
            Timestamp startTimestamp = parseToTimestamp(startAt);
            Timestamp endTimestamp = parseToTimestamp(endAt);
            pstmt.setTimestamp(7, startTimestamp);
            pstmt.setTimestamp(8, endTimestamp);

            pstmt.setBoolean(9, active);
            if (shopId != null) {
                pstmt.setInt(10, shopId);
            } else {
                pstmt.setNull(10, java.sql.Types.INTEGER);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt("id");
                    java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                    out.write("{\"id\":" + newId + ", \"created_at\":\"" + (createdAt != null ? createdAt.toString() : "") + "\", \"message\":\"Promotion created successfully\"}");
                } else {
                    out.write("{\"error\":\"Failed to create promotion\"}");
                }
            }
        }
    }

    private void updatePromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String source = req.getParameter("source");
        if (source == null) source = "system";

        // Common fields
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String discountValueStr = req.getParameter("discount_value");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");

        if (idStr == null || code == null || discountValueStr == null) {
            out.write("{\"error\":\"Thiếu dữ liệu bắt buộc\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal discountValue = new BigDecimal(discountValueStr);
        boolean active = activeStr != null && (activeStr.equalsIgnoreCase("true") || activeStr.equalsIgnoreCase("active"));

        try (Connection conn = DBUtil.getConnection()) {
            String sql;
            if ("shop".equals(source)) {
                sql = "UPDATE shop_coupons SET code = ?, description = ?, discount_type = ?, discount_value = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, code.trim());
                    ps.setString(2, description);
                    ps.setString(3, type);
                    ps.setBigDecimal(4, discountValue);
                    ps.setTimestamp(5, parseToTimestamp(startAt));
                    ps.setTimestamp(6, parseToTimestamp(endAt));
                    ps.setString(7, active ? "active" : "inactive");
                    ps.setInt(8, id);

                    int rows = ps.executeUpdate();
                    out.write(rows > 0 ? "{\"message\":\"Shop coupon updated\"}" : "{\"error\":\"Not found\"}");
                }
            } else {
                sql = "UPDATE promotions SET code = ?, description = ?, discount_type = ?, discount_value = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, code.trim());
                    ps.setString(2, description);
                    ps.setString(3, type);
                    ps.setBigDecimal(4, discountValue);
                    ps.setTimestamp(5, parseToTimestamp(startAt));
                    ps.setTimestamp(6, parseToTimestamp(endAt));
                    ps.setBoolean(7, active);
                    ps.setInt(8, id);

                    int rows = ps.executeUpdate();
                    out.write(rows > 0 ? "{\"message\":\"System promotion updated\"}" : "{\"error\":\"Not found\"}");
                }
            }
        }
    }

    private void deletePromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String source = req.getParameter("source");
        if (source == null) source = "system";

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String table = "shop".equals(source) ? "shop_coupons" : "promotions";
        String sql = "DELETE FROM " + table + " WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            out.write(rows > 0 ? "{\"message\":\"Deleted successfully\"}" : "{\"error\":\"Not found\"}");
        }
    }


    private Timestamp parseToTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;

        // Thử parse theo nhiều định dạng có thể có
        String[] patterns = {
            "dd/MM/yyyy hh:mm a",  // 22/10/2025 02:45 PM (AM/PM)
            "dd/MM/yyyy HH:mm",    // 22/10/2025 14:45 (24h format)
            "yyyy-MM-dd'T'HH:mm"   // 2025-10-22T14:45 (from datetime-local input)
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dt = LocalDateTime.parse(dateStr.trim(), fmt);
                return Timestamp.valueOf(dt);
            } catch (Exception ignored) {}
        }

        System.err.println("⚠️ [parseToTimestamp] Unrecognized format: " + dateStr);
        return null;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replaceAll("[\\x00-\\x1F\\x7F-\\x9F]", "")
                  .replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
