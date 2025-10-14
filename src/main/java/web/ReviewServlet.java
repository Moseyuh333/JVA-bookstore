package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/books/review"})
public class ReviewServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String bookId = req.getParameter("bookId");
        String name = req.getParameter("authorName");
        String rating = req.getParameter("rating");
        String comment = req.getParameter("comment");

        if (bookId == null || rating == null) {
            resp.sendRedirect("index.jsp");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reviews(book_id, author_name, rating, comment, created_at) VALUES (?,?,?,?,NOW())");
            ps.setInt(1, Integer.parseInt(bookId));
            ps.setString(2, name == null || name.isBlank() ? "Ẩn danh" : name);
            ps.setInt(3, Integer.parseInt(rating));
            ps.setString(4, comment);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new ServletException(e);
        }

        resp.sendRedirect("book?id=" + bookId + "#reviews");
    }
}
