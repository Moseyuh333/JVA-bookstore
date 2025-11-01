package utils;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Utility class to store and serve user-uploaded review media safely.
 */
public final class FileStorageUtil {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final long MAX_VIDEO_SIZE = 20L * 1024 * 1024; // 20 MB
    private static final Map<String, String> IMAGE_TYPES = new ConcurrentHashMap<>();
    private static final Map<String, String> VIDEO_TYPES = new ConcurrentHashMap<>();
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final String REVIEW_MEDIA_PREFIX = "/media/reviews/";
    private static final String SHIPMENT_EVIDENCE_PREFIX = "/media/shipments/";
    private static final Path BASE_DIRECTORY = initBaseDirectory();

    static {
        IMAGE_TYPES.put("image/jpeg", "jpg");
        IMAGE_TYPES.put("image/jpg", "jpg");
        IMAGE_TYPES.put("image/png", "png");
        IMAGE_TYPES.put("image/gif", "gif");
        IMAGE_TYPES.put("image/webp", "webp");

        VIDEO_TYPES.put("video/mp4", "mp4");
        VIDEO_TYPES.put("video/webm", "webm");
        VIDEO_TYPES.put("video/ogg", "ogv");
    }

    private FileStorageUtil() {
    }

    private static Path initBaseDirectory() {
        String configured = System.getProperty("bookstore.upload.dir");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("BOOKSTORE_UPLOAD_DIR");
        }
        Path base;
        if (configured != null && !configured.trim().isEmpty()) {
            base = Paths.get(configured.trim());
        } else {
            Path home = Paths.get(System.getProperty("user.home", "."));
            base = home.resolve(".jva-bookstore").resolve("uploads");
        }
        try {
            Files.createDirectories(base);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create upload directory: " + base, ex);
        }
        return base;
    }

    private static Path reviewMediaDirectory() throws IOException {
        Path dir = BASE_DIRECTORY.resolve("reviews");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    private static Path shipmentEvidenceDirectory() throws IOException {
        Path dir = BASE_DIRECTORY.resolve("shipments");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    public static StoredFile storeReviewMedia(Part part) throws IOException {
        if (part == null || part.getSize() <= 0) {
            throw new IOException("Tệp tải lên không hợp lệ");
        }
        String contentType = normalizeContentType(part.getContentType());
        MediaCategory category = detectCategory(contentType);
        if (category == MediaCategory.UNKNOWN) {
            throw new IOException("Định dạng tệp không được hỗ trợ");
        }
        long size = part.getSize();
        if ((category == MediaCategory.IMAGE && size > MAX_IMAGE_SIZE)
                || (category == MediaCategory.VIDEO && size > MAX_VIDEO_SIZE)) {
            throw new IOException(category == MediaCategory.IMAGE
                    ? "Ảnh vượt quá dung lượng tối đa 5MB"
                    : "Video vượt quá dung lượng tối đa 20MB");
        }
        String extension = resolveExtension(part, contentType, category);
        String filename = generateFileName(extension);
        Path targetDir = reviewMediaDirectory();
        Path target = targetDir.resolve(filename);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredFile(REVIEW_MEDIA_PREFIX + filename,
                category == MediaCategory.IMAGE ? "image" : "video",
                contentType,
                size,
                target);
    }

    public static StoredFile storeShipmentEvidence(Part part) throws IOException {
        if (part == null || part.getSize() <= 0) {
            throw new IOException("Vui lòng chọn ảnh minh chứng.");
        }
        String contentType = normalizeContentType(part.getContentType());
        MediaCategory category = detectCategory(contentType);
        if (category != MediaCategory.IMAGE) {
            throw new IOException("Chỉ hỗ trợ các định dạng ảnh JPG, PNG, GIF hoặc WebP.");
        }
        long size = part.getSize();
        if (size > MAX_IMAGE_SIZE) {
            throw new IOException("Ảnh vượt quá dung lượng tối đa 5MB.");
        }
        String extension = resolveExtension(part, contentType, category);
        String filename = generateFileName(extension);
        Path targetDir = shipmentEvidenceDirectory();
        Path target = targetDir.resolve(filename);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredFile(SHIPMENT_EVIDENCE_PREFIX + filename,
                "image",
                contentType,
                size,
                target);
    }

    public static boolean deleteReviewMedia(String mediaUrl, String contextPath) {
        String fileName = extractReviewMediaFileName(mediaUrl, contextPath);
        if (fileName == null) {
            return false;
        }
        Path target = reviewMediaPath(fileName);
        try {
            return Files.deleteIfExists(target);
        } catch (IOException ex) {
            System.err.println("Cannot delete review media: " + target + " - " + ex.getMessage());
            return false;
        }
    }

    public static Path reviewMediaPath(String fileName) {
        Objects.requireNonNull(fileName, "fileName");
        return BASE_DIRECTORY.resolve("reviews").resolve(fileName);
    }

    public static String extractReviewMediaFileNameFromPathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            return null;
        }
        String raw = pathInfo.trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }
        if (raw.contains("/")) {
            return null;
        }
        if (!SAFE_FILENAME.matcher(raw).matches()) {
            return null;
        }
        return raw;
    }

    public static String extractReviewMediaFileName(String mediaUrl, String contextPath) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            return null;
        }
        String normalized = stripContextPath(mediaUrl.trim(), contextPath);
        if (!normalized.startsWith(REVIEW_MEDIA_PREFIX)) {
            return null;
        }
        String fileName = normalized.substring(REVIEW_MEDIA_PREFIX.length());
        if (!SAFE_FILENAME.matcher(fileName).matches()) {
            return null;
        }
        return fileName;
    }

    public static String normalizeReviewMediaUrl(String mediaUrl, String contextPath) {
        String fileName = extractReviewMediaFileName(mediaUrl, contextPath);
        if (fileName == null) {
            return null;
        }
        return REVIEW_MEDIA_PREFIX + fileName;
    }

    public static boolean isReviewMediaUrl(String mediaUrl, String contextPath) {
        return extractReviewMediaFileName(mediaUrl, contextPath) != null;
    }

    public static Path shipmentEvidencePath(String fileName) {
        Objects.requireNonNull(fileName, "fileName");
        return BASE_DIRECTORY.resolve("shipments").resolve(fileName);
    }

    public static String extractShipmentEvidenceFileNameFromPathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            return null;
        }
        String raw = pathInfo.trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }
        if (raw.contains("/")) {
            return null;
        }
        if (!SAFE_FILENAME.matcher(raw).matches()) {
            return null;
        }
        return raw;
    }

    public static String extractShipmentEvidenceFileName(String mediaUrl, String contextPath) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            return null;
        }
        String normalized = stripContextPath(mediaUrl.trim(), contextPath);
        if (!normalized.startsWith(SHIPMENT_EVIDENCE_PREFIX)) {
            return null;
        }
        String fileName = normalized.substring(SHIPMENT_EVIDENCE_PREFIX.length());
        if (!SAFE_FILENAME.matcher(fileName).matches()) {
            return null;
        }
        return fileName;
    }

    public static String normalizeShipmentEvidenceUrl(String mediaUrl, String contextPath) {
        String fileName = extractShipmentEvidenceFileName(mediaUrl, contextPath);
        if (fileName == null) {
            return null;
        }
        return SHIPMENT_EVIDENCE_PREFIX + fileName;
    }

    public static String guessContentType(Path file) {
        try {
            String type = Files.probeContentType(file);
            if (type != null) {
                return type;
            }
        } catch (IOException ignored) {
        }
        return "application/octet-stream";
    }

    private static String stripContextPath(String url, String contextPath) {
        if (url == null) {
            return "";
        }
        String trimmed = url.trim();
        if (contextPath != null && !contextPath.isEmpty() && !"/".equals(contextPath)) {
            if (trimmed.startsWith(contextPath + "/")) {
                trimmed = trimmed.substring(contextPath.length());
            }
        }
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        return trimmed;
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT).trim();
    }

    private static MediaCategory detectCategory(String contentType) {
        if (IMAGE_TYPES.containsKey(contentType)) {
            return MediaCategory.IMAGE;
        }
        if (VIDEO_TYPES.containsKey(contentType)) {
            return MediaCategory.VIDEO;
        }
        return MediaCategory.UNKNOWN;
    }

    private static String resolveExtension(Part part, String contentType, MediaCategory category) {
        String extension = null;
        if (category == MediaCategory.IMAGE) {
            extension = IMAGE_TYPES.get(contentType);
        } else if (category == MediaCategory.VIDEO) {
            extension = VIDEO_TYPES.get(contentType);
        }
        if (extension != null) {
            return extension;
        }
        String submittedName = part.getSubmittedFileName();
        if (submittedName != null) {
            int dot = submittedName.lastIndexOf('.');
            if (dot != -1 && dot < submittedName.length() - 1) {
                String candidate = submittedName.substring(dot + 1).toLowerCase(Locale.ROOT);
                if (SAFE_FILENAME.matcher(candidate).matches()) {
                    return candidate;
                }
            }
        }
        return category == MediaCategory.IMAGE ? "jpg" : "mp4";
    }

    private static String generateFileName(String extension) {
        String suffix = extension != null && !extension.isEmpty() ? "." + extension : "";
        String base = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().replace('-', '_');
        return base + suffix;
    }

    private enum MediaCategory {
        IMAGE,
        VIDEO,
        UNKNOWN
    }

    public static final class StoredFile {
        private final String publicUrl;
        private final String mediaType;
        private final String contentType;
        private final long size;
        private final Path filePath;

        private StoredFile(String publicUrl, String mediaType, String contentType, long size, Path filePath) {
            this.publicUrl = publicUrl;
            this.mediaType = mediaType;
            this.contentType = contentType;
            this.size = size;
            this.filePath = filePath;
        }

        public String getPublicUrl() {
            return publicUrl;
        }

        public String getMediaType() {
            return mediaType;
        }

        public String getContentType() {
            return contentType;
        }

        public long getSize() {
            return size;
        }

        public Path getFilePath() {
            return filePath;
        }
    }
}
