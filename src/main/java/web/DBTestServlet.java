package web;

import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

@WebServlet(name = "DBTestServlet", urlPatterns = {"/api/dbtest"})
public class DBTestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            try {
                Connection conn = DBUtil.getConnection();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    out.write("{\"status\":\"success\",\"message\":\"Database connection successful\"}");
                } else {
                    out.write("{\"status\":\"error\",\"message\":\"Database connection failed\"}");
                }
            } catch (Exception e) {
                System.err.println("Database test error: " + e.getMessage());
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}