package web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import com.google.gson.Gson;
import dao.BookDAO;
import models.Book;

@WebServlet(name = "BooksApiServlet", urlPatterns = {"/api/books/*"})
public class BooksApiServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            int limit = Integer.parseInt(req.getParameter("limit") != null ? req.getParameter("limit") : "12");
            int offset = Integer.parseInt(req.getParameter("offset") != null ? req.getParameter("offset") : "0");
            
            List<Book> books;
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Default: newest books
                books = BookDAO.getNewestBooks(limit, offset);
            } else if (pathInfo.equals("/newest")) {
                books = BookDAO.getNewestBooks(limit, offset);
            } else if (pathInfo.equals("/best-selling")) {
                books = BookDAO.getBestSellingBooks(limit, offset);
            } else if (pathInfo.equals("/top-rated")) {
                books = BookDAO.getTopRatedBooks(limit, offset);
            } else if (pathInfo.equals("/favorites")) {
                books = BookDAO.getFavoriteBooks(limit, offset);
            } else if (pathInfo.startsWith("/category/")) {
                String category = pathInfo.substring("/category/".length());
                String sortBy = req.getParameter("sortBy") != null ? req.getParameter("sortBy") : "created_at";
                books = BookDAO.getByCategory(category, sortBy, limit, offset);
            } else if (pathInfo.startsWith("/search/")) {
                String keyword = pathInfo.substring("/search/".length());
                books = BookDAO.searchBooks(keyword, limit, offset);
            } else if (pathInfo.startsWith("/")) {
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Book book = BookDAO.getById(id);
                    if (book != null) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.write(gson.toJson(book));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.write("{\"error\":\"Book not found\"}");
                    }
                    out.flush();
                    return;
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Invalid book ID\"}");
                    out.flush();
                    return;
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid endpoint\"}");
                out.flush();
                return;
            }
            
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(books));
            out.flush();
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
}
