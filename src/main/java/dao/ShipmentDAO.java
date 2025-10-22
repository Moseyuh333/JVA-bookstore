package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Shipment;
import models.ShipmentEvent;
import utils.DBUtil;

public class ShipmentDAO {

    public List<Shipment> findByShipper(String username, String status, int page, int size) throws SQLException {
        List<Shipment> list = new ArrayList<Shipment>();
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
            "WHERE ( s.shipper_user_id = ? " +
            "        OR s.shipper_user_id = (SELECT CAST(u.id AS TEXT) FROM users u WHERE u.username = ? LIMIT 1) ) " +
            (filterStatus ? "AND s.status = ? " : "") +
            "ORDER BY s.last_update_at DESC " +
            "LIMIT ? OFFSET ?";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(sql);

            int i = 1;
            ps.setString(i++, username);
            ps.setString(i++, username);
            if (filterStatus) ps.setString(i++, status);
            ps.setInt(i++, size);
            ps.setInt(i, (page - 1) * size);

            rs = ps.executeQuery();
            while (rs.next()) list.add(mapShipmentJoinedV2(rs));
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
        return list;
    }

    // ======== FIND one (owned) ========
    public Shipment findByIdOwned(long id, String username) throws SQLException {
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
            "WHERE s.id = ? AND ( s.shipper_user_id = ? " +
            "   OR s.shipper_user_id = (SELECT CAST(u.id AS TEXT) FROM users u WHERE u.username = ? LIMIT 1) )";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setString(2, username);
            ps.setString(3, username);
            rs = ps.executeQuery();
            if (rs.next()) return mapShipmentJoinedV2(rs);
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
    }

    // ======== FIND by id (plain) ========
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
            "  CASE WHEN o.payment_method = 'cod' THEN o.total_amount ELSE 0 END AS cod_amount, " +
            "  (o.shipping_snapshot->>'recipientName') AS customer_name " +
            "FROM shipments s " +
            "LEFT JOIN orders o ON o.id = s.order_id " +
            "WHERE s.id = ?";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapShipmentJoinedV2(rs);
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
    }

    // ======== Events / Updates ========
    public List<ShipmentEvent> findEvents(long shipmentId) throws SQLException {
        List<ShipmentEvent> list = new ArrayList<ShipmentEvent>();
        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement("SELECT * FROM shipment_events WHERE shipment_id=? ORDER BY created_at DESC");
            ps.setLong(1, shipmentId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapEvent(rs));
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
        return list;
    }

    public void addEvent(long shipmentId, String status, String note, String evidenceUrl, String createdBy)
            throws SQLException {
        Connection con = null; PreparedStatement ps = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(
                "INSERT INTO shipment_events (shipment_id,status,note,evidence_url,created_by) VALUES (?,?,?,?,?)");
            ps.setLong(1, shipmentId); ps.setString(2, status); ps.setString(3, note);
            ps.setString(4, evidenceUrl); ps.setString(5, createdBy);
            ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }

        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement("UPDATE shipments SET status=?, last_update_at=NOW() WHERE id=?");
            ps.setString(1, status); ps.setLong(2, shipmentId); ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
    }

    public void markDelivered(long shipmentId, boolean codCollected, String evidenceUrl,
                              String note, String createdBy) throws SQLException {
        Connection con = null;
        try {
            con = DBUtil.getConnection(); con.setAutoCommit(false);

            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE shipments SET status='DELIVERED', cod_collected=?, proof_image_url=?, " +
                "delivered_at=NOW(), last_update_at=NOW() WHERE id=?");
            ps1.setBoolean(1, codCollected); ps1.setString(2, evidenceUrl); ps1.setLong(3, shipmentId); ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO shipment_events (shipment_id,status,note,evidence_url,created_by) VALUES (?,?,?,?,?)");
            ps2.setLong(1, shipmentId); ps2.setString(2, "DELIVERED"); ps2.setString(3, note);
            ps2.setString(4, evidenceUrl); ps2.setString(5, createdBy); ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                "UPDATE orders SET status='DELIVERED_WAITING_CONFIRM' WHERE id=(SELECT order_id FROM shipments WHERE id=?)");
            ps3.setLong(1, shipmentId); ps3.executeUpdate();

            PreparedStatement ps4 = con.prepareStatement(
                "INSERT INTO order_events (order_id,status,created_by) " +
                "SELECT order_id,'DELIVERED_WAITING_CONFIRM',? FROM shipments WHERE id=?");
            ps4.setString(1, createdBy); ps4.setLong(2, shipmentId); ps4.executeUpdate();

            con.commit();
        } catch (Exception e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
    }

    // ======== Stats ========
    public Map<String, Integer> getStats(String username) throws SQLException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String sql =
            "SELECT s.status, COUNT(*) AS c " +
            "FROM shipments s " +
            "WHERE ( s.shipper_user_id = ? " +
            "   OR s.shipper_user_id = (SELECT CAST(u.id AS TEXT) FROM users u WHERE u.username=? LIMIT 1) ) " +
            "GROUP BY s.status";
        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, username); ps.setString(2, username);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("c"));
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (ps != null) try { ps.close(); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
        int delivered = map.getOrDefault("DELIVERED", 0);
        int failed = map.getOrDefault("FAILED_DELIVERY", 0);
        int inProgress = map.getOrDefault("ASSIGNED", 0)
                        + map.getOrDefault("PICKED_UP", 0)
                        + map.getOrDefault("IN_TRANSIT", 0)
                        + map.getOrDefault("OUT_FOR_DELIVERY", 0)
                        + map.getOrDefault("pending", 0); // nếu seed để chữ thường
        map.put("delivered", delivered);
        map.put("failed", failed);
        map.put("inProgress", inProgress);
        return map;
    }

    public void createForNewOrderRandomShipper(Connection con, long orderId) throws SQLException {
        final String pickSql = "SELECT u.username FROM users u WHERE role = 'shipper'::user_role ORDER BY random() LIMIT 1";
        String shipperUser = null;
        PreparedStatement ps = null; ResultSet rs = null;
        try { ps = con.prepareStatement(pickSql); rs = ps.executeQuery(); if (rs.next()) shipperUser = rs.getString(1); }
        finally { if (rs != null) try { rs.close(); } catch (Exception ignore) {} if (ps != null) try { ps.close(); } catch (Exception ignore) {} }
        if (shipperUser == null || shipperUser.isEmpty()) throw new SQLException("No shipper user found. Please seed at least one SHIPPER account.");

        final String insSql = "INSERT INTO shipments (order_id, shipper_user_id, assigned_at, last_update_at) VALUES (?, ?, NOW(), NOW())";
        try { ps = con.prepareStatement(insSql); ps.setLong(1, orderId); ps.setString(2, shipperUser); ps.executeUpdate(); }
        finally { if (ps != null) try { ps.close(); } catch (Exception ignore) {} }
    }

    public void createForNewOrderRandomShipper(long orderId) throws SQLException {
        Connection con = null;
        try { con = DBUtil.getConnection(); con.setAutoCommit(false); createForNewOrderRandomShipper(con, orderId); con.commit(); }
        catch (SQLException ex) { if (con != null) try { con.rollback(); } catch (Exception ignore) {} throw ex; }
        finally { if (con != null) try { con.setAutoCommit(true); } catch (Exception ignore) {} if (con != null) try { con.close(); } catch (Exception ignore) {} }
    }

    // ======== Mappers ========
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
}
