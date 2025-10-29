package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Client for the Attachment Scanner Heroku add-on.
 */
public final class AttachmentScannerClient {

    private static final String ENV_API_KEY = "ATTACHMENT_SCANNER_API_KEY";
    private static final String ENV_BASE_URL = "ATTACHMENT_SCANNER_BASE_URL";
    private static final String DEFAULT_BASE_URL = "https://us-east-1.attachmentscanner.com";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();
    private static final Gson GSON = new Gson();

    private AttachmentScannerClient() {
    }

    public static ScanVerdict scanFile(Path file, String originalFilename, String contentType) throws IOException {
        Objects.requireNonNull(file, "file");
        Config config = Config.resolve();
        if (!config.enabled()) {
            return ScanVerdict.skipped("attachment-scanner-disabled");
        }
        byte[] data = Files.readAllBytes(file);
        String filename = sanitizeFilename(originalFilename);
        if (filename == null || filename.isEmpty()) {
            filename = "upload.bin";
        }
        String boundary = "----JvaAttachmentScanner" + UUID.randomUUID();
        HttpRequest.BodyPublisher body = buildMultipartBody(boundary, filename, normalizeContentType(contentType), data);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(config.scansUri())
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(body)
                .build();
        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Attachment scan interrupted", ex);
        }
        if (response == null) {
            throw new IOException("Attachment scanner did not return a response");
        }
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Attachment scanner returned HTTP " + statusCode);
        }
        return parseResponseBody(response.body());
    }

    private static HttpRequest.BodyPublisher buildMultipartBody(String boundary,
                                                                String filename,
                                                                String contentType,
                                                                byte[] data) {
        StringBuilder builder = new StringBuilder();
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"\r\n");
        builder.append("Content-Type: ").append(contentType).append("\r\n\r\n");
        byte[] prefix = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] suffix = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        return HttpRequest.BodyPublishers.ofByteArrays(List.of(prefix, data, suffix));
    }

    private static ScanVerdict parseResponseBody(String responseBody) throws IOException {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new IOException("Attachment scanner provided an empty response");
        }
        JsonElement parsed = JsonParser.parseString(responseBody);
        if (!parsed.isJsonObject()) {
            throw new IOException("Attachment scanner returned unexpected payload");
        }
        JsonObject root = parsed.getAsJsonObject();
        String status = asString(root, "status");
        JsonArray matches = null;
        if (root.has("matches") && root.get("matches").isJsonArray()) {
            matches = root.getAsJsonArray("matches");
        }
        boolean hasMatches = matches != null && matches.size() > 0;
        boolean suspicious = hasMatches
                || "infected".equalsIgnoreCase(status)
                || "blocked".equalsIgnoreCase(status)
                || "error".equalsIgnoreCase(status);
        if (!suspicious) {
            return ScanVerdict.clean(status != null ? status : "ok");
        }
        List<String> details = new ArrayList<>();
        if (matches != null) {
            for (JsonElement element : matches) {
                if (element == null || element.isJsonNull()) {
                    continue;
                }
                if (element.isJsonPrimitive()) {
                    details.add(element.getAsString());
                } else if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    String name = asString(obj, "name");
                    String signature = asString(obj, "signature");
                    if (name != null && signature != null) {
                        details.add(name + " (" + signature + ")");
                    } else if (name != null) {
                        details.add(name);
                    } else if (signature != null) {
                        details.add(signature);
                    } else {
                        details.add(GSON.toJson(obj));
                    }
                } else {
                    details.add(element.toString());
                }
            }
        }
        if (details.isEmpty() && status != null) {
            details.add(status);
        }
        String message = String.join(", ", details);
        return ScanVerdict.rejected(status != null ? status : "infected", message);
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return "application/octet-stream";
        }
        return contentType.trim();
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        String trimmed = filename.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < normalized.length() - 1) {
            normalized = normalized.substring(lastSlash + 1);
        }
        if (SAFE_FILENAME.matcher(normalized).matches()) {
            return normalized;
        }
        String cleaned = normalized.replaceAll("[^A-Za-z0-9._-]", "_");
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned;
    }

    private static String asString(JsonObject object, String property) {
        if (object == null || property == null || !object.has(property)) {
            return null;
        }
        JsonElement element = object.get(property);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        return element.toString();
    }

    private record Config(boolean enabled, String apiKey, URI scansUri) {
        static Config resolve() {
            String apiKeyValue = System.getenv(ENV_API_KEY);
            if (apiKeyValue == null || apiKeyValue.trim().isEmpty()) {
                return new Config(false, null, null);
            }
            String baseUrl = System.getenv(ENV_BASE_URL);
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                baseUrl = DEFAULT_BASE_URL;
            }
            String normalized = baseUrl.trim();
            if (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            URI scansUri = URI.create(normalized + "/v0.1/scans");
            return new Config(true, apiKeyValue.trim(), scansUri);
        }
    }

    public static final class ScanVerdict {
        private final boolean clean;
        private final boolean skipped;
        private final String status;
        private final String details;

        private ScanVerdict(boolean clean, boolean skipped, String status, String details) {
            this.clean = clean;
            this.skipped = skipped;
            this.status = status;
            this.details = details;
        }

        public static ScanVerdict clean(String status) {
            return new ScanVerdict(true, false, status, null);
        }

        public static ScanVerdict skipped(String status) {
            return new ScanVerdict(true, true, status, null);
        }

        public static ScanVerdict rejected(String status, String details) {
            return new ScanVerdict(false, false, status, details);
        }

        public boolean isClean() {
            return clean;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public boolean isRejected() {
            return !clean && !skipped;
        }

        public String getStatus() {
            return status;
        }

        public String getDetails() {
            return details;
        }
    }
}
