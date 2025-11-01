package dao;

import models.SupportConversation;
import models.SupportConversationSummary;
import models.SupportMessage;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SupportChatDAO {

    private static final String WELCOME_MESSAGE =
        "Xin chào! Đội Bookish Bliss Haven đã nhận được yêu cầu của bạn và sẽ phản hồi sớm nhất có thể.";

    public SupportChatDAO() {
        try {
            ensureSchema();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to initialize support chat schema", ex);
        }
    }

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
                try {
                    con.rollback();
                } catch (SQLException ignore) {
                    // ignore rollback failure
                }
            }
            throw ex;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignore) {
                    // ignore
                }
                try {
                    con.close();
                } catch (SQLException ignore) {
                    // ignore
                }
            }
        }
    }

    public SupportConversation getConversation(long conversationId) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            return findById(con, conversationId);
        }
    }

    public SupportConversationSummary getConversationSummary(long conversationId) throws SQLException {
        String sql =
            "SELECT sc.id, sc.user_id, sc.status, sc.created_at, sc.updated_at, " +
            "       COALESCE(u.username, '') AS username, " +
            "       (SELECT content FROM support_messages WHERE conversation_id = sc.id ORDER BY created_at DESC LIMIT 1) AS last_message, " +
            "       (SELECT created_at FROM support_messages WHERE conversation_id = sc.id ORDER BY created_at DESC LIMIT 1) AS last_message_at " +
            "FROM support_conversations sc " +
            "LEFT JOIN users u ON u.id = sc.user_id " +
            "WHERE sc.id = ? LIMIT 1";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConversationSummary(rs);
                }
            }
        }
        return null;
    }

    public List<SupportConversationSummary> listConversations(int limit, int offset) throws SQLException {
        if (limit <= 0) {
            limit = 20;
        }
        if (limit > 200) {
            limit = 200;
        }
        if (offset < 0) {
            offset = 0;
        }

        String sql =
            "SELECT sc.id, sc.user_id, sc.status, sc.created_at, sc.updated_at, " +
            "       COALESCE(u.username, '') AS username, " +
            "       (SELECT content FROM support_messages WHERE conversation_id = sc.id ORDER BY created_at DESC LIMIT 1) AS last_message, " +
            "       (SELECT created_at FROM support_messages WHERE conversation_id = sc.id ORDER BY created_at DESC LIMIT 1) AS last_message_at " +
            "FROM support_conversations sc " +
            "LEFT JOIN users u ON u.id = sc.user_id " +
            "ORDER BY sc.updated_at DESC, sc.id DESC " +
            "LIMIT ? OFFSET ?";

        List<SupportConversationSummary> results = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapConversationSummary(rs));
                }
            }
        }
        return results;
    }

    public List<SupportMessage> listMessages(long conversationId, Timestamp since, int limit) throws SQLException {
        List<SupportMessage> messages = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, conversation_id, sender_type, content, created_at FROM support_messages WHERE conversation_id = ?");
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
        String sql = "SELECT id, user_id, status, created_at, updated_at FROM support_conversations WHERE user_id = ? LIMIT 1";
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
                SupportMessage message = insertMessage(con, conversationId, senderType, content);
                con.commit();
                return message;
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

    private void ensureSchema() throws SQLException {
        String createConversations =
            "CREATE TABLE IF NOT EXISTS support_conversations (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'open', " +
                "created_at TIMESTAMP NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMP NOT NULL DEFAULT NOW()" +
            ")";
        String createConversationIndex =
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_support_conversations_user ON support_conversations(user_id)";
        String createMessages =
            "CREATE TABLE IF NOT EXISTS support_messages (" +
                "id SERIAL PRIMARY KEY, " +
                "conversation_id INTEGER NOT NULL REFERENCES support_conversations(id) ON DELETE CASCADE, " +
                "sender_type VARCHAR(20) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "created_at TIMESTAMP NOT NULL DEFAULT NOW()" +
            ")";
        String createMessageIndex =
            "CREATE INDEX IF NOT EXISTS idx_support_messages_conversation_created ON support_messages(conversation_id, created_at)";

        try (Connection con = DBUtil.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(createConversations);
            stmt.execute(createConversationIndex);
            stmt.execute(createMessages);
            stmt.execute(createMessageIndex);
        }
    }

    private SupportConversation mapConversation(ResultSet rs) throws SQLException {
        SupportConversation conversation = new SupportConversation();
        conversation.setId(rs.getLong("id"));
        conversation.setUserId(rs.getInt("user_id"));
        conversation.setStatus(rs.getString("status"));
        conversation.setCreatedAt(rs.getTimestamp("created_at"));
        conversation.setUpdatedAt(rs.getTimestamp("updated_at"));
        return conversation;
    }

    private SupportConversationSummary mapConversationSummary(ResultSet rs) throws SQLException {
        SupportConversationSummary summary = new SupportConversationSummary();
        summary.setId(rs.getLong("id"));
        summary.setUserId(rs.getInt("user_id"));
        summary.setUsername(rs.getString("username"));
        summary.setStatus(rs.getString("status"));
        summary.setCreatedAt(rs.getTimestamp("created_at"));
        summary.setUpdatedAt(rs.getTimestamp("updated_at"));
        summary.setLastMessage(rs.getString("last_message"));
        summary.setLastMessageAt(rs.getTimestamp("last_message_at"));
        return summary;
    }

    private SupportMessage mapMessage(ResultSet rs) throws SQLException {
        SupportMessage message = new SupportMessage();
        message.setId(rs.getLong("id"));
        message.setConversationId(rs.getLong("conversation_id"));
        message.setSenderType(rs.getString("sender_type"));
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getTimestamp("created_at"));
        return message;
    }
}
