package web.admin;

import com.google.gson.JsonArray;
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
import java.util.ArrayList;
import java.util.List;

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

            // Get top shops
            JsonObject topShops = getTopShops();
            response.add("topShops", topShops);

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
            String revenueQuery = "SELECT COALESCE(SUM(total_amount), 0) AS revenue " +
                "FROM orders " +
                "WHERE LOWER(CAST(status AS TEXT)) IN ('completed', 'delivered')";
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
                "TO_CHAR(created_at, 'YYYY-MM') AS month, " +
                "COALESCE(SUM(total_amount), 0) AS revenue " +
                "FROM orders " +
                "WHERE LOWER(CAST(status AS TEXT)) IN ('completed', 'delivered') " +
                "AND created_at >= NOW() - INTERVAL '6 months' " +
                "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
                "ORDER BY month";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                List<String> months = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                while (rs.next()) {
                    months.add(rs.getString("month"));
                    values.add(rs.getDouble("revenue"));
                }

                revenue.add("labels", toJsonArray(months));
                revenue.add("data", toJsonArray(values));
            }
        } catch (SQLException e) {
            // If PostgreSQL syntax fails, try a simpler approach
            System.out.println("PostgreSQL query failed, trying alternative: " + e.getMessage());
            try (Connection conn = DBUtil.getConnection()) {
                String query = "SELECT " +
                    "TO_CHAR(created_at, 'YYYY-MM') AS month, " +
                    "COALESCE(SUM(total_amount), 0) AS revenue " +
                    "FROM orders " +
                    "WHERE LOWER(CAST(status AS TEXT)) IN ('completed', 'delivered') " +
                    "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
                    "ORDER BY month";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    List<String> months = new ArrayList<>();
                    List<Double> values = new ArrayList<>();

                    while (rs.next()) {
                        months.add(rs.getString("month"));
                        values.add(rs.getDouble("revenue"));
                    }

                    revenue.add("labels", toJsonArray(months));
                    revenue.add("data", toJsonArray(values));
                }
            }
        }

        return revenue;
    }

    private JsonArray toJsonArray(List<?> items) {
        JsonArray array = new JsonArray();
        for (Object item : items) {
            if (item == null) {
                array.add((String) null);
            } else if (item instanceof Number) {
                array.add(((Number) item).doubleValue());
            } else {
                array.add(item.toString());
            }
        }
        return array;
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

    private JsonObject getTopShops() throws SQLException {
        JsonObject topShops = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT " +
                "s.id AS shop_id, " +
                "s.name AS shop_name, " +
                "s.commission_rate, " +
                "COUNT(DISTINCT o.id) AS total_orders, " +
                "COALESCE(SUM(oi.total_price), 0) AS revenue " +
                "FROM shops s " +
                "LEFT JOIN books b ON s.id = b.shop_id " +
                "LEFT JOIN order_items oi ON b.id = oi.book_id " +
                "LEFT JOIN orders o ON oi.order_id = o.id AND LOWER(CAST(o.status AS TEXT)) IN ('completed', 'delivered') " +
                "GROUP BY s.id, s.name, s.commission_rate " +
                "ORDER BY revenue DESC, total_orders DESC " +
                "LIMIT 5";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                JsonObject shops = new JsonObject();
                int index = 0;
                while (rs.next()) {
                    JsonObject shop = new JsonObject();
                    shop.addProperty("store_name", rs.getString("shop_name"));
                    shop.addProperty("total_orders", rs.getInt("total_orders"));
                    shop.addProperty("revenue", rs.getDouble("revenue"));
                    shop.addProperty("commission_rate", rs.getDouble("commission_rate"));
                    shops.add(String.valueOf(index), shop);
                    index++;
                }

                topShops.add("shops", shops);
            }
        }

        return topShops;
    }
}
