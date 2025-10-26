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

@WebServlet(name = "AdminShippersServlet", urlPatterns = { "/api/admin/shippers" })
public class AdminShippersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listShippers(req, out);
            } else if ("get".equals(action)) {
                getShipper(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            e.printStackTrace(); // In log cho Heroku để xem lỗi SQL thật
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" 
                + e.getMessage()
                    .replace("\\", "\\\\")   // escape dấu backslash
                    .replace("\"", "\\\"")    // escape dấu nháy kép
                    .replace("\n", "\\n")     // escape xuống dòng
                    .replace("\r", "\\r")     // escape carriage return
                + "\"}");

        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                createShipper(req, out);
            } else if ("update".equals(action)) {
                updateShipper(req, out);
            } else if ("delete".equals(action)) {
                deleteShipper(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            e.printStackTrace(); // In log cho Heroku để xem lỗi SQL thật
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" 
                + e.getMessage()
                    .replace("\\", "\\\\")   // escape dấu backslash
                    .replace("\"", "\\\"")    // escape dấu nháy kép
                    .replace("\n", "\\n")     // escape xuống dòng
                    .replace("\r", "\\r")     // escape carriage return
                + "\"}");
        } finally {
            out.flush();
        }
    }

    private void listShippers(HttpServletRequest req, PrintWriter out) throws SQLException {
        String search = req.getParameter("search");
        String searchType = req.getParameter("searchType");

        StringBuilder sql = new StringBuilder(
            "SELECT id, name, phone, email, base_fee, service_area, estimated_time, status, created_at, updated_at "
            + "FROM shippers WHERE 1=1"
        );

        // Add search conditions
        if (search != null && !search.trim().isEmpty()) {
            if ("name".equals(searchType)) {
                sql.append(" AND name ILIKE ?");
            } else if ("phone".equals(searchType)) {
                sql.append(" AND phone ILIKE ?");
            } else if ("email".equals(searchType)) {
                sql.append(" AND email ILIKE ?");
            } else if ("service_area".equals(searchType)) {
                sql.append(" AND service_area ILIKE ?");
            } else {
                // Default "all"
                sql.append(" AND (name ILIKE ? OR phone ILIKE ? OR email ILIKE ? OR service_area ILIKE ?)");
            }
        }

        sql.append(" ORDER BY name");

        StringBuilder json = new StringBuilder();
        json.append("{\"shippers\":[");

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set search parameters
            int paramIndex = 1;
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                if ("name".equals(searchType) || "phone".equals(searchType) || "email".equals(searchType) || "service_area".equals(searchType)) {
                    pstmt.setString(paramIndex++, pattern);
                } else {
                    // "all" search
                    pstmt.setString(paramIndex++, pattern);
                    pstmt.setString(paramIndex++, pattern);
                    pstmt.setString(paramIndex++, pattern);
                    pstmt.setString(paramIndex++, pattern);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    first = false;

                    json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                            .append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",")
                            .append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",")
                            .append("\"base_fee\":").append(rs.getBigDecimal("base_fee")).append(",")
                            .append("\"service_area\":\"").append(escapeJson(rs.getString("service_area"))).append("\",")
                            .append("\"estimated_time\":\"").append(escapeJson(rs.getString("estimated_time")))
                            .append("\",")
                            .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                            .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                            .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                            .append("}");
                }
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    private void getShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "SELECT id, name, phone, email, base_fee, service_area, estimated_time, status, created_at, updated_at "
                +
                "FROM shippers WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = "{"
                            + "\"id\":" + rs.getInt("id") + ","
                            + "\"name\":\"" + escapeJson(rs.getString("name")) + "\","
                            + "\"phone\":\"" + escapeJson(rs.getString("phone")) + "\","
                            + "\"email\":\"" + escapeJson(rs.getString("email")) + "\","
                            + "\"base_fee\":" + rs.getBigDecimal("base_fee") + ","
                            + "\"service_area\":\"" + escapeJson(rs.getString("service_area")) + "\","
                            + "\"estimated_time\":\"" + escapeJson(rs.getString("estimated_time")) + "\","
                            + "\"status\":\"" + escapeJson(rs.getString("status")) + "\","
                            + "\"created_at\":\"" + rs.getTimestamp("created_at") + "\","
                            + "\"updated_at\":\"" + rs.getTimestamp("updated_at") + "\""
                            + "}";
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Shipper not found\"}");
                }
            }
        }
    }

    private void createShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String email = req.getParameter("email");
        String baseFeeStr = req.getParameter("base_fee");
        String serviceArea = req.getParameter("service_area");
        String estimatedTime = req.getParameter("estimated_time");
        String status = req.getParameter("status");

        if (name == null || name.trim().isEmpty() || baseFeeStr == null) {
            out.write("{\"error\":\"Name and base fee are required\"}");
            return;
        }

        BigDecimal baseFee = new BigDecimal(baseFeeStr);

        String sql = "INSERT INTO shippers (name, phone, email, base_fee, service_area, estimated_time, status, updated_at) "
           + "VALUES (?, ?, ?, ?, ?, ?, ?::shipper_status, CURRENT_TIMESTAMP) RETURNING id, created_at";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, phone != null ? phone.trim() : null);
            pstmt.setString(3, email != null ? email.trim() : null);
            pstmt.setBigDecimal(4, baseFee);
            pstmt.setString(5, serviceArea != null ? serviceArea.trim() : null);
            pstmt.setString(6, estimatedTime != null ? estimatedTime.trim() : null);
            pstmt.setString(7, status != null ? status : "active");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt("id");
                    java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                    out.write("{\"id\":" + newId + ", \"created_at\":\"" + (createdAt != null ? createdAt.toString() : "") + "\", \"message\":\"Shipper created successfully\"}");
                } else {
                    out.write("{\"error\":\"Failed to create shipper\"}");
                }
            }
        }
    }

    private void updateShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String email = req.getParameter("email");
        String baseFeeStr = req.getParameter("base_fee");
        String serviceArea = req.getParameter("service_area");
        String estimatedTime = req.getParameter("estimated_time");
        String status = req.getParameter("status");

        if (idStr == null || name == null || name.trim().isEmpty() || baseFeeStr == null) {
            out.write("{\"error\":\"ID, name and base fee are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal baseFee = new BigDecimal(baseFeeStr);

        String sql = "UPDATE shippers SET name = ?, phone = ?, email = ?, base_fee = ?, " +
                "service_area = ?, estimated_time = ?, status = ?::shipper_status, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, phone != null ? phone.trim() : null);
            pstmt.setString(3, email != null ? email.trim() : null);
            pstmt.setBigDecimal(4, baseFee);
            pstmt.setString(5, serviceArea != null ? serviceArea.trim() : null);
            pstmt.setString(6, estimatedTime != null ? estimatedTime.trim() : null);
            pstmt.setString(7, status != null ? status : "active");
            pstmt.setInt(8, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Shipper updated successfully\"}");
            } else {
                out.write("{\"error\":\"Shipper not found\"}");
            }
        }
    }

    private void deleteShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM shippers WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Shipper deleted successfully\"}");
            } else {
                out.write("{\"error\":\"Shipper not found\"}");
            }
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
