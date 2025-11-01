package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.SupportChatDAO;
import models.SupportConversation;
import models.SupportMessage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (!isAuthenticated(req, resp)) {
            return;
        }

        String username = getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "UNAUTHORIZED");
            error.put("message", "User not authenticated");
            resp.getWriter().write(gson.toJson(error));
            return;
        }

        Timestamp since = parseSince(req.getParameter("since"));
        try {
            SupportConversation conversation = supportChatDAO.getOrCreateConversation(username);
            List<SupportMessage> messages = supportChatDAO.listMessages(conversation.getId(), since, 100);

            List<Map<String, Object>> messageData = new ArrayList<>();
            for (SupportMessage message : messages) {
                messageData.add(toMessageMap(message));
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ok", true);
            payload.put("conversation", toConversationMap(conversation));
            payload.put("messages", messageData);
            resp.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "SERVER_ERROR");
            error.put("message", ex.getMessage());
            resp.getWriter().write(gson.toJson(error));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (!isAuthenticated(req, resp)) {
            return;
        }

        String username = getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"ok\":false,\"error\":\"UNAUTHORIZED\",\"message\":\"User not authenticated\"}");
            return;
        }

        JsonObject body = readJsonBody(req);
        String content = body != null && body.has("content") ? body.get("content").getAsString() : null;
        if (content == null || content.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"ok\":false,\"error\":\"BAD_REQUEST\",\"message\":\"Content is required\"}");
            return;
        }

        try {
            SupportConversation conversation = supportChatDAO.getOrCreateConversation(username);
            SupportMessage message = supportChatDAO.addUserMessage(conversation.getId(), content);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ok", true);
            payload.put("message", toMessageMap(message));
            resp.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "SERVER_ERROR");
            error.put("message", ex.getMessage());
            resp.getWriter().write(gson.toJson(error));
        }
    }

    private boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "UNAUTHORIZED");
            error.put("message", "Missing or invalid token");
            resp.getWriter().write(gson.toJson(error));
            return false;
        }
        // Note: Actual token validation should be implemented here
        // For now, assume token is valid if present
        return true;
    }

    private String getUsername(HttpServletRequest req) {
        // Extract username from session
        HttpSession session = req.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("username");
        }
        return null;
    }

    private JsonObject readJsonBody(HttpServletRequest req) throws IOException {
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
        map.put("userId", conversation.getUserId());
        map.put("status", conversation.getStatus());
        map.put("createdAt", timestampToIso(conversation.getCreatedAt()));
        map.put("updatedAt", timestampToIso(conversation.getUpdatedAt()));
        return map;
    }

    private Map<String, Object> toMessageMap(SupportMessage message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", message.getId());
        map.put("conversationId", message.getConversationId());
        map.put("senderType", message.getSenderType());
        map.put("content", message.getContent());
        map.put("createdAt", timestampToIso(message.getCreatedAt()));
        return map;
    }

    private String timestampToIso(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().toString();
    }
}
