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

            int limit = parseInt(req.getParameter("limit"), 12);
            int offsetParam = req.getParameter("offset") != null ? parseInt(req.getParameter("offset"), 0) : -1;
            int page = parseInt(req.getParameter("page"), 1);
            if (page < 1) {
                page = 1;
            }

            if (offsetParam >= 0) {
                page = (offsetParam / limit) + 1;
            }

            int offset = offsetParam >= 0 ? offsetParam : (page - 1) * limit;
            
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
            } else if (pathInfo.equals("/categories")) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(BookDAO.getAllCategories()));
                out.flush();
                return;
            } else if (pathInfo.startsWith("/category/")) {
                String category = pathInfo.substring("/category/".length());
                String sortBy = req.getParameter("sortBy") != null ? req.getParameter("sortBy") : "created_at";
                Double minPrice = parseDouble(req.getParameter("minPrice"));
                Double maxPrice = parseDouble(req.getParameter("maxPrice"));
                Double minRating = parseDouble(req.getParameter("minRating"));
                Boolean inStockOnly = req.getParameter("inStock") != null ? Boolean.parseBoolean(req.getParameter("inStock")) : null;

                books = BookDAO.getByCategory(category, sortBy, minPrice, maxPrice, minRating, inStockOnly, limit, offset);

                boolean includeMeta = Boolean.parseBoolean(req.getParameter("meta"));
                if (includeMeta) {
                    int totalItems = BookDAO.getCategoryCount(category, minPrice, maxPrice, minRating, inStockOnly);
                    int totalPages = limit > 0 ? (int) Math.ceil((double) totalItems / limit) : 1;

                    java.util.Map<String, Object> payload = new java.util.HashMap<>();
                    payload.put("items", books);

                    java.util.Map<String, Object> pagination = new java.util.HashMap<>();
                    pagination.put("page", page);
                    pagination.put("pageSize", limit);
                    pagination.put("totalItems", totalItems);
                    pagination.put("totalPages", totalPages);
                    pagination.put("hasNext", page < totalPages);
                    pagination.put("hasPrevious", page > 1);
                    payload.put("pagination", pagination);

                    java.util.Map<String, Object> appliedFilters = new java.util.HashMap<>();
                    appliedFilters.put("sortBy", sortBy);
                    if (minPrice != null) {
                        appliedFilters.put("minPrice", minPrice);
                    }
                    if (maxPrice != null) {
                        appliedFilters.put("maxPrice", maxPrice);
                    }
                    if (minRating != null) {
                        appliedFilters.put("minRating", minRating);
                    }
                    if (inStockOnly != null) {
                        appliedFilters.put("inStock", inStockOnly);
                    }
                    payload.put("filters", appliedFilters);

                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(payload));
                    out.flush();
                    return;
                }
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

    private int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
