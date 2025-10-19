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
                listShippers(out);
            } else if ("get".equals(action)) {
                getShipper(req, out);
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
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private void listShippers(PrintWriter out) throws SQLException {
        String sql = "SELECT id, name, phone, email, base_fee, service_area, estimated_time, status, created_at, updated_at "
                +
                "FROM shippers ORDER BY name";

        StringBuilder json = new StringBuilder();
        json.append("{\"shippers\":[");

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

        String sql = "INSERT INTO shippers (name, phone, email, base_fee, service_area, estimated_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, phone != null ? phone.trim() : null);
            pstmt.setString(3, email != null ? email.trim() : null);
            pstmt.setBigDecimal(4, baseFee);
            pstmt.setString(5, serviceArea != null ? serviceArea.trim() : null);
            pstmt.setString(6, estimatedTime != null ? estimatedTime.trim() : null);
            pstmt.setString(7, status != null ? status : "active");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Shipper created successfully\"}");
            } else {
                out.write("{\"error\":\"Failed to create shipper\"}");
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
                "service_area = ?, estimated_time = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
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
