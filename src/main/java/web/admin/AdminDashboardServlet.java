package web.admin;

import com.google.gson.JsonObject;
import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/api/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            // Get stats
            JsonObject stats = getDashboardStats();
            response.add("stats", stats);

            // Get revenue data for chart
            JsonObject revenueData = getRevenueData();
            response.add("revenue", revenueData);

            // Get order status data for chart
            JsonObject orderStatusData = getOrderStatusData();
            response.add("orderStatus", orderStatusData);

            // Get top sellers
            JsonObject topSellers = getTopSellers();
            response.add("topSellers", topSellers);

            response.addProperty("success", true);

        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("success", false);
            response.addProperty("error", e.getMessage());
        }

        out.write(response.toString());
        out.flush();
    }

    private JsonObject getDashboardStats() throws SQLException {
        JsonObject stats = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            // Total users
            String userQuery = "SELECT COUNT(*) as total FROM users";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalUsers", rs.getInt("total"));
                }
            }

            // Total products
            String productQuery = "SELECT COUNT(*) as total FROM books";
            try (PreparedStatement stmt = conn.prepareStatement(productQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalProducts", rs.getInt("total"));
                }
            }

            // Total orders
            String orderQuery = "SELECT COUNT(*) as total FROM orders";
            try (PreparedStatement stmt = conn.prepareStatement(orderQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalOrders", rs.getInt("total"));
                }
            }

            // Total revenue
            String revenueQuery = "SELECT COALESCE(SUM(total_amount), 0) as revenue FROM orders WHERE status = 'completed'";
            try (PreparedStatement stmt = conn.prepareStatement(revenueQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalRevenue", rs.getDouble("revenue"));
                }
            }
        }

        return stats;
    }

    private JsonObject getRevenueData() throws SQLException {
        JsonObject revenue = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT " +
                "TO_CHAR(created_at, 'YYYY-MM') as month, " +
                "COALESCE(SUM(total_amount), 0) as revenue " +
                "FROM orders " +
                "WHERE status = 'completed' " +
                "AND created_at >= NOW() - INTERVAL '6 months' " +
                "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
                "ORDER BY month";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                JsonObject labels = new JsonObject();
                JsonObject data = new JsonObject();

                int index = 0;
                while (rs.next()) {
                    labels.addProperty(String.valueOf(index), rs.getString("month"));
                    data.addProperty(String.valueOf(index), rs.getDouble("revenue"));
                    index++;
                }

                revenue.add("labels", labels);
                revenue.add("data", data);
            }
        } catch (SQLException e) {
            // If PostgreSQL syntax fails, try a simpler approach
            System.out.println("PostgreSQL query failed, trying alternative: " + e.getMessage());
            try (Connection conn = DBUtil.getConnection()) {
                String query = "SELECT " +
                    "TO_CHAR(created_at, 'YYYY-MM') as month, " +
                    "COALESCE(SUM(total_amount), 0) as revenue " +
                    "FROM orders " +
                    "WHERE status = 'completed' " +
                    "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
                    "ORDER BY month";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    JsonObject labels = new JsonObject();
                    JsonObject data = new JsonObject();

                    int index = 0;
                    while (rs.next()) {
                        labels.addProperty(String.valueOf(index), rs.getString("month"));
                        data.addProperty(String.valueOf(index), rs.getDouble("revenue"));
                        index++;
                    }

                    revenue.add("labels", labels);
                    revenue.add("data", data);
                }
            }
        }

        return revenue;
    }

    private JsonObject getOrderStatusData() throws SQLException {
        JsonObject orderStatus = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT status, COUNT(*) as count FROM orders GROUP BY status";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                JsonObject labels = new JsonObject();
                JsonObject data = new JsonObject();

                int index = 0;
                while (rs.next()) {
                    String status = rs.getString("status");
                    labels.addProperty(String.valueOf(index), status);
                    data.addProperty(String.valueOf(index), rs.getInt("count"));
                    index++;
                }

                orderStatus.add("labels", labels);
                orderStatus.add("data", data);
            }
        }

        return orderStatus;
    }

    private JsonObject getTopSellers() throws SQLException {
        JsonObject topSellers = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT " +
                "u.id AS user_id, " +
                "COALESCE(u.username, u.email) AS store_name, " +
                "COUNT(o.id) AS total_orders, " +
                "COALESCE(SUM(o.total_amount), 0) AS revenue " +
                "FROM users u " +
                "LEFT JOIN orders o ON u.id = o.user_id AND LOWER(CAST(o.status AS TEXT)) IN ('completed', 'delivered') " +
                "WHERE u.role IN ('SELLER', 'ADMIN', 'USER') " +
                "GROUP BY u.id, u.username, u.email " +
                "ORDER BY revenue DESC, total_orders DESC " +
                "LIMIT 5";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                JsonObject sellers = new JsonObject();
                int index = 0;
                while (rs.next()) {
                    JsonObject seller = new JsonObject();
                    seller.addProperty("store_name", rs.getString("store_name"));
                    seller.addProperty("total_orders", rs.getInt("total_orders"));
                    seller.addProperty("revenue", rs.getDouble("revenue"));
                    seller.addProperty("commission_rate", 0);
                    sellers.add(String.valueOf(index), seller);
                    index++;
                }

                topSellers.add("sellers", sellers);
            }
        }

        return topSellers;
    }
}
