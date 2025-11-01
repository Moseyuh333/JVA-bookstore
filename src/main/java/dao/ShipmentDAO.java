package dao;

import java.sql.*;
import java.util.*;
import models.*;
import utils.DBUtil;

public class ShipmentDAO {

    // ================== HELPER: map email -> username ==================
    /** 
     * Chuẩn hoá định danh shipper lấy từ JWT/login.
     * - Nếu là email: tra bảng users để lấy username tương ứng.
     * - Nếu không phải email: dùng nguyên làm username.
     * Trả về null nếu rỗng.
     */
    private String resolveUsernameFromIdentifier(Connection con, String ident) throws SQLException {
        if (ident == null) return null;
        String trimmed = ident.trim();
        if (trimmed.isEmpty()) return null;

        if (trimmed.contains("@")) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT username FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1")) {
                ps.setString(1, trimmed);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString(1);
                }
            }
            // Không tìm thấy email -> fallback dùng nguyên giá trị (để không chặn)
            return trimmed;
        }

        // Không phải email -> coi như username
        return trimmed;
    }

    // ================= FIND BY SHIPPER =================
    public List<Shipment> findByShipper(String usernameOrEmail, String status, String q, int page, int size) throws SQLException {
        List<Shipment> list = new ArrayList<>();
        boolean filterStatus = status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status);
        boolean filterQ = q != null && !q.isEmpty();

        final String selectSql =
            "SELECT " +
            "  s.id, s.order_id, s.shipper_user_id, s.status, s.last_update_at, s.cod_collected, " +
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
            "WHERE LOWER(TRIM(s.shipper_user_id)) = LOWER(?) " +
            (filterStatus ? "AND s.status = ? " : "") +
            (filterQ ? "AND (LOWER(s.id::text) LIKE LOWER(?) OR LOWER(o.code) LIKE LOWER(?) OR LOWER(o.shipping_snapshot->>'recipientName') LIKE LOWER(?)) " : "") +
            "ORDER BY s.last_update_at DESC " +
            "LIMIT ? OFFSET ?";

        try (Connection con = DBUtil.getConnection()) {
            String resolvedUsername = resolveUsernameFromIdentifier(con, usernameOrEmail);
            if (resolvedUsername == null || resolvedUsername.isBlank()) {
                return list;
            }

            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                int i = 1;
                ps.setString(i++, resolvedUsername);
                if (filterStatus) ps.setString(i++, status);
                if (filterQ) {
                    String like = "%" + q + "%";
                    ps.setString(i++, like);
                    ps.setString(i++, like);
                    ps.setString(i++, like);
                }
                ps.setInt(i++, Math.max(size, 1));
                ps.setInt(i, Math.max(page, 1) - 1 >= 0 ? (Math.max(page, 1) - 1) * Math.max(size, 1) : 0);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapShipmentJoinedV2(rs));
                }
            }
        }
        return list;
    }

    // ================= FIND ONE BY ID & SHIPPER =================
    public Shipment findByIdOwned(long id, String usernameOrEmail) throws SQLException {
        final String selectSql =
            "SELECT s.id, s.order_id, s.shipper_user_id, s.status, s.last_update_at, s.cod_collected, " +
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
            "  CASE WHEN o.payment_method='cod' THEN o.total_amount ELSE 0 END AS cod_amount, " +
            "  (o.shipping_snapshot->>'recipientName') AS customer_name " +
            "FROM shipments s " +
            "LEFT JOIN orders o ON o.id = s.order_id " +
            "WHERE s.id = ? AND LOWER(TRIM(s.shipper_user_id)) = LOWER(?)";

        try (Connection con = DBUtil.getConnection()) {
            String resolvedUsername = resolveUsernameFromIdentifier(con, usernameOrEmail);
            if (resolvedUsername == null || resolvedUsername.isBlank()) return null;

            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setLong(1, id);
                ps.setString(2, resolvedUsername);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapShipmentJoinedV2(rs);
                }
            }
        }
        return null;
    }

    // ================= FIND BY ID =================
    public Shipment findById(long id) throws SQLException {
        String sql =
            "SELECT s.id, s.order_id, s.shipper_user_id, s.status, s.last_update_at, s.cod_collected, " +
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
            "  CASE WHEN o.payment_method='cod' THEN o.total_amount ELSE 0 END AS cod_amount, " +
            "  (o.shipping_snapshot->>'recipientName') AS customer_name " +
            "FROM shipments s LEFT JOIN orders o ON o.id=s.order_id WHERE s.id=?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapShipmentJoinedV2(rs);
            }
        }
        return null;
    }

    // ================= EVENTS (RESTORED) =================
    public List<ShipmentEvent> findEvents(long shipmentId) throws SQLException {
        List<ShipmentEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM shipment_events WHERE shipment_id=? ORDER BY created_at DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEvent(rs));
            }
        }
        return list;
    }

    public void addEvent(long shipmentId, String status, String note, String evidenceUrl, String createdBy)
            throws SQLException {

        final String insertEventSql =
            "INSERT INTO shipment_events (shipment_id, status, note, evidence_url, created_by, created_at) " +
            "VALUES (?, ?::shipment_status, ?, ?, ?, NOW())";

        final String updateShipmentSql =
            "UPDATE shipments SET status = ?::shipment_status, last_update_at = NOW() WHERE id = ?";

        final String fetchOrderSql = "SELECT order_id FROM shipments WHERE id = ?";

        Connection con = null;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false);

            Long orderId = null;
            try (PreparedStatement psFetch = con.prepareStatement(fetchOrderSql)) {
                psFetch.setLong(1, shipmentId);
                try (ResultSet rs = psFetch.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getLong(1);
                    }
                }
            }

            try (PreparedStatement psIns = con.prepareStatement(insertEventSql)) {
                psIns.setLong(1, shipmentId);
                psIns.setString(2, status); // cast sang enum ở SQL
                if (note == null || note.trim().isEmpty()) {
                    psIns.setNull(3, Types.VARCHAR);
                } else {
                    psIns.setString(3, note.trim());
                }
                if (evidenceUrl == null || evidenceUrl.isEmpty()) {
                    psIns.setNull(4, Types.VARCHAR);
                } else {
                    psIns.setString(4, evidenceUrl);
                }
                if (createdBy == null || createdBy.isEmpty()) {
                    psIns.setNull(5, Types.VARCHAR);
                } else {
                    psIns.setString(5, createdBy);
                }
                psIns.executeUpdate();
            }

            try (PreparedStatement psUpd = con.prepareStatement(updateShipmentSql)) {
                psUpd.setString(1, status); // cast sang enum ở SQL
                psUpd.setLong(2, shipmentId);
                psUpd.executeUpdate();
            }

            if (orderId != null) {
                String mappedStatus = mapOrderStatusFromShipment(status);
                if (mappedStatus != null) {
                    String trimmedNote = note == null ? "" : note.trim();
                    String historyNote = trimmedNote.isEmpty()
                        ? defaultOrderHistoryNote(mappedStatus)
                        : trimmedNote;
                    String historyActor = createdBy == null || createdBy.trim().isEmpty()
                        ? "shipper"
                        : "shipper:" + createdBy.trim();
                    updateOrderStatusFromShipment(con, orderId, mappedStatus, historyNote, historyActor);
                }
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (Exception ignore) {}
            throw e;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
    }

    public void markDelivered(long shipmentId, boolean codCollected, String evidenceUrl,
                              String note, String createdBy) throws SQLException {
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false);

            String normalizedEvidence = evidenceUrl == null ? null : evidenceUrl.trim();
            String normalizedNote = note == null ? null : note.trim();
            String normalizedCreator = createdBy == null ? null : createdBy.trim();

            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE shipments SET status='DELIVERED', cod_collected=?, evidence_url=?, " +
                "delivered_at=NOW(), last_update_at=NOW() WHERE id=?");
            ps1.setBoolean(1, codCollected);
            if (normalizedEvidence == null || normalizedEvidence.isEmpty()) {
                ps1.setNull(2, Types.VARCHAR);
            } else {
                ps1.setString(2, normalizedEvidence);
            }
            ps1.setLong(3, shipmentId);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO shipment_events (shipment_id,status,note,evidence_url,created_by) VALUES (?,?,?,?,?)");
            ps2.setLong(1, shipmentId);
            ps2.setString(2, "DELIVERED");
            if (normalizedNote == null || normalizedNote.isEmpty()) {
                ps2.setNull(3, Types.VARCHAR);
            } else {
                ps2.setString(3, normalizedNote);
            }
            if (normalizedEvidence == null || normalizedEvidence.isEmpty()) {
                ps2.setNull(4, Types.VARCHAR);
            } else {
                ps2.setString(4, normalizedEvidence);
            }
            if (normalizedCreator == null || normalizedCreator.isEmpty()) {
                ps2.setNull(5, Types.VARCHAR);
            } else {
                ps2.setString(5, normalizedCreator);
            }
            ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                "UPDATE orders SET status='delivered' " +
                "WHERE id=(SELECT order_id FROM shipments WHERE id=?)");
            ps3.setLong(1, shipmentId);
            ps3.executeUpdate();

            con.commit();
        } catch (Exception e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (Exception ignore) {}
            if (con != null) try { con.close(); } catch (Exception ignore) {}
        }
    }

    // ================= GET STATS =================
    public Map<String, Integer> getStats(String usernameOrEmail) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        final String sql =
            "SELECT s.status, COUNT(*) AS c FROM shipments s " +
            "WHERE LOWER(TRIM(s.shipper_user_id)) = LOWER(?) " +
            "GROUP BY s.status";

        try (Connection con = DBUtil.getConnection()) {
            String resolvedUsername = resolveUsernameFromIdentifier(con, usernameOrEmail);
            if (resolvedUsername == null || resolvedUsername.isBlank()) return map;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, resolvedUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) map.put(rs.getString("status"), rs.getInt("c"));
                }
            }
        }

        int delivered = map.getOrDefault("DELIVERED", 0);
        int failed = map.getOrDefault("FAILED_DELIVERY", 0);
        int inProgress = map.getOrDefault("ASSIGNED", 0)
                        + map.getOrDefault("PICKED_UP", 0)
                        + map.getOrDefault("OUT_FOR_DELIVERY", 0)
                        + map.getOrDefault("RETURNING", 0)
                        + map.getOrDefault("pending", 0);
        map.put("delivered", delivered);
        map.put("failed", failed);
        map.put("inProgress", inProgress);
        return map;
    }

    private void updateOrderStatusFromShipment(Connection con, long orderId, String newStatus,
                                               String note, String actor) throws SQLException {
        final String updateSql =
            "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ? AND status <> ?";
        final String insertHistorySql =
            "INSERT INTO order_status_history (order_id, status, note, created_by, created_at) VALUES (?, ?, ?, ?, NOW())";

        int affected;
        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, orderId);
            ps.setString(3, newStatus);
            affected = ps.executeUpdate();
        }

        if (affected > 0) {
            try (PreparedStatement ps = con.prepareStatement(insertHistorySql)) {
                ps.setLong(1, orderId);
                ps.setString(2, newStatus);
                if (note == null || note.trim().isEmpty()) {
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(3, note.trim());
                }
                if (actor == null || actor.trim().isEmpty()) {
                    ps.setString(4, "shipper");
                } else {
                    ps.setString(4, actor.trim());
                }
                ps.executeUpdate();
            }
        }
    }

    private String mapOrderStatusFromShipment(String shipmentStatus) {
        if (shipmentStatus == null) {
            return null;
        }
        switch (shipmentStatus.toUpperCase(Locale.ROOT)) {
            case "FAILED_DELIVERY":
                return "failed";
            case "DELIVERED":
                return "delivered";
            case "RETURNING":
            case "RETURNED":
                return "returned";
            case "CANCELLED":
                return "cancelled";
            case "ASSIGNED":
            case "PICKED_UP":
            case "OUT_FOR_DELIVERY":
                return "shipping";
            default:
                return null;
        }
    }

    private String defaultOrderHistoryNote(String orderStatus) {
        if (orderStatus == null) {
            return "Cập nhật trạng thái đơn hàng";
        }
        switch (orderStatus) {
            case "failed":
                return "Shipper báo giao thất bại";
            case "delivered":
                return "Shipper xác nhận đã giao thành công";
            case "returned":
                return "Đơn hàng đang/đã trả về";
            case "cancelled":
                return "Đơn hàng bị hủy trong quá trình giao";
            case "shipping":
                return "Đơn hàng đang được giao";
            default:
                return "Cập nhật trạng thái đơn hàng";
        }
    }

    // ================= MAPPERS =================
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
        s.setCodCollected(rs.getBoolean("cod_collected"));
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

    // ================= SEED RANDOM SHIPPER =================
    public void ensureShipmentAssigned(Connection con, long orderId) throws SQLException {
        if (shipmentExists(con, orderId)) {
            return;
        }
        createForNewOrderRandomShipper(con, orderId);
    }

    public void createForNewOrderRandomShipper(long orderId) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                ensureShipmentAssigned(con, orderId);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void createForNewOrderRandomShipper(Connection con, long orderId) throws SQLException {
        if (shipmentExists(con, orderId)) {
            return;
        }
        final String pickSql = "SELECT u.username FROM users u WHERE role='shipper'::user_role AND (status IS NULL OR status NOT IN ('banned', 'inactive')) ORDER BY random() LIMIT 1";
        String shipperUser = null;
        try (PreparedStatement ps = con.prepareStatement(pickSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) shipperUser = rs.getString(1);
        }
        if (shipperUser == null || shipperUser.isEmpty()) throw new SQLException("No active shipper user found.");
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO shipments (order_id, shipper_user_id, assigned_at, last_update_at) VALUES (?, ?, NOW(), NOW())")) {
            ps.setLong(1, orderId);
            ps.setString(2, shipperUser);
            ps.executeUpdate();
        }
    }

    private boolean shipmentExists(Connection con, long orderId) throws SQLException {
        final String sql = "SELECT 1 FROM shipments WHERE order_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Map<String, Object> findUserProfile(String ident) throws SQLException {
        if (ident == null || ident.trim().isEmpty()) return null;
        String key = ident.trim();

        String sql = "SELECT username, email, COALESCE(NULLIF(full_name,''), username) AS full_name, phone " +
                    "FROM users WHERE LOWER(username)=LOWER(?) OR LOWER(email)=LOWER(?) LIMIT 1";
        try (Connection con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("username", rs.getString("username"));
                    m.put("email", rs.getString("email"));
                    m.put("fullName", rs.getString("full_name"));
                    m.put("phone", rs.getString("phone"));
                    return m;
                }
            }
        }
        return null;
    }
}
