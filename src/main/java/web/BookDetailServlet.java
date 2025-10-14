package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "BookDetailServlet", urlPatterns = {"/books/detail"})
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
                "SELECT id, title, author, price, description, cover_image, category, rating_int, rating_text, in_stock, stock_text, book_url " +
                "FROM books WHERE id = ?"
            );
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                req.setAttribute("bookId", rs.getInt("id"));
                req.setAttribute("bookTitle", rs.getString("title"));
                req.setAttribute("bookAuthor", rs.getString("author"));       // có thể null vì CSV không có author; OK
                req.setAttribute("bookPrice", rs.getBigDecimal("price"));
                req.setAttribute("bookDescription", rs.getString("description"));
                req.setAttribute("bookImage", rs.getString("cover_image"));   // có thể null
                req.setAttribute("bookCategory", rs.getString("category"));
                req.setAttribute("bookRatingInt", rs.getObject("rating_int"));
                req.setAttribute("bookRatingText", rs.getString("rating_text"));
                req.setAttribute("bookInStock", rs.getBoolean("in_stock"));
                req.setAttribute("bookStockText", rs.getString("stock_text"));
                req.setAttribute("bookUrl", rs.getString("book_url"));
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