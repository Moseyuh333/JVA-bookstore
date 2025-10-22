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

    // --------------- routing ---------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = currentUsername(req);
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid token")); return; }

        String path = pathInfo(req);
        try {
            if (path.equals("") || path.equals("/")) {
                writeJson(resp, 200, mapOf("ok", true, "service", "shipper-api"));
                return;
            }

            if (path.equals("/shipments")) {
                String status = opt(req.getParameter("status"));
                int page = parseInt(req.getParameter("page"), 1);
                int size = clamp(parseInt(req.getParameter("size"), 20), 1, 100);

                List<Shipment> items = shipmentDAO.findByShipper(user, status, page, size);
                int total = countByShipper(user, status); // count nhanh-gn
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("items", items);
                out.put("page", page);
                out.put("size", size);
                out.put("total", total);
                writeJson(resp, 200, out);
                return;
            }

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

            if (path.equals("/stats")) {
                Map<String, Integer> st = shipmentDAO.getStats(user);
                // Bảo đảm 3 field bắt buộc cho pie chart
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("inProgress", st.getOrDefault("inProgress", 0));
                out.put("delivered", st.getOrDefault("delivered", 0));
                out.put("failed", st.getOrDefault("failed", 0));
                // bonus KPI
                int d = st.getOrDefault("delivered", 0);
                int f = st.getOrDefault("failed", 0);
                double rate = (d + f) == 0 ? 0.0 : (double) d / (double) (d + f);
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
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid token")); return; }

        String path = pathInfo(req);
        try {
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
        if (user == null) { writeJson(resp, 401, err("UNAUTHORIZED", "Missing/invalid token")); return; }

        String path = pathInfo(req);
        try {
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

    // --------------- helpers ---------------
    private String currentUsername(HttpServletRequest req) {
        // 1) nếu filter có set attribute
        Object v = req.getAttribute("username");
        if (v != null && !v.toString().trim().isEmpty()) return v.toString().trim();

        // 2) lấy token từ cookie "token" hoặc header Authorization
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("token".equalsIgnoreCase(c.getName())) { token = c.getValue(); break; }
            }
        }
        if (token == null || token.isEmpty()) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7).trim();
        }
        if (token == null || token.isEmpty()) return null;

        // 3) decode JWT → lấy subject = username
        try {
            String user = JwtUtil.validateToken(token);
            return (user != null && !user.isBlank()) ? user : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String pathInfo(HttpServletRequest req) {
        String p = req.getPathInfo();
        return p == null ? "" : p.trim();
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(body));
        }
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
        try (BufferedReader br = req.getReader()) {
            String line; while ((line = br.readLine()) != null) sb.append(line);
        }
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

    // -------- count helper (nhẹ nhàng) --------
    private int countByShipper(String username, String status) throws SQLException {
        // cách nhẹ: dùng cùng connection trong DBUtil mỗi lần gọi — đủ dùng cho trang list
        String base = "SELECT COUNT(*) FROM shipments WHERE shipper_user_id=? ";
        String sql = base + (status != null && !status.isEmpty() ? "AND status=?" : "");
        try (java.sql.Connection con = DBUtil.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, username);
            if (status != null && !status.isEmpty()) ps.setString(i++, status);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
