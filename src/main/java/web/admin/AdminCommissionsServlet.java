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

@WebServlet(name = "AdminCommissionsServlet", urlPatterns = {"/api/admin/commissions"})
public class AdminCommissionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listCommissions(out);
            } else if ("get".equals(action)) {
                getCommission(req, out);
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
                createCommission(req, out);
            } else if ("update".equals(action)) {
                updateCommission(req, out);
            } else if ("delete".equals(action)) {
                deleteCommission(req, out);
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

    private void listCommissions(PrintWriter out) throws SQLException {
        String sql = "SELECT sd.id, sd.shop_id, s.name as shop_name, sd.discount_rate, sd.start_date, " +
                     "sd.end_date, sd.active, sd.description, sd.created_at " +
                     "FROM store_discounts sd " +
                     "LEFT JOIN shops s ON sd.shop_id = s.id " +
                     "ORDER BY sd.created_at DESC";

        StringBuilder json = new StringBuilder();
        json.append("{\"commissions\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"shop_id\":").append(rs.getInt("shop_id")).append(",")
                    .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                    .append("\"discount_rate\":").append(rs.getBigDecimal("discount_rate")).append(",")
                    .append("\"start_date\":\"").append(rs.getTimestamp("start_date")).append("\",")
                    .append("\"end_date\":\"").append(rs.getTimestamp("end_date")).append("\",")
                    .append("\"active\":").append(rs.getBoolean("active")).append(",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"")
                    .append("}");
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    private void getCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "SELECT sd.id, sd.shop_id, s.name as shop_name, sd.discount_rate, sd.start_date, " +
                     "sd.end_date, sd.active, sd.description, sd.created_at " +
                     "FROM store_discounts sd " +
                     "LEFT JOIN shops s ON sd.shop_id = s.id " +
                     "WHERE sd.id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = "{"
                        + "\"id\":" + rs.getInt("id") + ","
                        + "\"shop_id\":" + rs.getInt("shop_id") + ","
                        + "\"shop_name\":\"" + escapeJson(rs.getString("shop_name")) + "\","
                        + "\"discount_rate\":" + rs.getBigDecimal("discount_rate") + ","
                        + "\"start_date\":\"" + rs.getTimestamp("start_date") + "\","
                        + "\"end_date\":\"" + rs.getTimestamp("end_date") + "\","
                        + "\"active\":" + rs.getBoolean("active") + ","
                        + "\"description\":\"" + escapeJson(rs.getString("description")) + "\","
                        + "\"created_at\":\"" + rs.getTimestamp("created_at") + "\""
                        + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Commission not found\"}");
                }
            }
        }
    }

    private void createCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String shopIdStr = req.getParameter("shop_id");
        String discountRateStr = req.getParameter("discount_rate");
        String startDate = req.getParameter("start_date");
        String endDate = req.getParameter("end_date");
        String activeStr = req.getParameter("active");
        String description = req.getParameter("description");

        if (shopIdStr == null || discountRateStr == null) {
            out.write("{\"error\":\"Shop ID and discount rate are required\"}");
            return;
        }

        int shopId = Integer.parseInt(shopIdStr);
        BigDecimal discountRate = new BigDecimal(discountRateStr);
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;

        String sql = "INSERT INTO store_discounts (shop_id, discount_rate, start_date, end_date, active, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, shopId);
            pstmt.setBigDecimal(2, discountRate);
            pstmt.setString(3, startDate != null ? startDate : "NOW()");
            pstmt.setString(4, endDate);
            pstmt.setBoolean(5, active);
            pstmt.setString(6, description != null ? description.trim() : null);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Commission created successfully\"}");
            } else {
                out.write("{\"error\":\"Failed to create commission\"}");
            }
        }
    }

    private void updateCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String discountRateStr = req.getParameter("discount_rate");
        String startDate = req.getParameter("start_date");
        String endDate = req.getParameter("end_date");
        String activeStr = req.getParameter("active");
        String description = req.getParameter("description");

        if (idStr == null || discountRateStr == null) {
            out.write("{\"error\":\"ID and discount rate are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal discountRate = new BigDecimal(discountRateStr);
        boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : true;

        String sql = "UPDATE store_discounts SET discount_rate = ?, start_date = ?, end_date = ?, " +
                     "active = ?, description = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, discountRate);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            pstmt.setBoolean(4, active);
            pstmt.setString(5, description != null ? description.trim() : null);
            pstmt.setInt(6, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Commission updated successfully\"}");
            } else {
                out.write("{\"error\":\"Commission not found\"}");
            }
        }
    }

    private void deleteCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM store_discounts WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Commission deleted successfully\"}");
            } else {
                out.write("{\"error\":\"Commission not found\"}");
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
