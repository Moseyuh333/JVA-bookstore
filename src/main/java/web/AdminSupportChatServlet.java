package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.SupportChatDAO;
import models.SupportConversationSummary;
import models.SupportMessage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "AdminSupportChatServlet", urlPatterns = {"/api/admin/support-chat"})
public class AdminSupportChatServlet extends HttpServlet {

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
        if (!isAuthorized(req, resp)) {
            return;
        }

        String action = valueOrDefault(req.getParameter("action"), "conversations").toLowerCase(Locale.ROOT);
        try {
            switch (action) {
                case "messages":
                    handleMessages(req, resp);
                    break;
                case "conversations":
                default:
                    handleConversations(req, resp);
                    break;
            }
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
        if (!isAuthorized(req, resp)) {
            return;
        }

        JsonObject body = readJsonBody(req);
        String action = null;
        if (body != null && body.has("action")) {
            action = body.get("action").getAsString();
        }
        if (action == null) {
            action = valueOrDefault(req.getParameter("action"), "");
        }
        action = action.toLowerCase(Locale.ROOT);

        try {
            if ("reply".equals(action)) {
                handleReply(body, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("error", "BAD_REQUEST");
                error.put("message", "Unknown action: " + action);
                resp.getWriter().write(gson.toJson(error));
            }
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "SERVER_ERROR");
            error.put("message", ex.getMessage());
            resp.getWriter().write(gson.toJson(error));
        }
    }

    private void handleConversations(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        int limit = clampPositiveInt(req.getParameter("limit"), 20, 1, 200);
        int offset = clampPositiveInt(req.getParameter("offset"), 0, 0, 10000);

        List<SupportConversationSummary> conversations = supportChatDAO.listConversations(limit, offset);
        List<Map<String, Object>> data = new ArrayList<>();
        for (SupportConversationSummary summary : conversations) {
            data.add(toSummaryMap(summary));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("conversations", data);
        payload.put("limit", limit);
        payload.put("offset", offset);

        resp.getWriter().write(gson.toJson(payload));
    }

    private void handleMessages(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        long conversationId = parseLong(req.getParameter("conversationId"), -1L);
        if (conversationId <= 0L) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "BAD_REQUEST");
            error.put("message", "conversationId is required");
            resp.getWriter().write(gson.toJson(error));
            return;
        }

        Timestamp since = parseSince(req.getParameter("since"));
        int limit = clampPositiveInt(req.getParameter("limit"), 100, 1, 500);

        SupportConversationSummary summary = supportChatDAO.getConversationSummary(conversationId);
        if (summary == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", "NOT_FOUND");
            error.put("message", "Conversation not found");
            resp.getWriter().write(gson.toJson(error));
            return;
        }

        List<SupportMessage> messages = supportChatDAO.listMessages(conversationId, since, limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (SupportMessage message : messages) {
            data.add(toMessageMap(message));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("conversation", toSummaryMap(summary));
        payload.put("messages", data);
        resp.getWriter().write(gson.toJson(payload));
    }

    private void handleReply(JsonObject body, HttpServletResponse resp) throws IOException, SQLException {
        if (body == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"ok\":false,\"error\":\"BAD_REQUEST\",\"message\":\"Missing request body\"}");
            return;
        }
        long conversationId = body.has("conversationId") ? body.get("conversationId").getAsLong() : -1L;
        String content = body.has("content") ? body.get("content").getAsString() : null;
        if (conversationId <= 0L || content == null || content.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"ok\":false,\"error\":\"BAD_REQUEST\",\"message\":\"conversationId and content are required\"}");
            return;
        }

        SupportConversationSummary summary = supportChatDAO.getConversationSummary(conversationId);
        if (summary == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"ok\":false,\"error\":\"NOT_FOUND\",\"message\":\"Conversation not found\"}");
            return;
        }

        SupportMessage message = supportChatDAO.addSupportMessage(conversationId, content);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("message", toMessageMap(message));
        resp.getWriter().write(gson.toJson(payload));
    }

    private boolean isAuthorized(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isLocalhost(req)) {
            return true;
        }
        String expected = getAdminSecret();
        String paramSecret = trimToNull(req.getParameter("secret"));
        String headerSecret = trimToNull(req.getHeader("X-Admin-Secret"));
        if (expected.equals(paramSecret) || expected.equals(headerSecret)) {
            return true;
        }
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("error", "FORBIDDEN");
        error.put("message", "Forbidden");
        resp.getWriter().write(gson.toJson(error));
        return false;
    }

    private boolean isLocalhost(HttpServletRequest req) {
        String remote = req.getRemoteAddr();
        return "127.0.0.1".equals(remote) || "0:0:0:0:0:0:0:1".equals(remote) || "::1".equals(remote);
    }

    private String getAdminSecret() {
        String env = System.getenv("ADMIN_PANEL_SECRET");
        if (env != null) {
            env = env.trim();
            if (!env.isEmpty()) {
                return env;
            }
        }
        return "dev-secret-key-change-me";
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

    private Map<String, Object> toSummaryMap(SupportConversationSummary summary) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", summary.getId());
        map.put("userId", summary.getUserId());
        map.put("username", summary.getUsername());
        map.put("status", summary.getStatus());
        map.put("createdAt", timestampToIso(summary.getCreatedAt()));
        map.put("updatedAt", timestampToIso(summary.getUpdatedAt()));
        map.put("lastMessage", summary.getLastMessage());
        map.put("lastMessageAt", timestampToIso(summary.getLastMessageAt()));
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

    private int clampPositiveInt(String raw, int defaultValue, int min, int max) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < min) {
                return min;
            }
            if (value > max) {
                return max;
            }
            return value;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private long parseLong(String raw, long defaultValue) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


    private String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
