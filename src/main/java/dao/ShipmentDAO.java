package dao;

import java.sql.*;
import java.util.*;
import models.*;
import utils.DBUtil;

public class ShipmentDAO {

    public List<Shipment> findByShipper(String username, String status, int page, int size) throws SQLException {
        List<Shipment> list = new ArrayList<>();
        boolean filterStatus = status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status);

        String sql =
            "SELECT " +
            "  s.id, s.order_id, s.shipper_user_id, s.status, s.last_update_at, " +
            "  o.code AS order_code, " +
            "  (o.shipping_snapshot->>'recipientName') AS receiver_name, " +
            "  (o.shipping_snapshot->>'phone')         AS receiver_phone, " +
            "  NULLIF(CONCAT_WS(', ', " +
            "      NULLIF(o.shipping_snapshot->>'line1',''), " +
            "      NULLIF(o.shipping_snapshot->>'line2',''), " +
            "      NULLIF(o.shipping_snapshot->>'ward',''), " +
            "      NULLIF(o.shipping_snapshot->>'district',''), " +
            "      NULLIF(o.shipping_snapshot->>'city',''), " +
            "      NULLIF(o.shipping_snapshot->>'province','')" +
            "  ), '') AS receiver_address, " +
            "  CASE WHEN o.payment_method = 'cod' THEN o.total_amount ELSE 0 END AS cod_amount, " +
            "  (o.shipping_snapshot->>'recipientName') AS customer_name " +
            "FROM shipments s " +
            "LEFT JOIN orders o ON o.id = s.order_id " +
            "WHERE ( " +
            "    TRIM(s.shipper_user_id) = ? " +
            "    OR LOWER(TRIM(s.shipper_user_id)) = LOWER(?) " +
            "    OR TRIM(s.shipper_user_id) = ( " +
            "        SELECT CAST(u.id AS TEXT) FROM users u WHERE u.username = ? LIMIT 1" +
            "    )" +
            ") " +
            (filterStatus ? "AND s.status = ? " : "") +
            "ORDER BY s.last_update_at DESC " +
            "LIMIT ? OFFSET ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, username);
            ps.setString(i++, username);
            ps.setString(i++, username);
            if (filterStatus) ps.setString(i++, status);
            ps.setInt(i++, size);
            ps.setInt(i, (page - 1) * size);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapShipmentJoinedV2(rs));
        }
        return list;
    }

    public Shipment findByIdOwned(long id, String username) throws SQLException {
        String sql =
            "SELECT s.id, s.order_id, s.shipper_user_id, s.status, s.last_update_at, " +
            "  o.code AS order_code, " +
            "  (o.shipping_snapshot->>'recipientName') AS receiver_name, " +
            "  (o.shipping_snapshot->>'phone') AS receiver_phone, " +
            "  NULLIF(CONCAT_WS(', ', " +
            "      NULLIF(o.shipping_snapshot->>'line1',''), " +
            "      NULLIF(o.shipping_snapshot->>'line2',''), " +
            "      NULLIF(o.shipping_snapshot->>'ward',''), " +
            "      NULLIF(o.shipping_snapshot->>'district',''), " +
            "      NULLIF(o.shipping_snapshot->>'city',''), " +
            "      NULLIF(o.shipping_snapshot->>'province','')" +
            "  ), '') AS receiver_address, " +
            "  CASE WHEN o.payment_method='cod' THEN o.total_amount ELSE 0 END AS cod_amount, " +
            "  (o.shipping_snapshot->>'recipientName') AS customer_name " +
            "FROM shipments s " +
            "LEFT JOIN orders o ON o.id = s.order_id " +
            "WHERE s.id = ? AND ( " +
            "    TRIM(s.shipper_user_id) = ? " +
            "    OR LOWER(TRIM(s.shipper_user_id)) = LOWER(?) " +
            "    OR TRIM(s.shipper_user_id) = ( " +
            "        SELECT CAST(u.id AS TEXT) FROM users u WHERE u.username = ? LIMIT 1" +
            "    )" +
            ")";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setString(2, username);
            ps.setString(3, username);
            ps.setString(4, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapShipmentJoinedV2(rs);
        }
        return null;
    }

    public Shipment findById(long id) throws SQLException {
        String sql =
            "SELECT s.id, s.order_id, s.shipper_user_id, s.status, s.last_update_at, " +
            "  o.code AS order_code, " +
            "  (o.shipping_snapshot->>'recipientName') AS receiver_name, " +
            "  (o.shipping_snapshot->>'phone') AS receiver_phone, " +
            "  NULLIF(CONCAT_WS(', ', " +
            "      NULLIF(o.shipping_snapshot->>'line1',''), " +
            "      NULLIF(o.shipping_snapshot->>'line2',''), " +
            "      NULLIF(o.shipping_snapshot->>'ward',''), " +
            "      NULLIF(o.shipping_snapshot->>'district',''), " +
            "      NULLIF(o.shipping_snapshot->>'city',''), " +
            "      NULLIF(o.shipping_snapshot->>'province','')" +
            "  ), '') AS receiver_address, " +
            "  CASE WHEN o.payment_method='cod' THEN o.total_amount ELSE 0 END AS cod_amount, " +
            "  (o.shipping_snapshot->>'recipientName') AS customer_name " +
            "FROM shipments s LEFT JOIN orders o ON o.id=s.order_id WHERE s.id=?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapShipmentJoinedV2(rs);
        }
        return null;
    }

    public Map<String, Integer> getStats(String username) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql =
            "SELECT s.status, COUNT(*) AS c FROM shipments s " +
            "WHERE ( TRIM(s.shipper_user_id)=? " +
            "    OR LOWER(TRIM(s.shipper_user_id))=LOWER(?) " +
            "    OR TRIM(s.shipper_user_id)=(SELECT CAST(u.id AS TEXT) FROM users u WHERE u.username=? LIMIT 1) ) " +
            "GROUP BY s.status";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("c"));
        }

        int delivered = map.getOrDefault("DELIVERED", 0);
        int failed = map.getOrDefault("FAILED_DELIVERY", 0);
        int inProgress = map.getOrDefault("ASSIGNED", 0)
                        + map.getOrDefault("PICKED_UP", 0)
                        + map.getOrDefault("IN_TRANSIT", 0)
                        + map.getOrDefault("OUT_FOR_DELIVERY", 0)
                        + map.getOrDefault("pending", 0);
        map.put("delivered", delivered);
        map.put("failed", failed);
        map.put("inProgress", inProgress);
        return map;
    }

    private Shipment mapShipmentJoinedV2(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setId(rs.getLong("id"));
        s.setOrderId(rs.getLong("order_id"));
        s.setShipperUserId(rs.getString("shipper_user_id"));
        s.setStatus(rs.getString("status"));
        s.setLastUpdateAt(rs.getTimestamp("last_update_at"));
        s.setOrderCode(rs.getString("order_code"));
        s.setReceiverName(rs.getString("receiver_name"));
        s.setReceiverPhone(rs.getString("receiver_phone"));
        s.setReceiverAddress(rs.getString("receiver_address"));
        s.setCodAmount(rs.getDouble("cod_amount"));
        try { s.getClass().getMethod("setCustomerName", String.class).invoke(s, rs.getString("customer_name")); } catch (Exception ignore) {}
        s.setCodCollected(false);
        return s;
    }

    public void createForNewOrderRandomShipper(Connection con, long orderId) throws SQLException {
        final String pickSql = "SELECT u.username FROM users u WHERE role='shipper'::user_role ORDER BY random() LIMIT 1";
        String shipperUser = null;
        try (PreparedStatement ps = con.prepareStatement(pickSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) shipperUser = rs.getString(1);
        }
        if (shipperUser == null || shipperUser.isEmpty()) throw new SQLException("No shipper user found.");
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO shipments (order_id, shipper_user_id, assigned_at, last_update_at) VALUES (?, ?, NOW(), NOW())")) {
            ps.setLong(1, orderId);
            ps.setString(2, shipperUser);
            ps.executeUpdate();
        }
    }

    public void createForNewOrderRandomShipper(long orderId) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                createForNewOrderRandomShipper(con, orderId);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
