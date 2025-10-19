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
import java.sql.*;

@WebServlet(name = "AdminCommissionsServlet", urlPatterns = {"/api/admin/commissions"})
public class AdminCommissionsServlet extends HttpServlet {

    // ====== GET (LIST + ONE) ======
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

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
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            out.flush();
        }
    }

    // ====== POST (CREATE / UPDATE / DELETE) ======
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

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
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            out.flush();
        }
    }

    // ====== LIST ======
    private void listCommissions(PrintWriter out) throws SQLException {
        String sql =
            "SELECT sd.id, sd.shop_id, s.name AS shop_name, " +
            "sd.discount_rate AS rate, sd.start_date AS since, " +
            "sd.end_date, sd.active, sd.description " +
            "FROM store_discounts sd " +
            "LEFT JOIN shops s ON sd.shop_id = s.id " +
            "ORDER BY sd.id DESC";

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
                    .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                    .append("\"rate\":").append(rs.getBigDecimal("rate") != null ? rs.getBigDecimal("rate") : BigDecimal.ZERO).append(",")
                    .append("\"since\":\"").append(rs.getTimestamp("since") != null ? rs.getTimestamp("since") : "").append("\",")
                    .append("\"end_date\":\"").append(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date") : "").append("\",")
                    .append("\"active\":").append(rs.getBoolean("active")).append(",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\"")
                    .append("}");
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    // ====== GET ONE ======
    private void getCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql =
            "SELECT sd.id, sd.shop_id, s.name AS shop_name, " +
            "sd.discount_rate AS rate, sd.start_date AS since, " +
            "sd.end_date, sd.active, sd.description " +
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
                        + "\"shop_name\":\"" + escapeJson(rs.getString("shop_name")) + "\","
                        + "\"rate\":" + rs.getBigDecimal("rate") + ","
                        + "\"since\":\"" + rs.getTimestamp("since") + "\","
                        + "\"end_date\":\"" + rs.getTimestamp("end_date") + "\","
                        + "\"active\":" + rs.getBoolean("active") + ","
                        + "\"description\":\"" + escapeJson(rs.getString("description")) + "\""
                        + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Commission not found\"}");
                }
            }
        }
    }

    // ====== CREATE ======
    private void createCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String shopIdStr = req.getParameter("shop_id");
        String rateStr = req.getParameter("discount_rate");
        String desc = req.getParameter("description");

        if (shopIdStr == null || rateStr == null) {
            out.write("{\"error\":\"Shop ID and discount rate are required\"}");
            return;
        }

        int shopId = Integer.parseInt(shopIdStr);
        BigDecimal rate = new BigDecimal(rateStr);
        boolean active = req.getParameter("active") == null || Boolean.parseBoolean(req.getParameter("active"));

        String sql = "INSERT INTO store_discounts (shop_id, discount_rate, start_date, active, description) " +
                     "VALUES (?, ?, NOW(), ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, shopId);
            pstmt.setBigDecimal(2, rate);
            pstmt.setBoolean(3, active);
            pstmt.setString(4, desc != null ? desc.trim() : null);

            int rows = pstmt.executeUpdate();
            out.write(rows > 0
                ? "{\"message\":\"Commission created successfully\"}"
                : "{\"error\":\"Failed to create commission\"}");
        }
    }

    // ====== UPDATE ======
    private void updateCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String rateStr = req.getParameter("discount_rate");
        String desc = req.getParameter("description");

        if (idStr == null || rateStr == null) {
            out.write("{\"error\":\"ID and discount rate are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal rate = new BigDecimal(rateStr);
        boolean active = req.getParameter("active") == null || Boolean.parseBoolean(req.getParameter("active"));

        String sql = "UPDATE store_discounts SET discount_rate = ?, active = ?, description = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, rate);
            pstmt.setBoolean(2, active);
            pstmt.setString(3, desc != null ? desc.trim() : null);
            pstmt.setInt(4, id);

            int rows = pstmt.executeUpdate();
            out.write(rows > 0
                ? "{\"message\":\"Commission updated successfully\"}"
                : "{\"error\":\"Commission not found\"}");
        }
    }

    // ====== DELETE ======
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
            out.write(rows > 0
                ? "{\"message\":\"Commission deleted successfully\"}"
                : "{\"error\":\"Commission not found\"}");
        }
    }

    // ====== ESCAPE ======
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
