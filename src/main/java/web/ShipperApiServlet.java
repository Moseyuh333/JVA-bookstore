package web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.ShipmentDAO;
import models.Shipment;
import models.ShipmentEvent;
import utils.DBUtil;
import utils.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;


@WebServlet(name = "ShipperApiServlet", urlPatterns = {"/api/shipper/*"})
public class ShipperApiServlet extends HttpServlet {

    private transient Gson gson;
    private transient ShipmentDAO shipmentDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.gson = new Gson();
        this.shipmentDAO = new ShipmentDAO();
    }

    // ========================= ROUTING =========================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = currentUsername(req);
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid login")); return; }

        String path = normalizedPath(req);

        try {
            // /api/shipper  or /api/shipper/
            if (path.equals("")) {
                writeJson(resp, 200, mapOf("ok", true, "service", "shipper-api"));
                return;
            }

            // /api/shipper/shipments
            if (path.equals("/shipments")) {
                String raw = opt(req.getParameter("status"));
                String status = normalizeStatusForDb(raw); // null => không filter
                int page = clamp(parseInt(req.getParameter("page"), 1), 1, 1_000_000);
                int size = clamp(parseInt(req.getParameter("size"), 20), 1, 100);

                List<Shipment> items = shipmentDAO.findByShipper(user, status == null ? "" : status, page, size);
                int total = countByShipper(user, status == null ? "" : status);

                Map<String, Object> out = new LinkedHashMap<>();
                out.put("items", items);
                out.put("page", page);
                out.put("size", size);
                out.put("total", total);
                writeJson(resp, 200, out);
                return;
            }

            // /api/shipper/shipments/{id}
            if (path.startsWith("/shipments/")) {
                String[] seg = path.split("/");
                if (seg.length == 3) {
                    long id = Long.parseLong(seg[2]);
                    Shipment s = shipmentDAO.findById(id);
                    if (s == null || !user.equals(s.getShipperUserId())) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }
                    List<ShipmentEvent> events = shipmentDAO.findEvents(id);
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("shipment", s);
                    out.put("events", events);
                    writeJson(resp, 200, out);
                    return;
                }
            }

            // /api/shipper/stats
            if (path.equals("/stats")) {
                Map<String, Integer> st = shipmentDAO.getStats(user);
                Map<String, Object> out = new LinkedHashMap<>();
                int inProgress = nz(st.get("inProgress"));
                int delivered  = nz(st.get("delivered"));
                int failed     = nz(st.get("failed"));
                out.put("inProgress", inProgress);
                out.put("delivered", delivered);
                out.put("failed", failed);

                double rate = (delivered + failed) == 0 ? 0.0 : (double) delivered / (delivered + failed);
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
            // /api/shipper/shipments/{id}/events
            if (path.startsWith("/shipments/") && path.endsWith("/events")) {
                String[] seg = path.split("/");
                if (seg.length == 4) {
                    long id = Long.parseLong(seg[2]);

                    Shipment s = shipmentDAO.findById(id);
                    if (s == null || !user.equals(s.getShipperUserId())) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }

                    JsonObject body = readJson(req);
                    String status = opt(getString(body, "status"));
                    String note = opt(getString(body, "note"));
                    String evidenceUrl = opt(getString(body, "evidenceUrl"));
                    if (status.isEmpty()) {
                        writeJson(resp, 400, err("BAD_REQUEST", "status is required"));
                        return;
                    }

                    shipmentDAO.addEvent(id, status, note, evidenceUrl, user);
                    writeJson(resp, 200, mapOf("ok", true));
                    return;
                }
            }

            writeJson(resp, 404, err("NOT_FOUND", "Unknown endpoint: " + path));
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp, 500, err("SERVER_ERROR", e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = currentUsername(req);
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid login")); return; }

        String path = normalizedPath(req);

        try {
            // /api/shipper/shipments/{id}/deliver
            if (path.startsWith("/shipments/") && path.endsWith("/deliver")) {
                String[] seg = path.split("/");
                if (seg.length == 4) {
                    long id = Long.parseLong(seg[2]);

                    Shipment s = shipmentDAO.findById(id);
                    if (s == null || !user.equals(s.getShipperUserId())) {
                        writeJson(resp, 404, err("NOT_FOUND", "Shipment not found or not yours"));
                        return;
                    }

                    JsonObject body = readJson(req);
                    boolean codCollected = getBoolean(body, "codCollected", false);
                    String evidenceUrl = opt(getString(body, "evidenceUrl"));
                    String note = opt(getString(body, "note"));

                    if (evidenceUrl.isEmpty()) {
                        writeJson(resp, 400, err("BAD_REQUEST", "evidenceUrl is required"));
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

    // ========================= HELPERS =========================

    /** Ưu tiên session → fallback JWT cookie/header. */
    private String currentUsername(HttpServletRequest req) {
        // 0) Filter có set sẵn?
        Object f = req.getAttribute("username");
        if (f != null && !String.valueOf(f).trim().isEmpty()) return String.valueOf(f).trim();

        // 1) HttpSession
        HttpSession sess = req.getSession(false);
        if (sess != null) {
            Object u = sess.getAttribute("username");
            if (u != null && !String.valueOf(u).trim().isEmpty()) return String.valueOf(u).trim();
        }

        // 2) JWT cookie/header
        String token = null;
        Cookie[] cs = req.getCookies();
        if (cs != null) {
            for (Cookie c : cs) if ("token".equalsIgnoreCase(c.getName())) { token = c.getValue(); break; }
        }
        if (token == null || token.isEmpty()) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7).trim();
        }
        if (token == null || token.isEmpty()) return null;

        try {
            String user = JwtUtil.validateToken(token);
            return (user != null && !user.isBlank()) ? user : null;
        } catch (Exception e) {
            return null;
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
        try (PrintWriter out = resp.getWriter()) { out.print(gson.toJson(body)); }
    }

    private Map<String, Object> err(String code, String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", code);
        m.put("message", msg);
        return m;
    }

    private Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private JsonObject readJson(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) { String line; while ((line = br.readLine()) != null) sb.append(line); }
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
    private int nz(Integer x){ return x==null?0:x; }


    private String normalizeStatusForDb(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || "all".equalsIgnoreCase(s)) return null;

        switch (s.toLowerCase()) {
            case "pending":    return "ASSIGNED";
            case "delivering": return "OUT_FOR_DELIVERY";
            case "done":       return "DELIVERED";
            case "failed":     return "FAILED_DELIVERY";
        }

        Set<String> valid = Set.of("ASSIGNED","PICKED_UP","IN_TRANSIT","OUT_FOR_DELIVERY",
                                   "DELIVERED","FAILED_DELIVERY");
        String up = s.toUpperCase();
        return valid.contains(up) ? up : null;
    }

    private int countByShipper(String username, String status) throws SQLException {
        String base = "SELECT COUNT(*) FROM shipments WHERE shipper_user_id=? ";
        String sql = base + (status != null && !status.isBlank() ? "AND status=?" : "");
        try (java.sql.Connection con = DBUtil.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, username);
            if (status != null && !status.isBlank()) ps.setString(i++, status);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
