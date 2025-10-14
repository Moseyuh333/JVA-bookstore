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

@WebServlet(name = "HealthServlet", urlPatterns = {"/health"})
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        boolean dbOk = false;
        try (Connection con = DBUtil.getConnection()) {
            dbOk = (con != null && !con.isClosed());
        } catch (Exception e) {
            dbOk = false;
        }
        try (PrintWriter out = resp.getWriter()) {
            out.write("{\"status\":\"ok\",\"db\":" + (dbOk ? "\"up\"" : "\"down\"") + "}");
        }
    }
}
