package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "BookDetailServlet", urlPatterns = { "/books/detail" })
public class BookDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String id = req.getParameter("id");
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, author, price, description, cover_image, category, rating_avg, rating_count, stock "
                            +
                            "FROM books WHERE id = ?");

            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                req.setAttribute("bookId", rs.getInt("id"));
                req.setAttribute("bookTitle", rs.getString("title"));
                req.setAttribute("bookAuthor", rs.getString("author"));
                req.setAttribute("bookPrice", rs.getBigDecimal("price"));
                req.setAttribute("bookDescription", rs.getString("description"));
                req.setAttribute("bookImage", rs.getString("cover_image"));
                req.setAttribute("bookCategory", rs.getString("category"));
                req.setAttribute("bookRating", rs.getDouble("rating_avg"));
                req.setAttribute("bookRatingCount", rs.getInt("rating_count"));
                req.setAttribute("bookStock", rs.getInt("stock"));

            } else {
                req.setAttribute("error", "Book not found!");
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }

        RequestDispatcher rd = req.getRequestDispatcher("/book-detail.jsp");
        rd.forward(req, resp);
    }
}