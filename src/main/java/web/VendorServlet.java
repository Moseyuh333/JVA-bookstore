package web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.StoreDAO;
import models.Store;
import utils.AuthUtil;
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
import java.util.List;

@WebServlet(name = "VendorServlet", urlPatterns = {"/api/vendor/*"})
public class VendorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        try {
            Long userId = AuthUtil.resolveUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Unauthorized\"}");
                return;
            }

            if (path == null || "/".equals(path)) {
                // list stores owned by user
                List<Store> stores = StoreDAO.listStoresByOwner(userId);
                JsonArray arr = new JsonArray();
                for (Store s : stores) {
                    JsonObject o = new JsonObject();
                    o.addProperty("id", s.getId());
                    o.addProperty("name", s.getName());
                    o.addProperty("description", s.getDescription());
                    o.addProperty("avatarUrl", s.getAvatarUrl());
                    o.addProperty("coverUrl", s.getCoverUrl());
                    arr.add(o);
                }
                out.write(arr.toString());
                return;
            }

            if (path.startsWith("/wallet")) {
                handleWalletGet(req, resp, out, userId);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\":\"Not found\"}");
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    private void handleWalletGet(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, Long userId) throws SQLException {
        String storeIdParam = req.getParameter("storeId");
        if (storeIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"storeId required\"}");
            return;
        }
        long storeId = Long.parseLong(storeIdParam);
        // verify ownership or employee
        if (!isStoreOwnerOrEmployee(storeId, userId)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write("{\"error\":\"Forbidden\"}");
            return;
        }

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM store_wallets WHERE store_id = ?")) {
            stmt.setLong(1, storeId);
            try (ResultSet rs = stmt.executeQuery()) {
                JsonObject res = new JsonObject();
                if (rs.next()) {
                    res.addProperty("balance", rs.getBigDecimal(1).toPlainString());
                } else {
                    res.addProperty("balance", "0");
                }
                out.write(res.toString());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        try {
            Long userId = AuthUtil.resolveUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\":\"Unauthorized\"}");
                return;
            }

            if (path == null || "/".equals(path)) {
                // create store
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                if (name == null || name.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"name required\"}");
                    return;
                }
                Store s = StoreDAO.createStore(userId, name, desc);
                if (s != null) {
                    out.write("{\"id\":" + s.getId() + ", \"message\":\"Store created\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write("{\"error\":\"Failed to create store\"}");
                }
                return;
            }

            if (path.startsWith("/employees")) {
                handleEmployeesPost(req, resp, out, userId);
                return;
            }

            if (path.startsWith("/wallet/withdraw")) {
                handleWithdraw(req, resp, out, userId);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\":\"Not found\"}");
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    private void handleEmployeesPost(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, Long userId) throws SQLException {
        String action = req.getParameter("action");
        String storeIdParam = req.getParameter("storeId");
        if (storeIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"storeId required\"}");
            return;
        }
        long storeId = Long.parseLong(storeIdParam);
        if (!isStoreOwner(storeId, userId)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write("{\"error\":\"Only owner can manage employees\"}");
            return;
        }
        if ("add".equalsIgnoreCase(action)) {
            String userIdStr = req.getParameter("userId");
            if (userIdStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"userId required\"}");
                return;
            }
            long u = Long.parseLong(userIdStr);
            boolean ok = StoreDAO.addEmployee(storeId, u, req.getParameter("role"));
            out.write("{\"success\":" + ok + "}");
            return;
        } else if ("remove".equalsIgnoreCase(action)) {
            String userIdStr = req.getParameter("userId");
            if (userIdStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"userId required\"}");
                return;
            }
            long u = Long.parseLong(userIdStr);
            boolean ok = StoreDAO.removeEmployee(storeId, u);
            out.write("{\"success\":" + ok + "}");
            return;
        }
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.write("{\"error\":\"unknown action\"}");
    }

    private void handleWithdraw(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, Long userId) throws SQLException {
        String storeIdParam = req.getParameter("storeId");
        String amountParam = req.getParameter("amount");
        if (storeIdParam == null || amountParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"storeId and amount required\"}");
            return;
        }
        long storeId = Long.parseLong(storeIdParam);
        if (!isStoreOwnerOrEmployee(storeId, userId)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write("{\"error\":\"Forbidden\"}");
            return;
        }
        java.math.BigDecimal amount = new java.math.BigDecimal(amountParam);
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement sel = conn.prepareStatement("SELECT balance FROM store_wallets WHERE store_id = ? FOR UPDATE")) {
                sel.setLong(1, storeId);
                java.math.BigDecimal balance = java.math.BigDecimal.ZERO;
                try (ResultSet rs = sel.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getBigDecimal(1);
                    } else {
                        // create row
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO store_wallets (store_id, balance) VALUES (?, ?)")) {
                            ins.setLong(1, storeId);
                            ins.setBigDecimal(2, java.math.BigDecimal.ZERO);
                            ins.executeUpdate();
                        }
                    }
                }

                if (balance.compareTo(amount) < 0) {
                    conn.rollback();
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Insufficient balance\"}");
                    return;
                }

                try (PreparedStatement upd = conn.prepareStatement("UPDATE store_wallets SET balance = balance - ?, updated_at = CURRENT_TIMESTAMP WHERE store_id = ?")) {
                    upd.setBigDecimal(1, amount);
                    upd.setLong(2, storeId);
                    upd.executeUpdate();
                }

                try (PreparedStatement tx = conn.prepareStatement("INSERT INTO store_wallet_transactions (store_id, amount, type, description) VALUES (?, ?, 'withdraw', ?)")) {
                    tx.setLong(1, storeId);
                    tx.setBigDecimal(2, amount);
                    tx.setString(3, "Withdrawal requested by user " + userId);
                    tx.executeUpdate();
                }

                conn.commit();
                out.write("{\"success\":true}");
                return;
            }
        }
    }

    private boolean isStoreOwner(long storeId, long userId) throws SQLException {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM stores WHERE id = ? AND owner_user_id = ?")) {
            stmt.setLong(1, storeId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isStoreOwnerOrEmployee(long storeId, long userId) throws SQLException {
        if (isStoreOwner(storeId, userId)) return true;
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM store_employees WHERE store_id = ? AND user_id = ?")) {
            stmt.setLong(1, storeId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
