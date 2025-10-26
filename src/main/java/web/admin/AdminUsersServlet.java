package web.admin;

import org.mindrot.jbcrypt.BCrypt;
import org.postgresql.util.PGobject;
import utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "AdminUsersServlet", urlPatterns = {"/api/admin/users"})
public class AdminUsersServlet extends HttpServlet {

    private static final List<String> ROLE_PRIORITY = List.of(
        "CUSTOMER",
        "USER",
        "ADMIN",
        "SELLER",
        "SHIPPER"
    );

    private static final List<String> STATUS_PRIORITY = List.of(
        "ACTIVE",
        "INACTIVE",
        "BANNED",
        "PENDING"
    );

    private volatile Map<String, String> roleLookup = Collections.emptyMap();
    private volatile Map<String, String> statusLookup = Collections.emptyMap();
    private volatile String defaultRole = "CUSTOMER";
    private volatile String defaultStatus = "ACTIVE";
    
    @Override
    public void init() throws ServletException {
        super.init();
        refreshEnumLookups();
    }

    private void refreshEnumLookups() {
        Map<String, String> resolvedRoles = loadEnumValues("user_role", ROLE_PRIORITY);
        Map<String, String> resolvedStatuses = loadEnumValues("user_status", STATUS_PRIORITY);

        this.roleLookup = resolvedRoles;
        this.statusLookup = resolvedStatuses;
        this.defaultRole = selectDefault(resolvedRoles, ROLE_PRIORITY, ROLE_PRIORITY.get(0));
        this.defaultStatus = selectDefault(resolvedStatuses, STATUS_PRIORITY, STATUS_PRIORITY.get(0));
    }

    private Map<String, String> loadEnumValues(String typeName, List<String> fallbacks) {
        Map<String, String> values = new HashMap<>();
        String sql = "SELECT enumlabel FROM pg_enum e JOIN pg_type t ON e.enumtypid = t.oid WHERE t.typname = ? ORDER BY e.enumsortorder";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, typeName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString(1);
                    if (label == null) {
                        continue;
                    }
                    String trimmed = label.trim();
                    if (!trimmed.isEmpty()) {
                        values.put(trimmed.toLowerCase(Locale.US), trimmed);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminUsersServlet] Unable to load enum values for " + typeName + ": " + e.getMessage());
        }

        for (String fallback : fallbacks) {
            if (fallback == null || fallback.trim().isEmpty()) {
                continue;
            }
            String trimmed = fallback.trim();
            String key = trimmed.toLowerCase(Locale.US);
            values.putIfAbsent(key, trimmed);
        }

        return Collections.unmodifiableMap(values);
    }

    private String selectDefault(Map<String, String> lookup, List<String> priority, String hardFallback) {
        for (String candidate : priority) {
            String resolved = lookup.get(candidate.toLowerCase(Locale.US));
            if (resolved != null) {
                return resolved;
            }
        }
        return lookup.values().stream().findFirst().orElse(hardFallback);
    }

    private String toJsonLabelArray(Collection<String> labels) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String label : labels) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            String value = label != null ? label.toLowerCase(Locale.US) : "";
            sb.append('"').append(escapeJson(value)).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String action = req.getParameter("action");
        
        try {
            if ("list".equals(action)) {
                listUsers(req, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                createUser(req, resp, out);
            } else if ("update".equals(action)) {
                updateUser(req, resp, out);
            } else if ("delete".equals(action)) {
                deleteUser(req, out);
            } else if ("clear-tokens".equals(action)) {
                int affected = clearVerificationTokens();
                out.write("{\"message\":\"Success\", \"affected\":" + affected + "}");
            } else if ("verify-all".equals(action)) {
                int affected = verifyAllUsers();
                out.write("{\"message\":\"Success\", \"affected\":" + affected + "}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid action\"}");
                return;
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
    
    private void listUsers(HttpServletRequest req, PrintWriter out) throws SQLException {
        String search = req.getParameter("search");
        String status = req.getParameter("status");
        String searchType = req.getParameter("searchType");

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatusFilter = status != null && !status.trim().isEmpty() && !"all".equals(status.trim());
        boolean hasSearchType = searchType != null && !searchType.trim().isEmpty() && !"all".equals(searchType.trim());

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id, username, email, full_name, phone, role, status, email_verified, created_at, updated_at, birth_date, address FROM users");

        List<String> conditions = new java.util.ArrayList<>();
        List<Object> params = new java.util.ArrayList<>();

        if (hasSearch) {
            if (hasSearchType) {
                // Search in specific field
                String field = "username"; // default
                if ("email".equals(searchType)) {
                    field = "email";
                } else if ("phone".equals(searchType)) {
                    field = "phone";
                } else if ("full_name".equals(searchType)) {
                    field = "full_name";
                }
                conditions.add("LOWER(" + field + ") LIKE ?");
                params.add("%" + search.trim().toLowerCase() + "%");
            } else {
                // Search in multiple fields
                conditions.add("(LOWER(username) LIKE ? OR LOWER(email) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(phone) LIKE ?)");
                String likeSearch = "%" + search.trim().toLowerCase() + "%";
                params.add(likeSearch);
                params.add(likeSearch);
                params.add(likeSearch);
                params.add(likeSearch);
            }
        }

        if (hasStatusFilter) {
            String normalizedStatus = normalizeStatus(status);
            if (normalizedStatus != null) {
                conditions.add("status = ?");
                params.add(normalizedStatus);
            }
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            sqlBuilder.append(String.join(" AND ", conditions));
        }

        sqlBuilder.append(" ORDER BY created_at DESC");

        String sql = sqlBuilder.toString();

        StringBuilder json = new StringBuilder();
        json.append("{\"users\":[");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (hasStatusFilter && i == params.size() - 1) {
                    setEnumParam(pstmt, i + 1, "user_status", (String) param);
                } else {
                    pstmt.setString(i + 1, (String) param);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean first = true;
                Set<Integer> seenIds = new HashSet<>();
                String roleFallback = defaultRole != null ? defaultRole.toLowerCase(Locale.US) : "customer";
                String statusFallback = defaultStatus != null ? defaultStatus.toLowerCase(Locale.US) : "active";
                while (rs.next()) {
                    int userId = rs.getInt("id");
                    if (!seenIds.add(userId)) {
                        continue;
                    }
                    if (!first) {
                        json.append(",");
                    }
                    first = false;

                    String createdAt = "";
                    java.sql.Timestamp createdTs = rs.getTimestamp("created_at");
                    if (createdTs != null) {
                        createdAt = escapeJson(createdTs.toString());
                    }

                    String updatedAt = "";
                    java.sql.Timestamp updatedTs = rs.getTimestamp("updated_at");
                    if (updatedTs != null) {
                        updatedAt = escapeJson(updatedTs.toString());
                    }

                    String birthDate = "";
                    java.sql.Date birth = rs.getDate("birth_date");
                    if (birth != null) {
                        birthDate = escapeJson(birth.toString());
                    }

                    String roleValue = rs.getString("role");
                    String statusValue = rs.getString("status");

                    json.append("{")
                        .append("\"id\":").append(userId).append(",")
                        .append("\"username\":\"").append(escapeJson(rs.getString("username"))).append("\",")
                        .append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",")
                        .append("\"full_name\":\"").append(escapeJson(rs.getString("full_name"))).append("\",")
                        .append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",")
                        .append("\"role\":\"").append(escapeJson(toLowerCase(roleValue, roleFallback))).append("\",")
                        .append("\"status\":\"").append(escapeJson(toLowerCase(statusValue, statusFallback))).append("\",")
                        .append("\"verified\":").append(rs.getBoolean("email_verified")).append(",")
                        .append("\"created\":\"").append(createdAt).append("\",")
                        .append("\"updated\":\"").append(updatedAt).append("\",")
                        .append("\"birth_date\":\"").append(birthDate).append("\",")
                        .append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\"")
                        .append("}");
                }
            }
        }

        json.append("]}");
        out.write(json.toString());
    }

    private String toLowerCase(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim().toLowerCase(Locale.US);
    }

    private String normalizeRole(String role) {
        Map<String, String> lookup = this.roleLookup;
        String fallback = defaultRole;

        if (role == null || role.trim().isEmpty()) {
            return fallback;
        }

        String trimmed = role.trim();
        String lower = trimmed.toLowerCase(Locale.US);
        String resolved = lookup.get(lower);
        if (resolved != null) {
            return resolved;
        }

        if ("user".equals(lower) || "customer".equals(lower)) {
            String customer = lookup.get("customer");
            if (customer != null) {
                return customer;
            }
            String userRole = lookup.get("user");
            if (userRole != null) {
                return userRole;
            }
        }

        return null;
    }

    private String normalizeStatus(String status) {
        Map<String, String> lookup = this.statusLookup;
        String fallback = defaultStatus;

        if (status == null || status.trim().isEmpty()) {
            return fallback;
        }

        String trimmed = status.trim();
        String lower = trimmed.toLowerCase(Locale.US);
        String resolved = lookup.get(lower);

        // Handle Vietnamese status names
        if (resolved == null) {
            switch (lower) {
                case "chờ kích hoạt":
                case "pending":
                    resolved = lookup.get("pending");
                    break;
                case "hoạt động":
                case "active":
                    resolved = lookup.get("active");
                    break;
                case "không hoạt động":
                case "inactive":
                    resolved = lookup.get("inactive");
                    break;
                case "cấm":
                case "banned":
                    resolved = lookup.get("banned");
                    break;
            }
        }

        return resolved != null ? resolved : null;
    }

    private void setEnumParam(PreparedStatement stmt, int index, String typeName, String value) throws SQLException {
        if (value == null) {
            setEnumNull(stmt, index);
            return;
        }

        PGobject enumObject = new PGobject();
        enumObject.setType(typeName);
        enumObject.setValue(value);
        try {
            stmt.setObject(index, enumObject);
        } catch (SQLException ex) {
            if (isEnumTypeUnavailable(ex)) {
                stmt.setString(index, value);
            } else {
                throw ex;
            }
        }
    }

    private void setEnumNull(PreparedStatement stmt, int index) throws SQLException {
        try {
            stmt.setNull(index, Types.OTHER);
        } catch (SQLException ex) {
            if (isEnumTypeUnavailable(ex)) {
                stmt.setNull(index, Types.VARCHAR);
            } else {
                throw ex;
            }
        }
    }

    private boolean isEnumTypeUnavailable(SQLException ex) {
        if (ex == null) {
            return false;
        }
        String state = ex.getSQLState();
        if (state != null) {
            if ("42704".equals(state) || "42883".equals(state) || "0A000".equals(state)) {
                return true;
            }
        }
        String message = ex.getMessage();
        if (message != null) {
            String lower = message.toLowerCase(Locale.US);
            if (lower.contains("type \"user_role\" does not exist") ||
                lower.contains("type \"user_status\" does not exist") ||
                (lower.contains("type \"") && lower.contains("does not exist"))) {
                return true;
            }
        }
        return false;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
    
    private int clearVerificationTokens() throws SQLException {
        String sql = "UPDATE users SET verification_token = NULL, email_verified = true WHERE verification_token IS NOT NULL";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        }
    }
    
    private int verifyAllUsers() throws SQLException {
        String sql = "UPDATE users SET email_verified = true WHERE email_verified = false";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        }
    }

    private void createUser(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws SQLException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String passwordHash = req.getParameter("password_hash");
        String rawPassword = req.getParameter("password");
        String fullName = req.getParameter("full_name");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");
        String status = req.getParameter("status");

        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Username and email are required\"}");
            return;
        }

        if ((passwordHash == null || passwordHash.trim().isEmpty()) && rawPassword != null && !rawPassword.trim().isEmpty()) {
            String trimmed = rawPassword.trim();
            if (trimmed.length() < 6) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Password must be at least 6 characters\"}");
                return;
            }
            passwordHash = BCrypt.hashpw(trimmed, BCrypt.gensalt());
        }

        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Password is required\"}");
            return;
        }

        String normalizedRole = normalizeRole(role);
        if (normalizedRole == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Giá trị quyền không hợp lệ cho cơ sở dữ liệu\"," +
                "\"role\":\"" + escapeJson(role != null ? role : "") + "\"," +
                "\"allowed\":" + toJsonLabelArray(roleLookup.values()) + "}");
            return;
        }

        String normalizedStatus = normalizeStatus(status);
        if (normalizedStatus == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Giá trị trạng thái không hợp lệ cho cơ sở dữ liệu\"," +
                "\"status\":\"" + escapeJson(status != null ? status : "") + "\"," +
                "\"allowed\":" + toJsonLabelArray(statusLookup.values()) + "}");
            return;
        }

        String sql = "INSERT INTO users (username, email, password_hash, full_name, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, fullName != null && !fullName.trim().isEmpty() ? fullName.trim() : null);
            pstmt.setString(5, phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
            setEnumParam(pstmt, 6, "user_role", normalizedRole);
            setEnumParam(pstmt, 7, "user_status", normalizedStatus);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.write("{\"message\":\"User created successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\":\"Failed to create user\"}");
            }
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if ("22P02".equals(sqlState)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Giá trị quyền hoặc trạng thái không hợp lệ cho cơ sở dữ liệu\"," +
                        "\"role\":\"" + escapeJson(normalizedRole) + "\"," +
                        "\"status\":\"" + escapeJson(normalizedStatus) + "\"}");
                return;
            }
            if (sqlState != null && sqlState.startsWith("23")) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write("{\"error\":\"Username or email already exists\"}");
                return;
            }
            throw e;
        }
    }

    private void updateUser(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String fullName = req.getParameter("full_name");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");
        String status = req.getParameter("status");

        if (idStr == null || username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            out.write("{\"error\":\"ID, username and email are required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String normalizedRole = normalizeRole(role);
        if (normalizedRole == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Giá trị quyền không hợp lệ cho cơ sở dữ liệu\"," +
                "\"role\":\"" + escapeJson(role != null ? role : "") + "\"," +
                "\"allowed\":" + toJsonLabelArray(roleLookup.values()) + "}");
            return;
        }

        String normalizedStatus = normalizeStatus(status);
        if (normalizedStatus == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Giá trị trạng thái không hợp lệ cho cơ sở dữ liệu\"," +
                "\"status\":\"" + escapeJson(status != null ? status : "") + "\"," +
                "\"allowed\":" + toJsonLabelArray(statusLookup.values()) + "}");
            return;
        }

        String sql = "UPDATE users SET username = ?, email = ?, full_name = ?, phone = ?, role = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());
            pstmt.setString(3, fullName != null ? fullName.trim() : null);
            pstmt.setString(4, phone != null ? phone.trim() : null);
            setEnumParam(pstmt, 5, "user_role", normalizedRole);
            setEnumParam(pstmt, 6, "user_status", normalizedStatus);
            pstmt.setInt(7, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"User updated successfully\"}");
            } else {
                out.write("{\"error\":\"User not found\"}");
            }
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if ("22P02".equals(sqlState)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Giá trị quyền hoặc trạng thái không hợp lệ cho cơ sở dữ liệu\"," +
                        "\"role\":\"" + escapeJson(normalizedRole) + "\"," +
                        "\"status\":\"" + escapeJson(normalizedStatus) + "\"}");
                return;
            }
            throw e;
        }
    }

    private void deleteUser(HttpServletRequest req, PrintWriter out) throws SQLException {
        String idStr = req.getParameter("id");

        if (idStr == null) {
            out.write("{\"error\":\"ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                out.write("{\"message\":\"User deleted successfully\"}");
            } else {
                out.write("{\"error\":\"User not found\"}");
            }
        }
    }
}
