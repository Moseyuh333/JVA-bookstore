package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name = "ShopServlet", urlPatterns = {"/shop"})
public class ShopServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Map<String, Object>> books = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, title, price, category, cover_image FROM books ORDER BY id ASC LIMIT 100"
            );
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", rs.getInt("id"));
                b.put("title", rs.getString("title"));
                b.put("author", rs.getString("category")); 
                b.put("price", rs.getDouble("price"));
                b.put("coverImage", rs.getString("cover_image"));
                books.add(b);
            }

            System.out.println("Total books loaded: " + books.size()); 
            req.setAttribute("books", books);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            e.printStackTrace();
        }

        RequestDispatcher rd = req.getRequestDispatcher("/shop.jsp");
        rd.forward(req, resp);
    }
}
