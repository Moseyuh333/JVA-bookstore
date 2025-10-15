package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet(name = "CollectionsServlet", urlPatterns = {"/collections"})
public class CollectionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, List<Map<String, Object>>> collections = new LinkedHashMap<>();

        String[] categories = {"Sách mới", "Sách bán chạy", "Tiểu thuyết", "Phi tiểu thuyết", "Thẻ quà tặng"};

        try (Connection conn = DBUtil.getConnection()) {
            for (String category : categories) {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, author, price, cover_image, rating_avg FROM books WHERE category = ? ORDER BY id ASC LIMIT 8"
                );
                ps.setString(1, category);
                ResultSet rs = ps.executeQuery();

                List<Map<String, Object>> books = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> b = new HashMap<>();
                    b.put("id", rs.getInt("id"));
                    b.put("title", rs.getString("title"));
                    b.put("author", rs.getString("author"));
                    b.put("price", rs.getBigDecimal("price"));
                    b.put("coverImage", rs.getString("cover_image"));
                    b.put("rating", rs.getDouble("rating_avg"));
                    books.add(b);
                }
                if (!books.isEmpty()) {
                    collections.put(category, books);
                }
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }

        req.setAttribute("collections", collections);
        RequestDispatcher rd = req.getRequestDispatcher("/collections.jsp");
        rd.forward(req, resp);
    }
}
