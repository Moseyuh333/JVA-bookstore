package web;

import utils.FileStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(name = "ReviewMediaServlet", urlPatterns = "/media/reviews/*")
public class ReviewMediaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serveMedia(req, resp, false);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serveMedia(req, resp, true);
    }

    private void serveMedia(HttpServletRequest request, HttpServletResponse response, boolean headOnly) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String fileName = FileStorageUtil.extractReviewMediaFileNameFromPathInfo(pathInfo);
        if (fileName == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path file = FileStorageUtil.reviewMediaPath(fileName);
        if (!Files.exists(file) || Files.isDirectory(file)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long size = Files.size(file);
        response.setContentType(FileStorageUtil.guessContentType(file));
        response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        response.setHeader("Content-Length", String.valueOf(size));
        if (headOnly) {
            return;
        }
        try (InputStream in = Files.newInputStream(file); OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
