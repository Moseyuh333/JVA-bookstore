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

@WebServlet(name = "AdminShippersServlet", urlPatterns = {"/api/admin/shippers"})
public class AdminShippersServlet extends HttpServlet {

    // =========================
    // ⚙️ COMMON UTF-8 SETUP
    // =========================
    private void setupEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupEncoding(req, resp);
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
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupEncoding(req, resp);
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            switch (action) {
                case "create" -> createShipper(req, out);
                case "update" -> updateShipper(req, out);
                case "delete" -> deleteShipper(req, out);
                default -> {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Invalid action\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            out.flush();
        }
    }

    // =========================
    // 📋 LIST ALL SHIPPERS
    // =========================
    private void listShippers(PrintWriter out) throws SQLException {
        String sql = """
                SELECT id, name, phone, email, base_fee, service_area,
                       estimated_time, status, created_at, updated_at
                FROM shippers ORDER BY name
                """;

        StringBuilder json = new StringBuilder("{\"shippers\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                    .append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",")
                    .append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",")
                    .append("\"base_fee\":").append(rs.getBigDecimal("base_fee")).append(",")
                    .append("\"service_area\":\"").append(escapeJson(rs.getString("service_area"))).append("\",")
                    .append("\"estimated_time\":\"").append(escapeJson(rs.getString("estimated_time"))).append("\",")
                    .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                    .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                    .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                    .append("}");
            }
        }
        json.append("]}");
        out.write(json.toString());
    }

    // =========================
    // 🧾 GET ONE SHIPPER
    // =========================
    private void getShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = """
                SELECT id, name, phone, email, base_fee, service_area,
                       estimated_time, status, created_at, updated_at
                FROM shippers WHERE id = ?
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = String.format("""
                            {
                              "id": %d,
                              "name": "%s",
                              "phone": "%s",
                              "email": "%s",
                              "base_fee": %s,
                              "service_area": "%s",
                              "estimated_time": "%s",
                              "status": "%s",
                              "created_at": "%s",
                              "updated_at": "%s"
                            }
                            """,
                            rs.getInt("id"),
                            escapeJson(rs.getString("name")),
                            escapeJson(rs.getString("phone")),
                            escapeJson(rs.getString("email")),
                            rs.getBigDecimal("base_fee"),
                            escapeJson(rs.getString("service_area")),
                            escapeJson(rs.getString("estimated_time")),
                            escapeJson(rs.getString("status")),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("updated_at")
                    );
                    out.write(json);
                } else {
                    out.write("{\"error\":\"Shipper not found\"}");
                }
            }
        }
    }

    // =========================
    // ➕ CREATE SHIPPER
    // =========================
    private void createShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String name = req.getParameter("name");
        String baseFeeStr = req.getParameter("base_fee");

        if (name == null || name.trim().isEmpty() || baseFeeStr == null) {
            out.write("{\"error\":\"Name and base fee are required\"}");
            return;
        }

        BigDecimal baseFee = new BigDecimal(baseFeeStr);
        String sql = """
                INSERT INTO shippers (name, phone, email, base_fee, service_area, estimated_time, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, req.getParameter("phone"));
            pstmt.setString(3, req.getParameter("email"));
            pstmt.setBigDecimal(4, baseFee);
            pstmt.setString(5, req.getParameter("service_area"));
            pstmt.setString(6, req.getParameter("estimated_time"));
            pstmt.setString(7, req.getParameter("status") != null ? req.getParameter("status") : "active");

            int rows = pstmt.executeUpdate();
            out.write(rows > 0 ? "{\"message\":\"Shipper created successfully\"}"
                    : "{\"error\":\"Failed to create shipper\"}");
        }
    }

    // =========================
    // ✏️ UPDATE SHIPPER
    // =========================
    private void updateShipper(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        BigDecimal baseFee = new BigDecimal(req.getParameter("base_fee"));

        String sql = """
                UPDATE shippers SET name=?, phone=?, email=?, base_fee=?, service_area=?,
                                    estimated_time=?, status=?, updated_at=CURRENT_TIMESTAMP
                WHERE id=?
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, req.getParameter("name"));
            pstmt.setString(2, req.getParameter("phone"));
            pstmt.setString(3, req.getParameter("email"));
            pstmt.setBigDecimal(4, baseFee);
            pstmt.setString(5, req.getParameter("service_area"));
            pstmt.setString(6, req.getParameter("estimated_time"));
            pstmt.setString(7, req.getParameter("status"));
            pstmt.setInt(8, id);

            int rows = pstmt.executeUpdate();
            out.write(rows > 0 ? "{\"message\":\"Shipper updated successfully\"}"
                    : "{\"error\":\"Shipper not found\"}");
        }
    }

    // =========================
    // ❌ DELETE SHIPPER
    // =========================
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
            out.write(rows > 0 ? "{\"message\":\"Shipper deleted successfully\"}"
                    : "{\"error\":\"Shipper not found\"}");
        }
    }

    // =========================
    // 🔒 JSON ESCAPE
    // =========================
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
