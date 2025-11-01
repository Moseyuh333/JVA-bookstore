package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.SupportChatDAO;
import models.SupportConversation;
import models.SupportMessage;
import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "SupportChatServlet", urlPatterns = {"/api/support-chat"})
public class SupportChatServlet extends HttpServlet {

    private transient Gson gson;
    private transient SupportChatDAO supportChatDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.gson = new Gson();
        this.supportChatDAO = new SupportChatDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = currentUsername(req);
        if (username == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, error("UNAUTHORIZED", "Bạn cần đăng nhập để sử dụng hỗ trợ chat."));
            return;
        }

        Timestamp since = parseSince(req.getParameter("since"));
        try {
            SupportConversation conversation = supportChatDAO.getOrCreateConversation(username);
            List<SupportMessage> messages = supportChatDAO.listMessages(conversation.getId(), since, 50);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ok", true);
            payload.put("conversation", toConversationMap(conversation));
            payload.put("messages", toMessageList(messages));
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException ex) {
            ex.printStackTrace();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("SERVER_ERROR", ex.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = currentUsername(req);
        if (username == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, error("UNAUTHORIZED", "Bạn cần đăng nhập để gửi tin nhắn."));
            return;
        }

        JsonObject body = readJson(req);
        String content = body != null && body.has("content") ? body.get("content").getAsString() : null;
        if (content == null || content.trim().isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, error("INVALID_CONTENT", "Nội dung tin nhắn không được để trống."));
            return;
        }

        try {
            SupportConversation conversation = supportChatDAO.getOrCreateConversation(username);
            SupportMessage message = supportChatDAO.addUserMessage(conversation.getId(), content);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ok", true);
            payload.put("message", toMessageMap(message));
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException ex) {
            ex.printStackTrace();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("SERVER_ERROR", ex.getMessage()));
        }
    }

    private Timestamp parseSince(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            long epochMillis = Long.parseLong(raw.trim());
            return new Timestamp(epochMillis);
        } catch (NumberFormatException ignored) {
        }
        try {
            Instant instant = Instant.parse(raw.trim());
            return Timestamp.from(instant);
        } catch (Exception ignored) {
        }
        return null;
    }

    private Map<String, Object> toConversationMap(SupportConversation conversation) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", conversation.getId());
        map.put("status", conversation.getStatus());
        map.put("createdAt", toIso(conversation.getCreatedAt()));
        map.put("updatedAt", toIso(conversation.getUpdatedAt()));
        return map;
    }

    private List<Map<String, Object>> toMessageList(List<SupportMessage> messages) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SupportMessage message : messages) {
            list.add(toMessageMap(message));
        }
        return list;
    }

    private Map<String, Object> toMessageMap(SupportMessage message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", message.getId());
        map.put("conversationId", message.getConversationId());
        map.put("senderType", message.getSenderType());
        map.put("content", message.getContent());
        map.put("createdAt", toIso(message.getCreatedAt()));
        return map;
    }

    private String toIso(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().toString();
    }

    private JsonObject readJson(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return new JsonObject();
        }
        try {
            return JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception ex) {
            return new JsonObject();
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Map<String, ?> payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(payload));
        }
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ok", false);
        map.put("error", code);
        map.put("message", message);
        return map;
    }

    private String currentUsername(HttpServletRequest req) {
        Object attr = req.getAttribute("username");
        if (attr != null) {
            String value = attr.toString().trim();
            if (!value.isEmpty()) {
                return normalizeUser(value);
            }
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object sessionUser = session.getAttribute("username");
            if (sessionUser != null && !sessionUser.toString().trim().isEmpty()) {
                return normalizeUser(sessionUser.toString().trim());
            }
        }

        String token = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equalsIgnoreCase(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null || token.isEmpty()) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7).trim();
            }
        }
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            String subject = JwtUtil.validateToken(token);
            if (subject == null || subject.trim().isEmpty()) {
                return null;
            }
            return normalizeUser(subject.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeUser(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }
        String sqlUsername = "SELECT username FROM users WHERE username = ? LIMIT 1";
        String sqlId = "SELECT username FROM users WHERE CAST(id AS TEXT) = ? LIMIT 1";
        String sqlEmail = "SELECT username FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";

        try (java.sql.Connection con = DBUtil.getConnection()) {
            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlUsername)) {
                ps.setString(1, subject);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlId)) {
                ps.setString(1, subject);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlEmail)) {
                ps.setString(1, subject);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return subject;
    }
}
