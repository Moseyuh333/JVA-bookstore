package web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.ReviewDAO;
import utils.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/api/reviews", "/api/reviews/*"})
public class ReviewServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<String> segments = getPathSegments(request.getPathInfo());
        if (segments.size() == 1 && "me".equalsIgnoreCase(segments.get(0))) {
            fetchOwnReview(request, response);
            return;
        }
        listReviews(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Long userId = AuthUtil.resolveUserId(request);
            if (userId == null) {
                sendUnauthorized(response);
                return;
            }
            Map<String, Object> payload = readJson(request);
            Long bookId = parseId(payload.get("bookId"));
            int rating = parseInt(payload.get("rating"), -1);
            if (bookId == null || rating < 1 || rating > 5) {
                sendBadRequest(response, "Dữ liệu đánh giá không hợp lệ");
                return;
            }
            String title = stringValue(payload.get("title"));
            String content = stringValue(payload.get("content"));
            String mediaUrl = stringValue(payload.get("mediaUrl"));
            String mediaType = stringValue(payload.get("mediaType"));
            ReviewDAO.upsertReview(userId, bookId, rating, title, content, mediaUrl, mediaType);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            response.getWriter().write(gson.toJson(result));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Long userId = AuthUtil.resolveUserId(request);
            if (userId == null) {
                sendUnauthorized(response);
                return;
            }
            List<String> segments = getPathSegments(request.getPathInfo());
            if (segments.size() != 1) {
                sendNotFound(response);
                return;
            }
            Long bookId = parseId(segments.get(0));
            if (bookId == null) {
                sendNotFound(response);
                return;
            }
            ReviewDAO.deleteReview(userId, bookId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            response.getWriter().write(gson.toJson(result));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private void listReviews(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long bookId = parseId(request.getParameter("bookId"));
        int limit = parseInt(request.getParameter("limit"), 20);
        int offset = parseInt(request.getParameter("offset"), 0);
        if (bookId == null) {
            sendBadRequest(response, "Thiếu bookId");
            return;
        }
        try {
            List<ReviewDAO.ReviewRecord> reviews = ReviewDAO.listReviews(bookId, limit, offset);
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("items", reviews);
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private void fetchOwnReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Long userId = AuthUtil.resolveUserId(request);
            if (userId == null) {
                sendUnauthorized(response);
                return;
            }
            Long bookId = parseId(request.getParameter("bookId"));
            if (bookId == null) {
                sendBadRequest(response, "Thiếu bookId");
                return;
            }
            ReviewDAO.ReviewRecord record = ReviewDAO.findUserReview(userId, bookId);
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("review", record);
            response.getWriter().write(gson.toJson(payload));
        } catch (SQLException ex) {
            handleServerError(response, ex);
        }
    }

    private Map<String, Object> readJson(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        if (json.length() == 0) {
            return new HashMap<>();
        }
        return gson.fromJson(json.toString(), new TypeToken<Map<String, Object>>() {}.getType());
    }

    private List<String> getPathSegments(String pathInfo) {
        List<String> segments = new java.util.ArrayList<>();
        if (pathInfo == null || pathInfo.isEmpty()) {
            return segments;
        }
        String[] tokens = pathInfo.split("/");
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                segments.add(token);
            }
        }
        return segments;
    }

    private Long parseId(Object raw) {
        if (raw instanceof Number) {
            long value = ((Number) raw).longValue();
            return value > 0 ? value : null;
        }
        if (raw instanceof String) {
            try {
                long value = Long.parseLong(((String) raw).trim());
                return value > 0 ? value : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private int parseInt(Object raw, int defaultValue) {
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        if (raw instanceof String) {
            try {
                return Integer.parseInt(((String) raw).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String stringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(buildError("Bạn cần đăng nhập để thực hiện thao tác này")));
    }

    private void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(buildError(message)));
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write(gson.toJson(buildError("Endpoint không hợp lệ")));
    }

    private Map<String, Object> buildError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }

    private void handleServerError(HttpServletResponse response, Exception ex) throws IOException {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(buildError("Có lỗi xảy ra: " + ex.getMessage())));
    }
}
