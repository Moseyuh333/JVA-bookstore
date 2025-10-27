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

            // Tổng người dùng
            String userQuery = "SELECT COUNT(*) AS total FROM users";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery);
                ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalUsers", rs.getInt("total"));
                }
            }

            // Tổng sản phẩm
            String productQuery = "SELECT COUNT(*) AS total FROM books";
            try (PreparedStatement stmt = conn.prepareStatement(productQuery);
                ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalProducts", rs.getInt("total"));
                }
            }

            // Tổng đơn hàng
            String orderQuery = "SELECT COUNT(*) AS total FROM orders";
            try (PreparedStatement stmt = conn.prepareStatement(orderQuery);
                ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalOrders", rs.getInt("total"));
                }
            }

            // Tổng doanh thu thật — dựa theo books.shop_id và đơn hàng delivered
            String revenueQuery =
                "WITH extracted_orders AS ( " +
                "   SELECT o.id AS order_id, b.shop_id, o.total_amount " +
                "   FROM orders o " +
                "   JOIN LATERAL jsonb_array_elements(o.cart_snapshot->'items') AS item ON TRUE " +
                "   JOIN books b ON (item->>'bookId')::int = b.id " +
                "   WHERE LOWER(o.status) = 'delivered' " +
                ") " +
                "SELECT COALESCE(SUM(o.total_amount), 0) AS revenue " +
                "FROM extracted_orders o;";

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

        String query =
            "WITH extracted_orders AS ( " +
            "   SELECT " +
            "       o.id AS order_id, " +
            "       DATE_TRUNC('month', o.created_at) AS month, " +
            "       b.shop_id, " +
            "       o.total_amount " +
            "   FROM orders o " +
            "   JOIN LATERAL jsonb_array_elements(o.cart_snapshot->'items') AS item ON TRUE " +
            "   JOIN books b ON (item->>'bookId')::int = b.id " +
            "   WHERE LOWER(o.status) = 'delivered' " +
            "   AND o.created_at >= NOW() - INTERVAL '6 months' " +
            ") " +
            "SELECT " +
            "   TO_CHAR(month, 'YYYY-MM') AS month, " +
            "   COALESCE(SUM(total_amount), 0) AS revenue " +
            "FROM extracted_orders " +
            "GROUP BY month " +
            "ORDER BY month;";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
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

   private JsonObject getTopSellers() throws SQLException {
        JsonObject topSellers = new JsonObject();

        String query =
            "WITH extracted_orders AS (" +
            "   SELECT o.id AS order_id, b.shop_id, o.total_amount " +
            "   FROM orders o " +
            "   JOIN LATERAL jsonb_array_elements(o.cart_snapshot->'items') AS item ON TRUE " +
            "   JOIN books b ON (item->>'bookId')::int = b.id " +
            "   WHERE LOWER(o.status) = 'delivered'" +
            ") " +
            "SELECT s.id AS shop_id, s.name AS store_name, s.status, " +
            "       COUNT(eo.order_id) AS total_orders, " +
            "       COALESCE(SUM(eo.total_amount), 0) AS revenue, " +
            "       ROUND(s.commission_rate, 2) AS commission_rate " +
            "FROM shops s " +
            "LEFT JOIN extracted_orders eo ON eo.shop_id = s.id " +
            "GROUP BY s.id, s.name, s.status, s.commission_rate " +
            "ORDER BY revenue DESC, total_orders DESC " +
            "LIMIT 5;";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {

            JsonObject sellers = new JsonObject();
            int index = 0;

            while (rs.next()) {
                JsonObject seller = new JsonObject();
                seller.addProperty("store_name", rs.getString("store_name"));
                seller.addProperty("total_orders", rs.getInt("total_orders"));
                seller.addProperty("revenue", rs.getDouble("revenue"));
                seller.addProperty("commission_rate", rs.getDouble("commission_rate"));
                sellers.add(String.valueOf(index), seller);
                index++;
            }

            topSellers.add("sellers", sellers);
        }

        return topSellers;
    }
}
