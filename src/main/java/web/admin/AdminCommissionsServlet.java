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

@WebServlet(name = "AdminCommissionsServlet", urlPatterns = { "/api/admin/commissions" })
public class AdminCommissionsServlet extends HttpServlet {

    // ========= COMMON UTF-8 =========
    private void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
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
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");
        try {
            switch (action) {
                case "create":
                    createCommission(req, out);
                    break;
                case "update":
                    updateCommission(req, out);
                    break;
                case "delete":
                    deleteCommission(req, out);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Invalid action\"}");
                    break;
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }

    // ========= GET LIST =========
    private void listCommissions(PrintWriter out) throws SQLException {
        String sql = "SELECT sd.id, sd.shop_id, s.name AS shop_name, " +
                "sd.discount_rate AS rate, sd.start_date AS since, " +
                "sd.end_date, sd.active, sd.description, sd.created_at " +
                "FROM store_discounts sd " +
                "LEFT JOIN shops s ON sd.shop_id = s.id " +
                "ORDER BY sd.created_at DESC";

        StringBuilder json = new StringBuilder("{\"commissions\":[");
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first)
                    json.append(",");
                first = false;

                json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"shop_name\":\"").append(escapeJson(rs.getString("shop_name"))).append("\",")
                        .append("\"rate\":").append(rs.getBigDecimal("rate") != null ? rs.getBigDecimal("rate") : "0")
                        .append(",")
                        .append("\"since\":\"").append(rs.getTimestamp("since") != null ? rs.getTimestamp("since") : "")
                        .append("\",")
                        .append("\"end_date\":\"")
                        .append(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date") : "").append("\",")
                        .append("\"active\":").append(rs.getBoolean("active")).append(",")
                        .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                        .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"")
                        .append("}");
            }
        }
        json.append("]}");
        out.write(json.toString());
    }

    // ========= GET ONE =========
    private void getCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "SELECT sd.id, sd.shop_id, s.name AS shop_name, " +
                "sd.discount_rate AS rate, sd.start_date AS since, " +
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
                            + "\"shop_name\":\"" + escapeJson(rs.getString("shop_name")) + "\","
                            + "\"rate\":" + rs.getBigDecimal("rate") + ","
                            + "\"since\":\"" + rs.getTimestamp("since") + "\","
                            + "\"description\":\"" + escapeJson(rs.getString("description")) + "\""
                            + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Commission not found\"}");
                }
            }
        }
    }

    // ========= CREATE =========
    private void createCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        int shopId = Integer.parseInt(req.getParameter("shop_id"));
        BigDecimal discountRate = new BigDecimal(req.getParameter("discount_rate"));
        boolean active = req.getParameter("active") == null || Boolean.parseBoolean(req.getParameter("active"));
        String description = req.getParameter("description");

        String sql = "INSERT INTO store_discounts (shop_id, discount_rate, start_date, active, description)" +
                "VALUES (?, ?, NOW(), ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, shopId);
            pstmt.setBigDecimal(2, discountRate);
            pstmt.setBoolean(3, active);
            pstmt.setString(4, description != null ? description.trim() : null);

            int rows = pstmt.executeUpdate();
            out.write(rows > 0
                    ? "{\"message\":\"Commission created successfully\"}"
                    : "{\"error\":\"Failed to create commission\"}");
        }
    }

    // ========= UPDATE =========
    private void updateCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        int id = Integer.parseInt(req.getParameter("id"));
        BigDecimal discountRate = new BigDecimal(req.getParameter("discount_rate"));
        boolean active = req.getParameter("active") == null || Boolean.parseBoolean(req.getParameter("active"));
        String description = req.getParameter("description");

        String sql = "UPDATE store_discounts " +
                "SET discount_rate = ?, active = ?, description = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, discountRate);
            pstmt.setBoolean(2, active);
            pstmt.setString(3, description);
            pstmt.setInt(4, id);

            int rows = pstmt.executeUpdate();
            out.write(rows > 0
                    ? "{\"message\":\"Commission updated successfully\"}"
                    : "{\"error\":\"Commission not found\"}");
        }
    }

    // ========= DELETE =========
    private void deleteCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        int id = Integer.parseInt(req.getParameter("id"));
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

    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
