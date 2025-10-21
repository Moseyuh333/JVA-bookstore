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
        // Check authentication
        if (!isAuthenticated(req, resp)) {
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listCategories(req, out);
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

        StringBuilder sql = new StringBuilder(
            "SELECT c.id, c.name, COALESCE(b.book_count, 0) as product_count, c.created_at " +
            "FROM categories c " +
            "LEFT JOIN (SELECT category, COUNT(*) as book_count FROM books GROUP BY category) b ON c.name = b.category " +
            "WHERE 1=1"
        );

        // Add search conditions
        if (search != null && !search.trim().isEmpty()) {
            if ("name".equals(searchType)) {
                sql.append(" AND name ILIKE ?");
            } else if ("description".equals(searchType)) {
                sql.append(" AND description ILIKE ?");
            } else {
                // "all" search
                sql.append(" AND (name ILIKE ? OR description ILIKE ?)");
            }
        }

        sql.append(" ORDER BY name");

        StringBuilder json = new StringBuilder();
        json.append("{\"categories\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set search parameters
            int paramIndex = 1;
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                if ("name".equals(searchType) || "description".equals(searchType)) {
                    pstmt.setString(paramIndex++, pattern);
                } else {
                    // "all" search
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
                        .append("\"product_count\":").append(rs.getInt("product_count")).append(",")
                        .append("\"created_at\":\"").append(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "").append("\"")
                        .append("}");
                }
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
        String sql = "UPDATE categories SET name = ?, total_product = ?, created_at = CURRENT_TIMESTAMP WHERE id = ?";

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

    private boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"Unauthorized\"}");
            return false;
        }
        return true;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
