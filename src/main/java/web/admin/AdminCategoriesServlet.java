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
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listCategories(out);
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

    private void listCategories(PrintWriter out) throws SQLException {
        String sql = "SELECT id, name, slug, description, created_at, updated_at FROM categories ORDER BY name";

        StringBuilder json = new StringBuilder();
        json.append("{\"categories\":[");

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
                    .append("\"slug\":\"").append(escapeJson(rs.getString("slug"))).append("\",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\",")
                    .append("\"updated_at\":\"").append(rs.getTimestamp("updated_at")).append("\"")
                    .append("}");
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    private void createCategory(HttpServletRequest req, PrintWriter out) throws SQLException {
        String name = req.getParameter("name");
        String slug = req.getParameter("slug");
        String description = req.getParameter("description");

        if (name == null || name.trim().isEmpty() || slug == null || slug.trim().isEmpty()) {
            out.write("{\"error\":\"Name and slug are required\"}");
            return;
        }

        String sql = "INSERT INTO categories (name, slug, description) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, slug.trim());
            pstmt.setString(3, description != null ? description.trim() : null);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"Category created successfully\"}");
            } else {
                out.write("{\"error\":\"Failed to create category\"}");
            }
        }
    }

    private void updateCategory(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String slug = req.getParameter("slug");
        String description = req.getParameter("description");

        if (idStr == null || name == null || name.trim().isEmpty() || slug == null || slug.trim().isEmpty()) {
            out.write("{\"error\":\"ID, name and slug are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "UPDATE categories SET name = ?, slug = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, slug.trim());
            pstmt.setString(3, description != null ? description.trim() : null);
            pstmt.setInt(4, id);

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
