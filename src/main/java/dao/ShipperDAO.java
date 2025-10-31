package dao;

import models.Shipper;
import utils.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ShipperDAO {

    private static volatile boolean schemaReady = false;

    private ShipperDAO() {
    }

    public static List<Shipper> findActiveShippers() throws SQLException {
        ensureSchema();
        String sql = "SELECT id, name, phone, email, base_fee, service_area, estimated_time, status, created_at, updated_at "
                + "FROM shippers "
                + "WHERE COALESCE(LOWER(status::text), '') = 'active' "
                + "ORDER BY name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Shipper> shippers = new ArrayList<>();
            while (rs.next()) {
                shippers.add(map(rs));
            }
            return shippers;
        }
    }

    public static List<Shipper> findAll() throws SQLException {
        ensureSchema();
        String sql = "SELECT id, name, phone, email, base_fee, service_area, estimated_time, status, created_at, updated_at "
                + "FROM shippers ORDER BY name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Shipper> shippers = new ArrayList<>();
            while (rs.next()) {
                shippers.add(map(rs));
            }
            return shippers;
        }
    }

    private static Shipper map(ResultSet rs) throws SQLException {
        Shipper shipper = new Shipper();
        shipper.setId(rs.getLong("id"));
        shipper.setName(rs.getString("name"));
        shipper.setPhone(rs.getString("phone"));
        shipper.setEmail(rs.getString("email"));
        BigDecimal baseFee = rs.getBigDecimal("base_fee");
        shipper.setBaseFee(baseFee != null ? baseFee : BigDecimal.ZERO);
        shipper.setServiceArea(rs.getString("service_area"));
        shipper.setEstimatedTime(rs.getString("estimated_time"));
        shipper.setStatus(rs.getString("status"));
        shipper.setCreatedAt(getDate(rs, "created_at"));
        shipper.setUpdatedAt(getDate(rs, "updated_at"));
        return shipper;
    }

    private static LocalDateTime getDate(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static void ensureSchema() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (ShipperDAO.class) {
            if (schemaReady) {
                return;
            }
            try (Connection conn = DBUtil.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DO $$ BEGIN "
                        + "IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'shipper_status') THEN "
                        + "CREATE TYPE shipper_status AS ENUM ('active', 'inactive', 'pending', 'banned'); "
                        + "END IF; END $$;");

                stmt.execute("CREATE TABLE IF NOT EXISTS shippers ("
                        + "id SERIAL PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "phone VARCHAR(30), "
                        + "email VARCHAR(255), "
                        + "base_fee DECIMAL(12,2) NOT NULL DEFAULT 0, "
                        + "service_area TEXT, "
                        + "estimated_time VARCHAR(120), "
                        + "status shipper_status NOT NULL DEFAULT 'active', "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE INDEX IF NOT EXISTS idx_shippers_status ON shippers(status)");
                stmt.execute("ALTER TABLE shippers ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP");
            }
            schemaReady = true;
        }
    }
}
