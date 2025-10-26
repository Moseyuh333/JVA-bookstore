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
                listCommissions(req, out);
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
    private void listCommissions(HttpServletRequest req, PrintWriter out) throws SQLException {
        String search = req.getParameter("search");
        String searchType = req.getParameter("searchType");
        String statusFilter = req.getParameter("status");

        StringBuilder sql = new StringBuilder(
            "SELECT id, name, type, min_revenue, max_revenue, rate, status, created_at, updated_at " +
            "FROM commissions WHERE 1=1"
        );

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"all".equals(statusFilter)) {
            sql.append(" AND status = ?");
        }

        if (search != null && !search.trim().isEmpty()) {
            if ("name".equals(searchType)) {
                sql.append(" AND name ILIKE ?");
            } else if ("type".equals(searchType)) {
                sql.append(" AND type ILIKE ?");
            } else if ("rate".equals(searchType)) {
                sql.append(" AND CAST(rate AS TEXT) ILIKE ?");
            } else {
                // Default "all"
                sql.append(" AND (name ILIKE ? OR type ILIKE ? OR CAST(rate AS TEXT) ILIKE ?)");
            }
        }

        sql.append(" ORDER BY id ASC");

        StringBuilder json = new StringBuilder();
        json.append("{\"commissions\":[");

        int totalCommissions = 0;
        int activeCommissions = 0;
        double sumRate = 0.0;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int param = 1;
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"all".equals(statusFilter)) {
                pstmt.setString(param++, statusFilter.trim());
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                if ("name".equals(searchType) || "type".equals(searchType) || "rate".equals(searchType)) {
                    pstmt.setString(param++, pattern);
                } else {
                    pstmt.setString(param++, pattern);
                    pstmt.setString(param++, pattern);
                    pstmt.setString(param++, pattern);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;

                    totalCommissions++;
                    if ("active".equals(rs.getString("status"))) {
                        activeCommissions++;
                    }
                    sumRate += rs.getBigDecimal("rate").doubleValue();

                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                        .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                        .append("\"min_revenue\":").append(rs.getBigDecimal("min_revenue") != null ? rs.getBigDecimal("min_revenue") : 0).append(",")
                        .append("\"max_revenue\":").append(rs.getBigDecimal("max_revenue") != null ? rs.getBigDecimal("max_revenue") : 0).append(",")
                        .append("\"rate\":").append(rs.getBigDecimal("rate")).append(",")
                        .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                        .append("\"created_at\":\"").append(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "").append("\",")
                        .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : "").append("\"")
                        .append("}");
                }
            }
        }

        double averageRate = totalCommissions > 0 ? sumRate / totalCommissions : 0;
        json.append("],\"total\":").append(totalCommissions).append(",\"active\":").append(activeCommissions).append(",\"average_rate\":").append(averageRate).append("}");
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
        String sql = "SELECT id, name, type, min_revenue, max_revenue, rate, status, created_at, updated_at FROM commissions WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = "{"
                        + "\"id\":" + rs.getInt("id") + ","
                        + "\"name\":\"" + escapeJson(rs.getString("name")) + "\","
                        + "\"type\":\"" + escapeJson(rs.getString("type")) + "\","
                        + "\"min_revenue\":" + rs.getBigDecimal("min_revenue") + ","
                        + "\"max_revenue\":" + rs.getBigDecimal("max_revenue") + ","
                        + "\"rate\":" + rs.getBigDecimal("rate") + ","
                        + "\"status\":\"" + escapeJson(rs.getString("status")) + "\","
                        + "\"created_at\":\"" + rs.getTimestamp("created_at") + "\","
                        + "\"updated_at\":\"" + rs.getTimestamp("updated_at") + "\""
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
        String name = req.getParameter("name");
        String type = req.getParameter("type");
        String minRevenueStr = req.getParameter("min_revenue");
        String maxRevenueStr = req.getParameter("max_revenue");
        String rateStr = req.getParameter("rate");
        String status = req.getParameter("status");

        if (name == null || name.trim().isEmpty() || rateStr == null) {
            out.write("{\"error\":\"Name and rate are required\"}");
            return;
        }

        BigDecimal minRevenue = (minRevenueStr != null && !minRevenueStr.trim().isEmpty()) ? new BigDecimal(minRevenueStr.trim()) : BigDecimal.ZERO;
        BigDecimal maxRevenue = (maxRevenueStr != null && !maxRevenueStr.trim().isEmpty()) ? new BigDecimal(maxRevenueStr.trim()) : null;
        BigDecimal rate = new BigDecimal(rateStr);
        if (status == null) status = "active";

        String sql = "INSERT INTO commissions (name, type, min_revenue, max_revenue, rate, status) VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            pstmt.setString(2, type != null ? type : "doanh số");
            pstmt.setBigDecimal(3, minRevenue);
            if (maxRevenue != null) {
                pstmt.setBigDecimal(4, maxRevenue);
            } else {
                pstmt.setNull(4, Types.NUMERIC);
            }
            pstmt.setBigDecimal(5, rate);
            pstmt.setString(6, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt("id");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    out.write("{\"id\":" + newId + ",\"created_at\":\"" + createdAt + "\",\"message\":\"Commission created successfully\"}");
                } else {
                    out.write("{\"error\":\"Failed to create commission\"}");
                }
            }
        }
    }

    // ====== UPDATE ======
    private void updateCommission(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String type = req.getParameter("type");
        String minRevenueStr = req.getParameter("min_revenue");
        String maxRevenueStr = req.getParameter("max_revenue");
        String rateStr = req.getParameter("rate");
        String status = req.getParameter("status");

        if (idStr == null || name == null || name.trim().isEmpty() || rateStr == null) {
            out.write("{\"error\":\"ID, name and rate are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal minRevenue = (minRevenueStr != null && !minRevenueStr.trim().isEmpty()) ? new BigDecimal(minRevenueStr.trim()) : BigDecimal.ZERO;
        BigDecimal maxRevenue = (maxRevenueStr != null && !maxRevenueStr.trim().isEmpty()) ? new BigDecimal(maxRevenueStr.trim()) : null;
        BigDecimal rate = new BigDecimal(rateStr);

        String sql = "UPDATE commissions SET name = ?, type = ?, min_revenue = ?, max_revenue = ?, rate = ?, status = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            pstmt.setString(2, type != null ? type : "doanh số");
            pstmt.setBigDecimal(3, minRevenue);
            if (maxRevenue != null) {
                pstmt.setBigDecimal(4, maxRevenue);
            } else {
                pstmt.setNull(4, Types.NUMERIC);
            }
            pstmt.setBigDecimal(5, rate);
            pstmt.setString(6, status != null ? status : "active");
            pstmt.setInt(7, id);

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
        String sql = "DELETE FROM commissions WHERE id = ?";

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
