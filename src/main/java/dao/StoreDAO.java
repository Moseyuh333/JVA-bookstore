package dao;

import models.Store;
import utils.DBUtil;

import java.sql.*;

public class StoreDAO {

    public Store getStoreByOwnerId(int ownerId) {
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM stores WHERE owner_id = ?");
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Store(
                        rs.getInt("id"),
                        rs.getInt("owner_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("description")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createOrUpdateStore(int ownerId, String name, String address, String description) {
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM stores WHERE owner_id = ?");
            check.setInt(1, ownerId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE stores SET name=?, address=?, description=? WHERE owner_id=?");
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, description);
                ps.setInt(4, ownerId);
                return ps.executeUpdate() > 0;
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO stores(owner_id, name, address, description) VALUES(?, ?, ?, ?)");
                ps.setInt(1, ownerId);
                ps.setString(2, name);
                ps.setString(3, address);
                ps.setString(4, description);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
