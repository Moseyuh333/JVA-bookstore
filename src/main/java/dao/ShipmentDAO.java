package dao;

import java.sql.*;
import java.util.*;
import models.*;
import utils.DBUtil;

public class ShipmentDAO {

    public List<Shipment> findByShipper(String username, String status, int page, int size) throws SQLException {
    List<Shipment> list = new ArrayList<>();

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
            "  CASE WHEN o.payment_method = 'cod' THEN o.total_amount ELSE 0 END AS cod_amount " +
            "FROM shipments s " +
            "JOIN orders o ON o.id = s.order_id " +
            "WHERE s.shipper_user_id = ? " +
            (status != null && !status.isEmpty() ? "AND s.status = ? " : "") +
            "ORDER BY s.last_update_at DESC " +
            "LIMIT ? OFFSET ?";

        try (Connection con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, username);
            if (status != null && !status.isEmpty()) ps.setString(i++, status);
            ps.setInt(i++, size);
            ps.setInt(i, (page - 1) * size);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapShipmentJoinedV2(rs));
                }
            }
        }
        return list;
    }


    public Shipment findById(long id) throws SQLException {
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
            "  CASE WHEN o.payment_method = 'cod' THEN o.total_amount ELSE 0 END AS cod_amount " +
            "FROM shipments s " +
            "JOIN orders o ON o.id = s.order_id " +
            "WHERE s.id = ?";


        try (Connection con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapShipmentJoinedV2(rs);
            }
        }
        return null;
    }

    private Shipment mapShipmentJoinedV2(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setId(rs.getLong("id"));
        s.setOrderId(rs.getLong("order_id"));
        s.setShipperUserId(rs.getString("shipper_user_id"));
        s.setStatus(rs.getString("status"));
        s.setLastUpdateAt(rs.getTimestamp("last_update_at"));

        // joined from orders
        s.setOrderCode(rs.getString("order_code"));
        s.setReceiverName(rs.getString("receiver_name"));
        s.setReceiverPhone(rs.getString("receiver_phone"));
        s.setReceiverAddress(rs.getString("receiver_address"));

        // derive COD from orders
        s.setCodAmount(rs.getDouble("cod_amount"));
        s.setCodCollected(false);

        return s;
    }


    public List<ShipmentEvent> findEvents(long shipmentId) throws SQLException {
        List<ShipmentEvent> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT * FROM shipment_events WHERE shipment_id=? ORDER BY created_at DESC")) {
            ps.setLong(1, shipmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ShipmentEvent ev = mapEvent(rs);
                list.add(ev);
            }
        }
        return list;
    }

    public void addEvent(long shipmentId, String status, String note, String evidenceUrl, String createdBy)
            throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO shipment_events (shipment_id,status,note,evidence_url,created_by) VALUES (?,?,?,?,?)")) {
            ps.setLong(1, shipmentId);
            ps.setString(2, status);
            ps.setString(3, note);
            ps.setString(4, evidenceUrl);
            ps.setString(5, createdBy);
            ps.executeUpdate();
        }

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE shipments SET status=?, last_update_at=NOW() WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, shipmentId);
            ps.executeUpdate();
        }
    }

    public void markDelivered(long shipmentId, boolean codCollected, String evidenceUrl,
                              String note, String createdBy) throws SQLException {
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false);

            // 1. update shipment
            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE shipments SET status='DELIVERED', cod_collected=?, proof_image_url=?, "
              + "delivered_at=NOW(), last_update_at=NOW() WHERE id=?");
            ps1.setBoolean(1, codCollected);
            ps1.setString(2, evidenceUrl);
            ps1.setLong(3, shipmentId);
            ps1.executeUpdate();

            // 2. add shipment event
            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO shipment_events (shipment_id,status,note,evidence_url,created_by) VALUES (?,?,?,?,?)");
            ps2.setLong(1, shipmentId);
            ps2.setString(2, "DELIVERED");
            ps2.setString(3, note);
            ps2.setString(4, evidenceUrl);
            ps2.setString(5, createdBy);
            ps2.executeUpdate();

            // 3. đồng bộ với orders
            PreparedStatement ps3 = con.prepareStatement(
                "UPDATE orders SET status='DELIVERED_WAITING_CONFIRM' "
              + "WHERE id=(SELECT order_id FROM shipments WHERE id=?)");
            ps3.setLong(1, shipmentId);
            ps3.executeUpdate();

            PreparedStatement ps4 = con.prepareStatement(
                "INSERT INTO order_events (order_id,status,created_by) "
              + "SELECT order_id,'DELIVERED_WAITING_CONFIRM',? "
              + "FROM shipments WHERE id=?");
            ps4.setString(1, createdBy);
            ps4.setLong(2, shipmentId);
            ps4.executeUpdate();

            con.commit();
        } catch (Exception e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.setAutoCommit(true);
            if (con != null) con.close();
        }
    }

    public Map<String, Integer> getStats(String username) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS c FROM shipments WHERE shipper_user_id=? GROUP BY status";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("c"));
        }
        int delivered = map.getOrDefault("DELIVERED", 0);
        int failed = map.getOrDefault("FAILED_DELIVERY", 0);
        int inProgress = map.getOrDefault("ASSIGNED", 0)
                        + map.getOrDefault("PICKED_UP", 0)
                        + map.getOrDefault("IN_TRANSIT", 0)
                        + map.getOrDefault("OUT_FOR_DELIVERY", 0);
        map.put("delivered", delivered);
        map.put("failed", failed);
        map.put("inProgress", inProgress);
        return map;
    }

    private ShipmentEvent mapEvent(ResultSet rs) throws SQLException {
        ShipmentEvent e = new ShipmentEvent();
        e.setId(rs.getLong("id"));
        e.setShipmentId(rs.getLong("shipment_id"));
        e.setStatus(rs.getString("status"));
        e.setNote(rs.getString("note"));
        e.setEvidenceUrl(rs.getString("evidence_url"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        e.setCreatedBy(rs.getString("created_by"));
        return e;
    }

    public void createForNewOrderRandomShipper(Connection con, long orderId) throws SQLException {
    // 1) lấy username của 1 shipper ngẫu nhiên
        final String pickSql =
            "SELECT u.username " +
            "FROM users u " +
            "JOIN user_roles ur ON ur.user_id = u.id " +
            "WHERE role = 'shipper'::user_role " +
            "ORDER BY random() " +
            "LIMIT 1";

        String shipperUser = null;
        try (PreparedStatement ps = con.prepareStatement(pickSql);
            ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                shipperUser = rs.getString(1);
            }
        }
        if (shipperUser == null || shipperUser.isEmpty()) {
            throw new SQLException("No shipper user found. Please seed at least one SHIPPER account.");
        }

        final String insSql =
            "INSERT INTO shipments (order_id, shipper_user_id, assigned_at, last_update_at) " +
            "VALUES (?, ?, NOW(), NOW())";

        try (PreparedStatement ps = con.prepareStatement(insSql)) {
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
