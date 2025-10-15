package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet(name = "ShopServlet", urlPatterns = {"/shop"})
public class ShopServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try (Connection conn = DBUtil.getConnection()) {
            // Lấy toàn bộ sách
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, title, author, price, cover_image, rating_avg, category FROM books ORDER BY id ASC"
            );
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
                b.put("category", rs.getString("category"));
                books.add(b);
            }
            req.setAttribute("books", books);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }

        RequestDispatcher rd = req.getRequestDispatcher("/shop.jsp");
        rd.forward(req, resp);
    }
}
