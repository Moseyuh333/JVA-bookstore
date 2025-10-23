package web;

import com.google.gson.Gson;
import models.Store;
import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/vendor/stores")
public class VendorStoreServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        int ownerId = getLoggedUserId(req);
        List<Store> stores = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM stores WHERE owner_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Store store = new Store(
                        rs.getInt("id"),
                        rs.getInt("owner_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("description")
                );
                stores.add(store);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Trả JSON danh sách cửa hàng
        resp.getWriter().write(gson.toJson(stores));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        int ownerId = getLoggedUserId(req);
        String name = req.getParameter("name");
        String address = req.getParameter("address");
        String description = req.getParameter("description");

        Store store = null;

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO stores (owner_id, name, address, description) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, ownerId);
            stmt.setString(2, name);
            stmt.setString(3, address);
            stmt.setString(4, description);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                store = new Store(id, ownerId, name, address, description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (store != null) {
            resp.getWriter().write(gson.toJson(store));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Không thể tạo cửa hàng\"}");
        }
    }

    // 👉 Hàm giả lập lấy ID user đang đăng nhập (tùy project)
    private int getLoggedUserId(HttpServletRequest req) {
        Object userId = req.getSession().getAttribute("user_id");
        if (userId != null) {
            return (int) userId;
        }
        // nếu chưa đăng nhập, trả 0 hoặc lỗi
        return 0;
    }
}
