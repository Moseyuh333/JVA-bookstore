package web;

import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/login"})
public class AuthServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        // Demo only: accept any non-empty creds. Replace with DB lookup.
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            String token = JwtUtil.generateToken(username);
            resp.setContentType("application/json");
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"token\":\"" + token + "\"}");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
        }
    }
}
