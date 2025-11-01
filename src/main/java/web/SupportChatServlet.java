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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
        String fullPath = req.getRequestURI();
        if (fullPath != null && fullPath.contains("/api/admin/support-chat")) {
            handleAdminRoutes(req, resp);
            return;
        }
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "SupportChatServlet", urlPatterns = {"/api/support-chat"})
public class SupportChatServlet extends HttpServlet {

    private transient Gson gson;
    private transient SupportChatDAO supportChatDAO;

    private static final List<AutoReplyRule> AUTO_REPLY_RULES = createAutoReplyRules();

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
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                error("UNAUTHORIZED", "Báº¡n cáº§n Ä‘Äƒng nháº­p Ä‘á»ƒ sá»­ dá»¥ng há»— trá»£ chat."));
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
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                error("UNAUTHORIZED", "Báº¡n cáº§n Ä‘Äƒng nháº­p Ä‘á»ƒ gá»­i tin nháº¯n."));
            return;
        }

        JsonObject body = readJson(req);
        String content = body != null && body.has("content") ? body.get("content").getAsString() : null;
        if (content == null || content.trim().isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                error("INVALID_CONTENT", "Ná»™i dung tin nháº¯n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng."));
            return;
        }

        try {
            SupportConversation conversation = supportChatDAO.getOrCreateConversation(username);
            SupportMessage message = supportChatDAO.addUserMessage(conversation.getId(), content);

            SupportMessage autoMessage = null;
            String autoReply = detectAutoReply(content);
            if (autoReply != null) {
                autoMessage = supportChatDAO.addSupportMessage(conversation.getId(), autoReply);
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ok", true);
            payload.put("message", toMessageMap(message));
            if (autoMessage != null) {
                payload.put("autoReply", toMessageMap(autoMessage));
            }
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
        resp.setCharacterEncoding("UTF-8");
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

    private String detectAutoReply(String content) {
        if (content == null) {
            return null;
        }
        String normalized = normalizeText(content);
        for (AutoReplyRule rule : AUTO_REPLY_RULES) {
            if (rule.matches(normalized)) {
                return rule.getResponse();
            }
        }
        return null;
    }

    private static String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private static List<AutoReplyRule> createAutoReplyRules() {
        return Collections.unmodifiableList(Arrays.asList(
            new AutoReplyRule(
                new String[]{"xin chao", "chao ban", "hello", "hi"},
                "Xin chÃ o! Äá»™i há»— trá»£ Bookish Bliss Haven Ä‘ang láº¯ng nghe. Báº¡n vui lÃ²ng cho biáº¿t thÃªm thÃ´ng tin Ä‘á»ƒ chÃºng tÃ´i há»— trá»£ nhanh hÆ¡n nhÃ©."
            ),
            new AutoReplyRule(
                new String[]{"giao hang", "thoi gian giao", "ship bao lau", "vanchuyen mat bao nhieu"},
                "Thá»i gian giao hÃ ng ná»™i thÃ nh thÆ°á»ng tá»« 1-2 ngÃ y, cÃ¡c tá»‰nh thÃ nh khÃ¡c 2-5 ngÃ y lÃ m viá»‡c. ÄÆ¡n tá»« 500.000â‚« Ä‘Æ°á»£c miá»…n phÃ­ giao tiÃªu chuáº©n. Báº¡n cÃ³ thá»ƒ kiá»ƒm tra tiáº¿n Ä‘á»™ trong má»¥c ÄÆ¡n hÃ ng cá»§a tÃ´i."
            ),
            new AutoReplyRule(
                new String[]{"phi ship", "mien phi van chuyen", "phi van chuyen"},
                "Bookish Bliss Haven miá»…n phÃ­ giao hÃ ng tiÃªu chuáº©n cho Ä‘Æ¡n tá»« 500.000â‚«. Vá»›i Ä‘Æ¡n nhá» hÆ¡n, phÃ­ sáº½ hiá»ƒn thá»‹ rÃµ trÆ°á»›c bÆ°á»›c thanh toÃ¡n tÃ¹y theo Ä‘á»‹a chá»‰ giao."
            ),
            new AutoReplyRule(
                new String[]{"doi tra", "doi sach", "hoan tien", "tra hang"},
                "Báº¡n cÃ³ thá»ƒ Ä‘á»•i hoáº·c tráº£ sÃ¡ch trong vÃ²ng 7 ngÃ y náº¿u sáº£n pháº©m bá»‹ lá»—i in áº¥n hoáº·c giao sai. Vui lÃ²ng giá»¯ hÃ³a Ä‘Æ¡n/biÃªn nháº­n vÃ  cung cáº¥p áº£nh sáº£n pháº©m Ä‘á»ƒ há»— trá»£ xá»­ lÃ½ nhanh nhÃ©."
            ),
            new AutoReplyRule(
                new String[]{"thu tuc thanh toan", "thanh toan", "payment", "tra gop"},
                "Hiá»‡n chÃºng tÃ´i há»— trá»£ thanh toÃ¡n khi nháº­n hÃ ng (COD) vÃ  cÃ¡c phÆ°Æ¡ng thá»©c trá»±c tuyáº¿n nhÆ° VNPay, Momo. Báº¡n chá»n tÃ¹y chá»n phÃ¹ há»£p á»Ÿ bÆ°á»›c thanh toÃ¡n."
            ),
            new AutoReplyRule(
                new String[]{"gio hoat dong", "lien he", "ho tro khi nao", "bao gio tra loi"},
                "Äá»™i há»— trá»£ lÃ m viá»‡c tá»« 8h00-20h00 (T2-T6) vÃ  9h00-17h00 (T7-CN). NgoÃ i khung giá» nÃ y báº¡n váº«n cÃ³ thá»ƒ Ä‘á»ƒ láº¡i tin nháº¯n, chÃºng tÃ´i sáº½ pháº£n há»“i sá»›m nháº¥t cÃ³ thá»ƒ."
            )
        ));
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
            Object user = session.getAttribute("username");
            if (user != null) {
                String value = user.toString().trim();
                if (!value.isEmpty()) {
                    return normalizeUser(value);
                }
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
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeUser(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }

        String sqlByUsername = "SELECT username FROM users WHERE username = ? LIMIT 1";
        String sqlById = "SELECT username FROM users WHERE CAST(id AS TEXT) = ? LIMIT 1";
        String sqlByEmail = "SELECT username FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";

        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlByUsername)) {
                ps.setString(1, subject);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlById)) {
                ps.setString(1, subject);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlByEmail)) {
                ps.setString(1, subject);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return subject;
    }

    private static class AutoReplyRule {
        private final String[] keywords;
        private final String response;

        private AutoReplyRule(String[] keywords, String response) {
            this.keywords = keywords;
            this.response = response;
        }

        private boolean matches(String normalizedContent) {
            if (normalizedContent == null || normalizedContent.isEmpty()) {
                return false;
            }
            for (String keyword : keywords) {
                if (normalizedContent.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }

        private String getResponse() {
            return response;
        }
    }
}
