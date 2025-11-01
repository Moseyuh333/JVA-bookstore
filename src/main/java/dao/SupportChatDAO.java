package dao;

import models.SupportConversation;
import models.SupportMessage;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupportChatDAO {

    private static final String WELCOME_MESSAGE =
        "Xin chào! Đội Bookish Bliss Haven đã nhận được yêu cầu của bạn và sẽ phản hồi sớm nhất có thể.";

    public SupportConversation getOrCreateConversation(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new SQLException("Missing username for support conversation");
        }

        Connection con = null;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false);

            int userId = DBUtil.getUserIdByUsername(username);
            if (userId <= 0) {
                throw new SQLException("User not found: " + username);
            }

            SupportConversation conversation = findByUserId(con, userId);
            if (conversation == null) {
                conversation = createConversation(con, userId);
                insertSupportMessage(con, conversation.getId(), WELCOME_MESSAGE);
                conversation = findById(con, conversation.getId());
            }

            con.commit();
            return conversation;
        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (Exception ignore) {}
            }
            throw ex;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
                try { con.close(); } catch (Exception ignore) {}
            }
        }
    }

    public List<SupportMessage> listMessages(long conversationId, Timestamp since, int limit) throws SQLException {
        List<SupportMessage> messages = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, conversation_id, sender_type, content, created_at " +
            "FROM support_messages WHERE conversation_id = ?");
        if (since != null) {
            sql.append(" AND created_at > ?");
        }
        sql.append(" ORDER BY created_at ASC");
        if (limit > 0) {
            sql.append(" LIMIT ?");
        }

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, conversationId);
            if (since != null) {
                ps.setTimestamp(idx++, since);
            }
            if (limit > 0) {
                ps.setInt(idx, limit);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        }
        return messages;
    }

    public SupportMessage addUserMessage(long conversationId, String content) throws SQLException {
        return insertMessage(conversationId, "user", content);
    }

    public SupportMessage addSupportMessage(long conversationId, String content) throws SQLException {
        return insertMessage(conversationId, "support", content);
    }

    private SupportConversation findByUserId(Connection con, int userId) throws SQLException {
        String sql = "SELECT id, user_id, status, created_at, updated_at " +
                     "FROM support_conversations WHERE user_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConversation(rs);
                }
            }
        }
        return null;
    }

    private SupportConversation findById(Connection con, long id) throws SQLException {
        String sql = "SELECT id, user_id, status, created_at, updated_at FROM support_conversations WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConversation(rs);
                }
            }
        }
        return null;
    }

    private SupportConversation createConversation(Connection con, int userId) throws SQLException {
        String sql = "INSERT INTO support_conversations (user_id) VALUES (?) RETURNING id, user_id, status, created_at, updated_at";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConversation(rs);
                }
            }
        }
        throw new SQLException("Failed to create support conversation for user " + userId);
    }

    private void insertSupportMessage(Connection con, long conversationId, String content) throws SQLException {
        insertMessage(con, conversationId, "support", content);
    }

    private SupportMessage insertMessage(Connection con, long conversationId, String senderType, String content) throws SQLException {
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isEmpty()) {
            throw new SQLException("Message content cannot be empty");
        }
        String sql = "INSERT INTO support_messages (conversation_id, sender_type, content) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, conversationId);
            ps.setString(2, senderType);
            ps.setString(3, normalizedContent);
            ps.executeUpdate();

            updateConversationTimestamp(con, conversationId);

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return findMessageById(con, id);
                }
            }
        }
        throw new SQLException("Unable to insert support message");
    }

    private SupportMessage insertMessage(long conversationId, String senderType, String content) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                SupportMessage msg = insertMessage(con, conversationId, senderType, content);
                con.commit();
                return msg;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private SupportMessage findMessageById(Connection con, long id) throws SQLException {
        String sql = "SELECT id, conversation_id, sender_type, content, created_at FROM support_messages WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMessage(rs);
                }
            }
        }
        return null;
    }

    private void updateConversationTimestamp(Connection con, long conversationId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
            "UPDATE support_conversations SET updated_at = NOW() WHERE id = ?")) {
            ps.setLong(1, conversationId);
            ps.executeUpdate();
        }
    }

    private SupportConversation mapConversation(ResultSet rs) throws SQLException {
        SupportConversation c = new SupportConversation();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        return c;
    }

    private SupportMessage mapMessage(ResultSet rs) throws SQLException {
        SupportMessage m = new SupportMessage();
        m.setId(rs.getLong("id"));
        m.setConversationId(rs.getLong("conversation_id"));
        m.setSenderType(rs.getString("sender_type"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
    }
}
