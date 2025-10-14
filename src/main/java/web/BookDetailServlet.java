package web;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/book")
public class BookDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        if (id == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Lay thong tin sach
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE id = ?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                response.sendRedirect("index.jsp");
                return;
            }
            Map<String, Object> book = new HashMap<>();
            book.put("id", rs.getInt("id"));
            book.put("title", rs.getString("title"));
            book.put("author", rs.getString("author"));
            book.put("price", rs.getDouble("price"));
            book.put("stock", rs.getInt("stock"));
            book.put("description", rs.getString("description"));
            book.put("coverImage", rs.getString("cover_image"));
            book.put("category", rs.getString("category"));
            request.setAttribute("book", book);

            // Goi y sach lien quan theo category
            PreparedStatement psRel = conn.prepareStatement(
                "SELECT id, title, author, price, cover_image FROM books WHERE category = ? AND id <> ? LIMIT 8");
            psRel.setString(1, rs.getString("category"));
            psRel.setInt(2, rs.getInt("id"));
            ResultSet rsRel = psRel.executeQuery();
            List<Map<String, Object>> relatedBooks = new ArrayList<>();
            while (rsRel.next()) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", rsRel.getInt("id"));
                b.put("title", rsRel.getString("title"));
                b.put("author", rsRel.getString("author"));
                b.put("price", rsRel.getDouble("price"));
                b.put("coverImage", rsRel.getString("cover_image"));
                relatedBooks.add(b);
            }
            request.setAttribute("relatedBooks", relatedBooks);

            // Danh gia
            PreparedStatement psRv = conn.prepareStatement(
                "SELECT * FROM reviews WHERE book_id = ? ORDER BY created_at DESC");
            psRv.setInt(1, rs.getInt("id"));
            ResultSet rsRv = psRv.executeQuery();
            List<Map<String, Object>> reviews = new ArrayList<>();
            double sum = 0;
            int count = 0;
            while (rsRv.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("authorName", rsRv.getString("author_name"));
                r.put("rating", rsRv.getInt("rating"));
                r.put("comment", rsRv.getString("comment"));
                r.put("createdAt", rsRv.getTimestamp("created_at"));
                reviews.add(r);
                sum += rsRv.getInt("rating");
                count++;
            }
            request.setAttribute("reviews", reviews);
            request.setAttribute("avgRating", count == 0 ? 0 : Math.round((sum / count) * 10.0) / 10.0);
            request.setAttribute("reviewCount", count);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/book-detail.jsp");
        rd.forward(request, response);
    }
}
