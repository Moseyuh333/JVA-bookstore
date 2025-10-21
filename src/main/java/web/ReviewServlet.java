package web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.ReviewDAO;
import utils.AuthUtil;
import utils.FileStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/api/reviews", "/api/reviews/*"})
@MultipartConfig
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
            String contentType = request.getContentType() != null ? request.getContentType().toLowerCase() : "";
            boolean isMultipart = contentType.contains("multipart/form-data");
            ReviewPayload payload;
            Part mediaPart = null;
            if (isMultipart) {
                payload = parsePayloadFromMultipart(request);
                mediaPart = safeGetPart(request, "media");
            } else {
                payload = parsePayloadFromJson(readJson(request));
            }
            if (payload.bookId == null || payload.rating < 1 || payload.rating > 5) {
                sendBadRequest(response, "Dữ liệu đánh giá không hợp lệ");
                return;
            }
            ReviewDAO.ReviewRecord existing = ReviewDAO.findUserReview(userId, payload.bookId);
            String contextPath = request.getContextPath();
            if (payload.mediaUrl != null) {
                payload.mediaUrl = FileStorageUtil.normalizeReviewMediaUrl(payload.mediaUrl, contextPath);
                if (payload.mediaUrl == null) {
                    payload.mediaType = null;
                }
            }
            if (!payload.removeMedia && (payload.mediaUrl == null || payload.mediaUrl.isEmpty()) && existing != null) {
                payload.mediaUrl = existing.mediaUrl;
                payload.mediaType = existing.mediaType;
            }
            try {
                if (mediaPart != null && mediaPart.getSize() > 0) {
                    FileStorageUtil.StoredFile storedFile = FileStorageUtil.storeReviewMedia(mediaPart);
                    payload.mediaUrl = storedFile.getPublicUrl();
                    payload.mediaType = storedFile.getMediaType();
                    payload.removeMedia = false;
                }
            } catch (IOException uploadEx) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(buildError(uploadEx.getMessage())));
                return;
            }
            if (payload.removeMedia) {
                payload.mediaUrl = null;
                payload.mediaType = null;
            }
            ReviewDAO.ReviewRecord saved = ReviewDAO.upsertReview(userId, payload.bookId, payload.rating, payload.title, payload.content, payload.mediaUrl, payload.mediaType);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("review", saved);
            response.getWriter().write(gson.toJson(result));
            if (existing != null && existing.mediaUrl != null) {
                boolean shouldDelete = payload.mediaUrl == null || !existing.mediaUrl.equals(payload.mediaUrl);
                if (shouldDelete) {
                    FileStorageUtil.deleteReviewMedia(existing.mediaUrl, contextPath);
                }
            }
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
            ReviewDAO.ReviewRecord existing = ReviewDAO.findUserReview(userId, bookId);
            ReviewDAO.deleteReview(userId, bookId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            response.getWriter().write(gson.toJson(result));
            if (existing != null && existing.mediaUrl != null) {
                FileStorageUtil.deleteReviewMedia(existing.mediaUrl, request.getContextPath());
            }
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

    private ReviewPayload parsePayloadFromJson(Map<String, Object> data) {
        ReviewPayload payload = new ReviewPayload();
        payload.bookId = parseId(data.get("bookId"));
        payload.rating = parseInt(data.get("rating"), -1);
        payload.title = stringValue(data.get("title"));
        payload.content = stringValue(data.get("content"));
        payload.mediaUrl = stringValue(data.get("mediaUrl"));
        payload.mediaType = stringValue(data.get("mediaType"));
        Object remove = data.get("removeMedia");
        payload.removeMedia = remove instanceof Boolean ? (Boolean) remove : "true".equalsIgnoreCase(stringValue(remove));
        return payload;
    }

    private ReviewPayload parsePayloadFromMultipart(HttpServletRequest request) {
        ReviewPayload payload = new ReviewPayload();
        payload.bookId = parseId(request.getParameter("bookId"));
        payload.rating = parseInt(request.getParameter("rating"), -1);
        payload.title = stringValue(request.getParameter("title"));
        payload.content = stringValue(request.getParameter("content"));
        payload.mediaUrl = stringValue(request.getParameter("mediaUrl"));
        payload.mediaType = stringValue(request.getParameter("mediaType"));
        String remove = stringValue(request.getParameter("removeMedia"));
        payload.removeMedia = remove != null && ("true".equalsIgnoreCase(remove) || "1".equals(remove));
        return payload;
    }

    private Part safeGetPart(HttpServletRequest request, String name) {
        try {
            return request.getPart(name);
        } catch (Exception ex) {
            return null;
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

    private static final class ReviewPayload {
        private Long bookId;
        private int rating;
        private String title;
        private String content;
        private String mediaUrl;
        private String mediaType;
        private boolean removeMedia;
    }
}
