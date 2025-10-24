package web.admin;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AdminCategoriesServlet", urlPatterns = {"/api/admin/categories"})
public class AdminCategoriesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listCategories(req, out);
            } else if ("get".equals(action)) {
                getCategory(req, out);
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
        req.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                createCategory(req, out);
            } else if ("update".equals(action)) {
                updateCategory(req, out);
            } else if ("delete".equals(action)) {
                deleteCategory(req, out);
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

    private void listCategories(HttpServletRequest req, PrintWriter out) throws SQLException {
        String search = req.getParameter("search");
        String searchType = req.getParameter("searchType");

        StringBuilder sql = new StringBuilder("SELECT id, name, total_products as product_count, created_at FROM categories");

        boolean hasSearch = (search != null && !search.trim().isEmpty());

        if (hasSearch) {
            search = "%" + search.trim().toLowerCase() + "%";
            if ("id".equalsIgnoreCase(searchType)) {
                sql.append(" WHERE CAST(id AS TEXT) LIKE ?");
            } else if ("name".equalsIgnoreCase(searchType)) {
                sql.append(" WHERE LOWER(name) LIKE ?");
            } else {
                sql.append(" WHERE LOWER(name) LIKE ? OR CAST(id AS TEXT) LIKE ?");
            }
        }
        sql.append(" ORDER BY id ASC");

        StringBuilder json = new StringBuilder();
        json.append("{\"categories\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            if (search != null && !search.trim().isEmpty()) {
                if ("id".equalsIgnoreCase(searchType) || "name".equalsIgnoreCase(searchType)) {
                    pstmt.setString(1, search);
                } else {
                    pstmt.setString(1, search);
                    pstmt.setString(2, search);
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
                        .append("\"product_count\":").append(rs.getInt("product_count")).append(",")
                        .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"")
                        .append("}");
                }
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    private void getCategory(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "SELECT id, name, total_products as product_count, created_at FROM categories WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    StringBuilder json = new StringBuilder();
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                        .append("\"product_count\":").append(rs.getInt("product_count")).append(",")
                        .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"")
                        .append("}");
                    out.write(json.toString());
                } else {
                    out.write("{\"error\":\"Category not found\"}");
                }
            }
        }
    }

    private void createCategory(HttpServletRequest req, PrintWriter out) throws SQLException {
        String name = req.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            out.write("{\"error\":\"Name is required\"}");
            return;
        }
        String sql = "INSERT INTO categories (name) VALUES (?) RETURNING id, total_products, created_at";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    int total = rs.getInt("total_products");
                    java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                    StringBuilder json = new StringBuilder();
                    json.append("{")
                        .append("\"message\":\"Category created successfully\"")
                        .append(",\"id\":").append(id)
                        .append(",\"total_products\":").append(total)
                        .append(",\"created_at\":\"").append(createdAt).append("\"")
                        .append("}");
                    out.write(json.toString());
                } else {
                    out.write("{\"error\":\"Failed to create category\"}");
                }
            }
        }
    }

    private void updateCategory(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        if (idStr == null || name == null || name.trim().isEmpty()) {
            out.write("{\"error\":\"ID and name are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "UPDATE categories SET name = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setInt(2, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Category updated successfully\"}");
            } else {
                out.write("{\"error\":\"Category not found\"}");
            }
        }
    }

    private void deleteCategory(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Category deleted successfully\"}");
            } else {
                out.write("{\"error\":\"Category not found\"}");
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
