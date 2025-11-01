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
import java.util.ArrayList;
import java.util.List;

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
        String search = req.getParameter("search");
        String searchType = req.getParameter("searchType");

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // Build WHERE clause for promotions table
        StringBuilder promoWhere = new StringBuilder();
        if (search != null && !search.trim().isEmpty()) {
            if ("all".equals(searchType) || searchType == null) {
                promoWhere.append(" WHERE (p.code ILIKE ? OR p.description ILIKE ? OR p.discount_scope ILIKE ? OR p.discount_type ILIKE ?)");
                String pattern = "%" + search.trim() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            } else if ("code".equals(searchType)) {
                promoWhere.append(" WHERE p.code ILIKE ?");
                params.add("%" + search.trim() + "%");
            } else if ("description".equals(searchType)) {
                promoWhere.append(" WHERE p.description ILIKE ?");
                params.add("%" + search.trim() + "%");
            } else if ("kind".equals(searchType)) {
                promoWhere.append(" WHERE (p.discount_scope ILIKE ? OR p.discount_scope = CASE WHEN LOWER(?) = 'giảm phí vận chuyển' THEN 'shipping' WHEN LOWER(?) = 'giảm giá sản phẩm' THEN 'product' END)");
                String pattern = "%" + search.trim() + "%";
                params.add(pattern);
                params.add(search.trim());
                params.add(search.trim());
            } else if ("type".equals(searchType)) {
                promoWhere.append(" WHERE p.discount_type ILIKE ?");
                params.add("%" + search.trim() + "%");
            } else if ("status".equals(searchType)) {
                promoWhere.append(" WHERE p.status = ?");
                boolean statusValue = "true".equalsIgnoreCase(search.trim()) || "active".equalsIgnoreCase(search.trim()) || "1".equals(search.trim());
                params.add(statusValue);
            } else {
                // Default to all if invalid searchType
                promoWhere.append(" WHERE (p.code ILIKE ? OR p.description ILIKE ? OR p.discount_scope ILIKE ? OR p.discount_type ILIKE ?)");
                String pattern = "%" + search.trim() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
        }

        // Build WHERE clause for shop_coupons table
        StringBuilder shopWhere = new StringBuilder();
        if (search != null && !search.trim().isEmpty()) {
            if ("all".equals(searchType) || searchType == null) {
                shopWhere.append(" WHERE (sc.code ILIKE ? OR sc.description ILIKE ? OR 'product' ILIKE ? OR sc.discount_type ILIKE ?)");
                String pattern = "%" + search.trim() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            } else if ("code".equals(searchType)) {
                shopWhere.append(" WHERE sc.code ILIKE ?");
                params.add("%" + search.trim() + "%");
            } else if ("description".equals(searchType)) {
                shopWhere.append(" WHERE sc.description ILIKE ?");
                params.add("%" + search.trim() + "%");
            } else if ("kind".equals(searchType)) {
                shopWhere.append(" WHERE ('product' ILIKE ? OR 'product' = CASE WHEN LOWER(?) = 'giảm phí vận chuyển' THEN 'shipping' WHEN LOWER(?) = 'giảm giá sản phẩm' THEN 'product' END)");
                String pattern = "%" + search.trim() + "%";
                params.add(pattern);
                params.add(search.trim());
                params.add(search.trim());
            } else if ("type".equals(searchType)) {
                shopWhere.append(" WHERE sc.discount_type ILIKE ?");
                params.add("%" + search.trim() + "%");
            } else if ("status".equals(searchType)) {
                shopWhere.append(" WHERE sc.status = ?");
                String statusStr = "true".equalsIgnoreCase(search.trim()) || "active".equalsIgnoreCase(search.trim()) || "1".equals(search.trim()) ? "active" : "inactive";
                params.add(statusStr);
            } else {
                // Default to all if invalid searchType
                shopWhere.append(" WHERE (sc.code ILIKE ? OR sc.description ILIKE ? OR 'product' ILIKE ? OR sc.discount_type ILIKE ?)");
                String pattern = "%" + search.trim() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
        }

        sql.append("SELECT * FROM (")
          .append("SELECT ")
          .append("    p.id, ")
          .append("    p.name, ")
          .append("    p.code, ")
          .append("    p.description, ")
          .append("    p.discount_scope AS kind, ")
          .append("    p.discount_type AS type, ")
          .append("    p.discount_value, ")
          .append("    p.max_discount_value, ")
          .append("    p.min_order_value, ")
          .append("    p.start_date AS start_at, ")
          .append("    p.end_date AS end_at, ")
          .append("    p.status AS active, ")
          .append("    NULL AS shop_name, ")
          .append("    'system' AS source ")
          .append("FROM promotions p")
          .append(promoWhere.toString())
          .append(" UNION ALL ")
          .append("SELECT ")
          .append("    sc.id, ")
          .append("    sc.code AS name, ")
          .append("    sc.code, ")
          .append("    sc.description, ")
          .append("    'product' AS kind, ")
          .append("    sc.discount_type AS type, ")
          .append("    sc.discount_value, ")
          .append("    NULL AS max_discount_value, ")
          .append("    sc.minimum_order AS min_order_value, ")
          .append("    sc.start_date AS start_at, ")
          .append("    sc.end_date AS end_at, ")
          .append("    (CASE WHEN sc.status = 'active' THEN true ELSE false END) AS active, ")
          .append("    s.name AS shop_name, ")
          .append("    'shop' AS source ")
          .append("FROM shop_coupons sc ")
          .append("LEFT JOIN shops s ON sc.shop_id = s.id")
          .append(shopWhere.toString())
          .append(") AS combined_promotions ORDER BY start_at DESC");

        StringBuilder json = new StringBuilder();
        json.append("{\"promotions\":[");

        int totalPromotions = 0;
        int activePromotions = 0;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set search parameters from the params list
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) param);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {

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
                        .append("\"active\":").append(rs.getBoolean("active"))
                        .append(",\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                        .append("\"source\":\"").append(escapeJson(rs.getString("source"))).append("\"")
                        .append("}");
                    
                }
            }
        }

        json.append("],\"total\":").append(totalPromotions).append(",\"active\":").append(activePromotions).append("}");
        out.write(json.toString());
    }

    private void getPromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String source = req.getParameter("source"); // "system", "shop", or null for both

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
            String sqlSystem =
                "SELECT p.id, p.name, p.code, p.description, " +
                "p.discount_type AS type, " +
                "p.discount_scope AS kind, " +
                "p.discount_value, " +
                "p.max_discount_value, " +
                "p.min_order_value, " +
                "p.start_date AS start_at, " +
                "p.end_date AS end_at, " +
                "p.status AS active, " +
                "p.shop_id, s.name AS shop_name, 'system' AS source " +
                "FROM promotions p " +
                "LEFT JOIN shops s ON p.shop_id = s.id " +
                "WHERE p.id = ?";

            String sqlShop =
                "SELECT sc.id, sc.code AS name, sc.code, sc.description, " +
                "sc.discount_type AS type, " +
                "'product' AS kind, " +
                "sc.discount_value, " +
                "NULL AS max_discount_value, " +
                "sc.minimum_order AS min_order_value, " +
                "sc.start_date AS start_at, " +
                "sc.end_date AS end_at, " +
                "(CASE WHEN sc.status = 'active' THEN TRUE ELSE FALSE END) AS active, " +
                "sc.shop_id, s.name AS shop_name, 'shop' AS source " +
                "FROM shop_coupons sc " +
                "LEFT JOIN shops s ON sc.shop_id = s.id " +
                "WHERE sc.id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            if ("shop".equals(source)) {
                try (PreparedStatement ps = conn.prepareStatement(sqlShop)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            writePromoJson(rs, out);
                            return;
                        }
                    }
                }
            } else if ("system".equals(source)) {
                try (PreparedStatement ps = conn.prepareStatement(sqlSystem)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            writePromoJson(rs, out);
                            return;
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(sqlSystem)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            writePromoJson(rs, out);
                            return;
                        }
                    }
                }
                try (PreparedStatement ps2 = conn.prepareStatement(sqlShop)) {
                    ps2.setInt(1, id);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) {
                            writePromoJson(rs2, out);
                            return;
                        }
                    }
                }
            }

            out.write("{\"error\":\"Promotion not found\"}");
        }
    }


    private void writePromoJson(ResultSet rs, PrintWriter out) throws SQLException {
        String json = "{"
            + "\"id\":" + rs.getInt("id") + ","
            + "\"name\":\"" + escapeJson(rs.getString("name")) + "\","
            + "\"code\":\"" + escapeJson(rs.getString("code")) + "\","
            + "\"description\":\"" + escapeJson(rs.getString("description")) + "\","
            + "\"type\":\"" + escapeJson(rs.getString("type")) + "\","
            + "\"kind\":\"" + escapeJson(rs.getString("kind")) + "\","
            + "\"discount_value\":" + rs.getBigDecimal("discount_value") + ","
            + "\"max_discount_value\":" + (rs.getBigDecimal("max_discount_value") != null ? rs.getBigDecimal("max_discount_value") : 0) + ","
            + "\"min_order_value\":" + (rs.getBigDecimal("min_order_value") != null ? rs.getBigDecimal("min_order_value") : 0) + ","
            + "\"start_at\":\"" + (rs.getTimestamp("start_at") != null ? rs.getTimestamp("start_at").toString() : "") + "\","
            + "\"end_at\":\"" + (rs.getTimestamp("end_at") != null ? rs.getTimestamp("end_at").toString() : "") + "\","
            + "\"active\":" + rs.getBoolean("active") + ","
            + "\"shop_id\":" + rs.getInt("shop_id") + ","
            + "\"shop_name\":\"" + escapeJson(rs.getString("shop_name")) + "\","
            + "\"source\":\"" + escapeJson(rs.getString("source")) + "\""
            + "}";
        out.write(json);
    }


    private void createPromotion(HttpServletRequest req, PrintWriter out) throws SQLException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String kind = req.getParameter("kind");
        String discountValueStr = req.getParameter("discount_value");
        String maxDiscountValueStr = req.getParameter("max_discount_value");
        String minOrderValueStr = req.getParameter("min_order_value");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");
        String shopIdStr = req.getParameter("shop_id");

        if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || type == null || discountValueStr == null) {
            out.write("{\"error\":\"Name, code, type and discount value are required\"}");
            return;
        }

        BigDecimal discountValue = new BigDecimal(discountValueStr);
        BigDecimal maxDiscountValue = maxDiscountValueStr != null && !maxDiscountValueStr.trim().isEmpty() ? new BigDecimal(maxDiscountValueStr) : null;
        BigDecimal minOrderValue = minOrderValueStr != null && !minOrderValueStr.trim().isEmpty() ? new BigDecimal(minOrderValueStr) : null;
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;
        Integer shopId = shopIdStr != null && !shopIdStr.trim().isEmpty() ? Integer.parseInt(shopIdStr) : null;

    String sql = "INSERT INTO promotions (name, code, description, discount_type, discount_scope, discount_value, max_discount_value, min_order_value, start_date, end_date, status, shop_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, start_date AS created_at";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, code.trim().toUpperCase());
            pstmt.setString(3, description != null ? description.trim() : null);
            pstmt.setString(4, type);
            pstmt.setString(5, kind);
            pstmt.setBigDecimal(6, discountValue);
            if (maxDiscountValue != null) {
                pstmt.setBigDecimal(7, maxDiscountValue);
            } else {
                pstmt.setNull(7, java.sql.Types.DECIMAL);
            }
            if (minOrderValue != null) {
                pstmt.setBigDecimal(8, minOrderValue);
            } else {
                pstmt.setNull(8, java.sql.Types.DECIMAL);
            }
            Timestamp startTimestamp = parseToTimestamp(startAt);
            Timestamp endTimestamp = parseToTimestamp(endAt);
            pstmt.setTimestamp(9, startTimestamp);
            pstmt.setTimestamp(10, endTimestamp);

            pstmt.setBoolean(11, active);
            if (shopId != null) {
                pstmt.setInt(12, shopId);
            } else {
                pstmt.setNull(12, java.sql.Types.INTEGER);
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
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String kind = req.getParameter("kind");
        String discountValueStr = req.getParameter("discount_value");
        String maxDiscountValueStr = req.getParameter("max_discount_value");
        String minOrderValueStr = req.getParameter("min_order_value");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");
        String shopIdStr = req.getParameter("shop_id");
        String source = req.getParameter("source");

        if (idStr == null || discountValueStr == null) {
            out.write("{\"error\":\"ID and discount value are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal discountValue = new BigDecimal(discountValueStr);
        BigDecimal maxDiscountValue = maxDiscountValueStr != null && !maxDiscountValueStr.trim().isEmpty() ? new BigDecimal(maxDiscountValueStr) : null;
        BigDecimal minOrderValue = minOrderValueStr != null && !minOrderValueStr.trim().isEmpty() ? new BigDecimal(minOrderValueStr) : null;

        try (Connection conn = DBUtil.getConnection()) {
            if ("shop".equals(source)) {
                // Update shop_coupons
                if (code == null || code.trim().isEmpty() || type == null) {
                    out.write("{\"error\":\"Code and type are required for shop coupons\"}");
                    return;
                }

                boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;
                String sql = "UPDATE shop_coupons SET code = ?, description = ?, discount_type = ?, discount_value = ?, max_discount_value = ?, minimum_order = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, code.trim().toUpperCase());
                    pstmt.setString(2, description != null ? description.trim() : null);
                    pstmt.setString(3, type);
                    pstmt.setBigDecimal(4, discountValue);
                    if (maxDiscountValue != null) {
                        pstmt.setBigDecimal(5, maxDiscountValue);
                    } else {
                        pstmt.setNull(5, java.sql.Types.DECIMAL);
                    }
                    if (minOrderValue != null) {
                        pstmt.setBigDecimal(6, minOrderValue);
                    } else {
                        pstmt.setNull(6, java.sql.Types.DECIMAL);
                    }
                    Timestamp startTimestamp = parseToTimestamp(startAt);
                    Timestamp endTimestamp = parseToTimestamp(endAt);
                    pstmt.setTimestamp(7, startTimestamp);
                    pstmt.setTimestamp(8, endTimestamp);
                    pstmt.setString(9, active ? "active" : "inactive");
                    pstmt.setInt(10, id);

                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        out.write("{\"message\":\"Shop coupon updated successfully\"}");
                    } else {
                        out.write("{\"error\":\"Shop coupon not found\"}");
                    }
                }
            } else {
                // Update promotions
                if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || type == null) {
                    out.write("{\"error\":\"Name, code, and type are required for system promotions\"}");
                    return;
                }

                boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;
                Integer shopId = shopIdStr != null && !shopIdStr.trim().isEmpty() ? Integer.parseInt(shopIdStr) : null;
                String sql = "UPDATE promotions SET name = ?, code = ?, description = ?, discount_type = ?, discount_scope = ?, discount_value = ?, max_discount_value = ?, min_order_value = ?, start_date = ?, end_date = ?, status = ?, shop_id = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name.trim());
                    pstmt.setString(2, code.trim().toUpperCase());
                    pstmt.setString(3, description != null ? description.trim() : null);
                    pstmt.setString(4, type);
                    pstmt.setString(5, kind);
                    pstmt.setBigDecimal(6, discountValue);
                    if (maxDiscountValue != null) {
                        pstmt.setBigDecimal(7, maxDiscountValue);
                    } else {
                        pstmt.setNull(7, java.sql.Types.DECIMAL);
                    }
                    if (minOrderValue != null) {
                        pstmt.setBigDecimal(8, minOrderValue);
                    } else {
                        pstmt.setNull(8, java.sql.Types.DECIMAL);
                    }
                    Timestamp startTimestamp = parseToTimestamp(startAt);
                    Timestamp endTimestamp = parseToTimestamp(endAt);
                    pstmt.setTimestamp(9, startTimestamp);
                    pstmt.setTimestamp(10, endTimestamp);
                    pstmt.setBoolean(11, active);
                    if (shopId != null) {
                        pstmt.setInt(12, shopId);
                    } else {
                        pstmt.setNull(12, java.sql.Types.INTEGER);
                    }
                    pstmt.setInt(13, id);

                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        out.write("{\"message\":\"Promotion updated successfully\"}");
                    } else {
                        out.write("{\"error\":\"Promotion not found\"}");
                    }
                }
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
        String sql = "DELETE FROM promotions WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 🪄 Sau khi xóa thành công → đồng bộ lại sequence
                try (PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT setval('promotions_id_seq', COALESCE((SELECT MAX(id) FROM promotions), 0) + 1)"
                )) {
                    ps2.execute();
                }

                out.write("{\"message\":\"Promotion deleted successfully and sequence updated!\"}");
            } else {
                out.write("{\"error\":\"Promotion not found\"}");
            }
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
