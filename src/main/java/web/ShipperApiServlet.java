package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.OrderDAO;
import dao.ShipmentDAO;
import models.Shipment;
import models.ShipmentEvent;
import utils.DBUtil;
import utils.FileStorageUtil;
import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "ShipperApiServlet", urlPatterns = {"/api/shipper/*"})
@MultipartConfig(
        fileSizeThreshold = 256 * 1024,
        maxFileSize = 6L * 1024 * 1024,
        maxRequestSize = 7L * 1024 * 1024
)
public class ShipperApiServlet extends HttpServlet {

    private transient Gson gson;
    private transient ShipmentDAO shipmentDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.gson = new Gson();
        this.shipmentDAO = new ShipmentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = currentUsername(req);
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid login")); return; }

        // Kiểm tra status user trước khi cho phép truy cập shipper API
        try {
            if (!isUserAllowedToShip(req)) {
                writeJson(resp, 403, err("FORBIDDEN", "Tài khoản của bạn đã bị hạn chế quyền giao hàng"));
                return;
            }
        } catch (SQLException ex) {
            writeJson(resp, 500, err("SERVER_ERROR", ex.getMessage()));
            return;
        }

        String path = normalizedPath(req);

        try {
            if (path.equals("")) {
                writeJson(resp, 200, mapOf("ok", true, "service", "shipper-api"));
                return;
            }

            if (path.equals("/whoami")) {
                writeJson(resp, 200, mapOf("username", user));
                return;
            }

            // ===================== NEW: LẤY THÔNG TIN USER TỪ BẢNG users ======================
            // GET /api/shipper/profile  -> {username, email, fullName, phone}
            if (path.equals("/profile")) {
                Map<String, Object> profile = fetchUserProfile(user);
                if (profile == null) {
                    writeJson(resp, 404, err("NOT_FOUND", "User not found"));
                } else {
                    writeJson(resp, 200, profile);
                }
                return;
            }
            // ===================================================================================

            if (path.equals("/shipments")) {
                String raw = opt(req.getParameter("status"));
                String status = normalizeStatusForDb(raw);
                String q = opt(req.getParameter("q"));
                int page = clamp(parseInt(req.getParameter("page"), 1), 1, 1000000);
                int size = clamp(parseInt(req.getParameter("size"), 20), 1, 100);

                List<Shipment> items = shipmentDAO.findByShipper(user, status == null ? "" : status, q, page, size);
                int total = countByShipper(user, status == null ? "" : status, q);

                Map<String, Object> out = new LinkedHashMap<String, Object>();
                out.put("items", items);
                out.put("page", page);
                out.put("size", size);
                out.put("total", total);
                writeJson(resp, 200, out);
                return;
            }

            if (path.startsWith("/shipments/")) {
                String[] seg = path.split("/");
                // /shipments/{id}
                if (seg.length == 3) {
                    long id = Long.parseLong(seg[2]);
                    Shipment s = shipmentDAO.findByIdOwned(id, user);
                    if (s == null) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }
                    List<ShipmentEvent> events = shipmentDAO.findEvents(id);
                    Map<String, Object> out = new LinkedHashMap<String, Object>();
                    out.put("shipment", s);
                    out.put("events", events);
                    writeJson(resp, 200, out);
                    return;
                }

                // GET /shipments/{id}/events  -> trả về mảng events thuần
                if (seg.length == 4 && "events".equals(seg[3])) {
                    long id = Long.parseLong(seg[2]);
                    Shipment s = shipmentDAO.findByIdOwned(id, user);
                    if (s == null) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }
                    List<ShipmentEvent> events = shipmentDAO.findEvents(id);
                    writeJson(resp, 200, events); // array
                    return;
                }
            }

            if (path.equals("/stats")) {
                Map<String, Integer> st = shipmentDAO.getStats(user);
                Map<String, Object> out = new LinkedHashMap<String, Object>();
                int inProgress = nz(st.get("inProgress"));
                int delivered  = nz(st.get("delivered"));
                int failed     = nz(st.get("failed"));
                out.put("inProgress", inProgress);
                out.put("delivered", delivered);
                out.put("failed", failed);

                double rate = (delivered + failed) == 0 ? 0.0 : (double) delivered / (double) (delivered + failed);
                out.put("successRate", rate);
                out.put("raw", st);
                writeJson(resp, 200, out);
                return;
            }

            writeJson(resp, 404, err("NOT_FOUND", "Unknown endpoint: " + path));
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp, 500, err("SERVER_ERROR", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = currentUsername(req);
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid login")); return; }

        String path = normalizedPath(req);

        try {
            // POST /shipments/{id}/events  -> ghi event + (DAO sẽ cập nhật luôn shipments.status)
            if (path.startsWith("/shipments/") && path.endsWith("/events")) {
                String[] seg = path.split("/");
                if (seg.length == 4) {
                    long id = Long.parseLong(seg[2]);

                    Shipment s = shipmentDAO.findByIdOwned(id, user);
                    if (s == null) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }

                    Long orderId = s.getOrderId();
                    String orderStatus = orderId == null ? null : OrderDAO.getOrderStatus(orderId);
                    if (!allowsShipmentEvent(orderStatus)) {
                        writeJson(resp, 409, err("INVALID_STATE", "Đơn hàng chưa sẵn sàng để cập nhật giao nhận"));
                        return;
                    }

                    JsonObject body = readJson(req);
                    String rawStatus  = opt(getString(body, "status"));
                    String status     = normalizeStatusForDb(rawStatus);  // <<— CHUẨN HOÁ Ở ĐÂY
                    String note       = opt(getString(body, "note"));
                    String evidenceUrl= opt(getString(body, "evidenceUrl"));

                    if (status == null || status.isEmpty()) {
                        writeJson(resp, 400, err("BAD_REQUEST", "status is required or invalid"));
                        return;
                    }

                    shipmentDAO.addEvent(id, status, note, evidenceUrl, user);
                    writeJson(resp, 200, mapOf("ok", true, "status", status));
                    return;
                }
            }

            // POST /shipments/{id}/deliver  -> đánh dấu delivered
            if (path.startsWith("/shipments/") && path.endsWith("/deliver")) {
                String[] seg = path.split("/");
                if (seg.length == 4) {
                    long id = Long.parseLong(seg[2]);

                    Shipment s = shipmentDAO.findByIdOwned(id, user);
                    if (s == null) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }

                    Long orderId = s.getOrderId();
                    String orderStatus = orderId == null ? null : OrderDAO.getOrderStatus(orderId);
                    if (!allowsDeliveryConfirmation(orderStatus)) {
                        writeJson(resp, 409, err("INVALID_STATE", "Đơn hàng chưa được bật trạng thái giao hàng"));
                        return;
                    }

                    String contentType = req.getContentType();
                    boolean isMultipart = contentType != null
                        && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");

                    boolean codCollected;
                    String evidenceUrl;
                    String note;

                    if (isMultipart) {
                        Part evidencePart;
                        try {
                            evidencePart = req.getPart("evidence");
                        } catch (ServletException se) {
                            writeJson(resp, 400, err("INVALID_MEDIA", "Không thể đọc tệp minh chứng"));
                            return;
                        }
                        if (evidencePart == null || evidencePart.getSize() <= 0) {
                            writeJson(resp, 400, err("BAD_REQUEST", "Ảnh minh chứng là bắt buộc"));
                            return;
                        }
                        try {
                            FileStorageUtil.StoredFile stored = FileStorageUtil.storeShipmentEvidence(evidencePart);
                            evidenceUrl = stored.getPublicUrl();
                        } catch (IOException io) {
                            writeJson(resp, 400, err("INVALID_MEDIA", io.getMessage()));
                            return;
                        }
                        codCollected = parseBoolean(req.getParameter("codCollected"));
                        note = opt(req.getParameter("note"));
                    } else {
                        JsonObject body = readJson(req);
                        codCollected = getBoolean(body, "codCollected", false);
                        evidenceUrl = opt(getString(body, "evidenceUrl"));
                        note = opt(getString(body, "note"));
                    }

                    if (evidenceUrl.isEmpty()) {
                        writeJson(resp, 400, err("BAD_REQUEST", "Ảnh minh chứng là bắt buộc"));
                        return;
                    }
                    if (s.getCodAmount() > 0 && !codCollected) {
                        writeJson(resp, 400, err("BAD_REQUEST", "COD shipment requires codCollected=true"));
                        return;
                    }

                    shipmentDAO.markDelivered(id, codCollected, evidenceUrl, note, user);
                    writeJson(resp, 200, mapOf("ok", true, "status", "DELIVERED"));
                    return;
                }
            }

            writeJson(resp, 404, err("NOT_FOUND", "Unknown endpoint: " + path));
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp, 500, err("SERVER_ERROR", e.getMessage()));
        }
    }



    // -------------------- helpers --------------------
    private String currentUsername(HttpServletRequest req) {
        // 1) filter/session
        Object f = req.getAttribute("username");
        if (f != null && !String.valueOf(f).trim().isEmpty()) return normalizeUser(String.valueOf(f).trim());

        HttpSession sess = req.getSession(false);
        if (sess != null) {
            Object u = sess.getAttribute("username");
            if (u != null && !String.valueOf(u).trim().isEmpty()) return normalizeUser(String.valueOf(u).trim());
        }

        // 2) cookie/header token
        String token = null;
        Cookie[] cs = req.getCookies();
        if (cs != null) {
            for (Cookie c : cs) {
                if ("token".equalsIgnoreCase(c.getName())) { token = c.getValue(); break; }
            }
        }
        if (token == null || token.isEmpty()) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7).trim();
        }
        if (token == null || token.isEmpty()) return null;

        try {
            String sub = JwtUtil.validateToken(token);
            if (sub == null || sub.isEmpty()) return null;
            return normalizeUser(sub.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeUser(String subject) {
        String sqlU = "SELECT username FROM users WHERE username = ? LIMIT 1";
        String sqlI = "SELECT username FROM users WHERE CAST(id AS TEXT) = ? LIMIT 1";
        java.sql.Connection con = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            con = utils.DBUtil.getConnection();

            ps = con.prepareStatement(sqlU);
            ps.setString(1, subject);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
            try { rs.close(); } catch (Exception ignore) {}
            try { ps.close(); } catch (Exception ignore) {}

            ps = con.prepareStatement(sqlI);
            ps.setString(1, subject);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);

            return subject;
        } catch (Exception e) {
            return subject;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (con != null) con.close(); } catch (Exception ignore) {}
        }
    }

    private String normalizedPath(HttpServletRequest req) {
        String p = req.getPathInfo();
        if (p == null) return "";
        p = p.replaceAll("/{2,}", "/");
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p.trim();
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        try { out.print(gson.toJson(body)); }
        finally { out.flush(); }
    }

    private Map<String, Object> err(String code, String msg) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("error", code);
        m.put("message", msg);
        return m;
    }

    private Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private JsonObject readJson(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = req.getReader();
        try {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } finally { br.close(); }
        if (sb.length() == 0) return new JsonObject();
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    private String opt(String s) { return s == null ? "" : s.trim(); }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private String getString(JsonObject o, String key) {
        return (o.has(key) && !o.get(key).isJsonNull()) ? o.get(key).getAsString() : null;
    }
    private boolean getBoolean(JsonObject o, String key, boolean def) {
        try { return (o.has(key) && !o.get(key).isJsonNull()) ? o.get(key).getAsBoolean() : def; }
        catch (Exception e){ return def; }
    }

    private boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
            || "1".equals(normalized)
            || "yes".equals(normalized)
            || "y".equals(normalized)
            || "on".equals(normalized);
    }
    private int nz(Integer x){ return x==null?0:x; }

    private boolean allowsShipmentEvent(String orderStatus) {
        if (orderStatus == null) {
            return false;
        }
        String normalized = orderStatus.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("confirmed")
                || normalized.equals("shipping")
                || normalized.equals("delivered")
                || normalized.equals("failed")
                || normalized.equals("returned");
    }

    private boolean allowsDeliveryConfirmation(String orderStatus) {
        if (orderStatus == null) {
            return false;
        }
        String normalized = orderStatus.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("shipping");
    }

    private String normalizeStatusForDb(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || "all".equalsIgnoreCase(s)) return null;

        String lower = s.toLowerCase();
        if ("pending".equals(lower)) return "ASSIGNED";
        if ("delivering".equals(lower)) return "OUT_FOR_DELIVERY";
        if ("done".equals(lower)) return "DELIVERED";
        if ("failed".equals(lower)) return "FAILED_DELIVERY";

        List<String> valid = Arrays.asList("ASSIGNED","PICKED_UP","OUT_FOR_DELIVERY","DELIVERED","FAILED_DELIVERY","CANCELLED","RETURNING","RETURNED");
        String up = s.toUpperCase();
        return valid.contains(up) ? up : null;
    }

    private int countByShipper(String username, String status, String q) throws SQLException {
        String base = "SELECT COUNT(*) FROM shipments s LEFT JOIN orders o ON o.id = s.order_id WHERE s.shipper_user_id = ? ";
        StringBuilder sql = new StringBuilder(base);
        if (status != null && !status.isEmpty()) sql.append("AND s.status=? ");
        if (q != null && !q.isEmpty()) {
            sql.append("AND (LOWER(s.id::text) LIKE LOWER(?) OR LOWER(o.code) LIKE LOWER(?) OR LOWER(o.shipping_snapshot->>'recipientName') LIKE LOWER(?)) ");
        }

        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(sql.toString());
            int i = 1;
            ps.setString(i++, username);
            if (status != null && !status.isEmpty()) ps.setString(i++, status);
            if (q != null && !q.isEmpty()) {
                String like = "%" + q + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
    }

    // ============== PRIVATE: query profile từ bảng users (không cần sửa DAO) ==============
    private Map<String, Object> fetchUserProfile(String ident) {
        if (ident == null || ident.trim().isEmpty()) return null;

        String sql =
            "SELECT username, email, COALESCE(NULLIF(full_name,''), username) AS full_name, phone " +
            "FROM users WHERE LOWER(username)=LOWER(?) OR LOWER(email)=LOWER(?) LIMIT 1";

        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, ident.trim());
            ps.setString(2, ident.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("username", rs.getString("username"));
                m.put("email", rs.getString("email"));
                m.put("fullName", rs.getString("full_name"));
                m.put("phone", rs.getString("phone"));
                return m;
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (con != null) con.close(); } catch (Exception ignore) {}
        }
    }
    // =======================================================================================

    private boolean isUserAllowedToShip(HttpServletRequest request) throws SQLException {
        String user = currentUsername(request);
        if (user == null) {
            return false; // Không đăng nhập
        }

        String status = DBUtil.getUserStatus(user);
        if (status == null) {
            return true; // Mặc định cho phép nếu không có status
        }

        String normalizedStatus = status.trim().toLowerCase();
        return !"banned".equals(normalizedStatus) && !"inactive".equals(normalizedStatus);
    }

    private String getUserEmail(HttpServletRequest request) {
        // Lấy email từ session hoặc attribute
        Object emailAttr = request.getAttribute("email");
        if (emailAttr != null && !String.valueOf(emailAttr).trim().isEmpty()) {
            return String.valueOf(emailAttr).trim();
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object emailSess = session.getAttribute("email");
            if (emailSess != null && !String.valueOf(emailSess).trim().isEmpty()) {
                return String.valueOf(emailSess).trim();
            }
        }

        // Nếu không có trong session, lấy từ token JWT
        String token = null;
        Cookie[] cs = request.getCookies();
        if (cs != null) {
            for (Cookie c : cs) {
                if ("token".equalsIgnoreCase(c.getName())) { token = c.getValue(); break; }
            }
        }
        if (token == null || token.isEmpty()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7).trim();
        }
        if (token != null && !token.isEmpty()) {
            try {
                // Lấy username từ token, sau đó query email từ DB
                String username = JwtUtil.validateToken(token);
                if (username != null) {
                    return getEmailFromUsername(username);
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private String getUsernameFromEmail(String email) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return null;
    }

    private String getEmailFromUsername(String username) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT email FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }
}
