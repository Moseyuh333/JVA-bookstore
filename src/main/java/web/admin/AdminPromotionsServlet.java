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
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
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
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private void listPromotions(HttpServletRequest req, PrintWriter out) throws SQLException {
        String search = req.getParameter("search");
        String searchType = req.getParameter("searchType");

        StringBuilder sql = new StringBuilder(
            "SELECT id, name, code, description, discount_type as type, discount_value, start_date as start_at, end_date as end_at, status as active, created_at, updated_at " +
            "FROM promotions WHERE 1=1"
        );

        // Add search conditions
        if (search != null && !search.trim().isEmpty()) {
            if ("code".equals(searchType)) {
                sql.append(" AND code ILIKE ?");
            } else if ("description".equals(searchType)) {
                sql.append(" AND description ILIKE ?");
            } else if ("type".equals(searchType)) {
                sql.append(" AND discount_type ILIKE ?");
            } else {
                // Default "all"
                sql.append(" AND (code ILIKE ? OR description ILIKE ? OR discount_type ILIKE ?)");
            }
        }

        sql.append(" ORDER BY created_at DESC");

        StringBuilder json = new StringBuilder();
        json.append("{\"promotions\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set search parameters
            int paramIndex = 1;
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                if ("code".equals(searchType) || "description".equals(searchType) || "type".equals(searchType)) {
                    pstmt.setString(paramIndex++, pattern);
                } else {
                    // "all" search
                    pstmt.setString(paramIndex++, pattern);
                    pstmt.setString(paramIndex++, pattern);
                    pstmt.setString(paramIndex++, pattern);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;

                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                        .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                        .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                        .append("\"discount_value\":").append(rs.getBigDecimal("discount_value")).append(",")
                        .append("\"start_at\":\"").append(rs.getTimestamp("start_at") != null ? rs.getTimestamp("start_at").toString() : "").append("\",")
                        .append("\"end_at\":\"").append(rs.getTimestamp("end_at") != null ? rs.getTimestamp("end_at").toString() : "").append("\",")
                        .append("\"active\":").append(rs.getBoolean("active")).append(",")
                        .append("\"created_at\":\"").append(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "").append("\",")
                        .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : "").append("\"")
                        .append("}");
                }
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
        String sql = "SELECT id, name, code, description, discount_type as type, discount_value, start_date as start_at, end_date as end_at, status as active, created_at, updated_at " +
                     "FROM promotions WHERE id = ?";

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
                        + "\"discount_value\":" + rs.getBigDecimal("discount_value") + ","
                        + "\"start_at\":\"" + (rs.getTimestamp("start_at") != null ? rs.getTimestamp("start_at").toString() : "") + "\","
                        + "\"end_at\":\"" + (rs.getTimestamp("end_at") != null ? rs.getTimestamp("end_at").toString() : "") + "\","
                        + "\"active\":" + rs.getBoolean("active") + ","
                        + "\"created_at\":\"" + (rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "") + "\","
                        + "\"updated_at\":\"" + (rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : "") + "\""
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
        String discountValueStr = req.getParameter("discount_value");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");

        if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || type == null || discountValueStr == null) {
            out.write("{\"error\":\"Name, code, type and discount value are required\"}");
            return;
        }

        BigDecimal discountValue = new BigDecimal(discountValueStr);
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;

        String sql = "INSERT INTO promotions (name, code, description, discount_type, discount_value, start_date, end_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, code.trim().toUpperCase());
            pstmt.setString(3, description != null ? description.trim() : null);
            pstmt.setString(4, type);
            pstmt.setBigDecimal(5, discountValue);
            pstmt.setString(6, startAt);
            pstmt.setString(7, endAt);
            pstmt.setBoolean(8, active);

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
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        String type = req.getParameter("type");
        String discountValueStr = req.getParameter("discount_value");
        String startAt = req.getParameter("start_at");
        String endAt = req.getParameter("end_at");
        String activeStr = req.getParameter("active");

        if (idStr == null || name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || type == null || discountValueStr == null) {
            out.write("{\"error\":\"ID, name, code, type and discount value are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal discountValue = new BigDecimal(discountValueStr);
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;

        String sql = "UPDATE promotions SET name = ?, code = ?, description = ?, discount_type = ?, discount_value = ?, start_date = ?, end_date = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, code.trim().toUpperCase());
            pstmt.setString(3, description != null ? description.trim() : null);
            pstmt.setString(4, type);
            pstmt.setBigDecimal(5, discountValue);
            pstmt.setString(6, startAt);
            pstmt.setString(7, endAt);
            pstmt.setBoolean(8, active);
            pstmt.setInt(9, id);

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
        String sql = "DELETE FROM promotions WHERE id = ?";

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
