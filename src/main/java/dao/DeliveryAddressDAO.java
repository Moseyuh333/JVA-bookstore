package dao;

import models.DeliveryAddress;
import utils.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DeliveryAddressDAO {
    
    /**
     * Get all delivery addresses for a user
     */
    public static List<DeliveryAddress> getAddressesByUserId(int userId) {
        String sql = "SELECT id, user_id, recipient_name, phone_number, province, district, " +
                    "ward, address_detail, is_default, created_at, updated_at " +
                    "FROM delivery_addresses WHERE user_id = ? " +
                    "ORDER BY is_default DESC, created_at DESC";
        
        List<DeliveryAddress> addresses = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                DeliveryAddress address = new DeliveryAddress();
                address.setId(rs.getInt("id"));
                address.setUserId(rs.getInt("user_id"));
                address.setRecipientName(rs.getString("recipient_name"));
                address.setPhoneNumber(rs.getString("phone_number"));
                address.setProvince(rs.getString("province"));
                address.setDistrict(rs.getString("district"));
                address.setWard(rs.getString("ward"));
                address.setAddressDetail(rs.getString("address_detail"));
                address.setDefault(rs.getBoolean("is_default"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    address.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    address.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                addresses.add(address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return addresses;
    }
    
    /**
     * Get default delivery address for user
     */
    public static DeliveryAddress getDefaultAddress(int userId) {
        String sql = "SELECT id, user_id, recipient_name, phone_number, province, district, " +
                    "ward, address_detail, is_default, created_at, updated_at " +
                    "FROM delivery_addresses WHERE user_id = ? AND is_default = true";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                DeliveryAddress address = new DeliveryAddress();
                address.setId(rs.getInt("id"));
                address.setUserId(rs.getInt("user_id"));
                address.setRecipientName(rs.getString("recipient_name"));
                address.setPhoneNumber(rs.getString("phone_number"));
                address.setProvince(rs.getString("province"));
                address.setDistrict(rs.getString("district"));
                address.setWard(rs.getString("ward"));
                address.setAddressDetail(rs.getString("address_detail"));
                address.setDefault(rs.getBoolean("is_default"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    address.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    address.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                return address;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get address by ID
     */
    public static DeliveryAddress getAddressById(int addressId) {
        String sql = "SELECT id, user_id, recipient_name, phone_number, province, district, " +
                    "ward, address_detail, is_default, created_at, updated_at " +
                    "FROM delivery_addresses WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, addressId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                DeliveryAddress address = new DeliveryAddress();
                address.setId(rs.getInt("id"));
                address.setUserId(rs.getInt("user_id"));
                address.setRecipientName(rs.getString("recipient_name"));
                address.setPhoneNumber(rs.getString("phone_number"));
                address.setProvince(rs.getString("province"));
                address.setDistrict(rs.getString("district"));
                address.setWard(rs.getString("ward"));
                address.setAddressDetail(rs.getString("address_detail"));
                address.setDefault(rs.getBoolean("is_default"));
                
                // Convert Timestamp to LocalDateTime
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                if (createdAtTs != null) {
                    address.setCreatedAt(createdAtTs.toLocalDateTime());
                }
                
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");
                if (updatedAtTs != null) {
                    address.setUpdatedAt(updatedAtTs.toLocalDateTime());
                }
                
                return address;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Add new delivery address
     */
    public static int addAddress(DeliveryAddress address) {
        String sql = "INSERT INTO delivery_addresses (user_id, recipient_name, phone_number, " +
                    "province, district, ward, address_detail, is_default) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // If this is set as default, remove default from other addresses
            if (address.isDefault()) {
                removeDefaultAddress(address.getUserId());
            }
            
            stmt.setInt(1, address.getUserId());
            stmt.setString(2, address.getRecipientName());
            stmt.setString(3, address.getPhoneNumber());
            stmt.setString(4, address.getProvince());
            stmt.setString(5, address.getDistrict());
            stmt.setString(6, address.getWard());
            stmt.setString(7, address.getAddressDetail());
            stmt.setBoolean(8, address.isDefault());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Update existing delivery address
     */
    public static boolean updateAddress(DeliveryAddress address) {
        String sql = "UPDATE delivery_addresses SET recipient_name = ?, phone_number = ?, " +
                    "province = ?, district = ?, ward = ?, address_detail = ?, is_default = ?, " +
                    "updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // If this is set as default, remove default from other addresses
            if (address.isDefault()) {
                removeDefaultAddress(address.getUserId());
            }
            
            stmt.setString(1, address.getRecipientName());
            stmt.setString(2, address.getPhoneNumber());
            stmt.setString(3, address.getProvince());
            stmt.setString(4, address.getDistrict());
            stmt.setString(5, address.getWard());
            stmt.setString(6, address.getAddressDetail());
            stmt.setBoolean(7, address.isDefault());
            stmt.setInt(8, address.getId());
            stmt.setInt(9, address.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete delivery address
     */
    public static boolean deleteAddress(int addressId, int userId) {
        String sql = "DELETE FROM delivery_addresses WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Set an address as default
     */
    public static boolean setDefaultAddress(int userId, int addressId) {
        // First, remove default from all addresses
        removeDefaultAddress(userId);
        
        // Then set the new default
        String sql = "UPDATE delivery_addresses SET is_default = true, " +
                    "updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Remove default flag from all addresses for a user
     */
    private static void removeDefaultAddress(int userId) {
        String sql = "UPDATE delivery_addresses SET is_default = false WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
