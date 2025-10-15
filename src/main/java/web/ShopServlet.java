package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name = "ShopServlet", urlPatterns = { "/shop" })
public class ShopServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Map<String, Object>> books = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, title, author, price, cover_image, category FROM books LIMIT 10";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", rs.getInt("id"));
                b.put("title", rs.getString("title"));
                b.put("author", rs.getString("author"));
                b.put("price", rs.getBigDecimal("price"));
                b.put("coverImage", rs.getString("cover_image"));
                b.put("category", rs.getString("category"));
                books.add(b);

                // In ra console để debug
                System.out.println("Loaded book: " + rs.getString("title"));
            }

            if (books.isEmpty()) {
                System.out.println("Không có sách nào được load từ DB!");
            }

            req.setAttribute("books", books);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "SQL Error: " + e.getMessage());

        }

        RequestDispatcher rd = req.getRequestDispatcher("/shop.jsp");
        rd.forward(req, resp);
    }
}
