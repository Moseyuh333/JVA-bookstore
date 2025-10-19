package filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ensures all requests and responses use UTF-8 so Vietnamese characters render correctly everywhere.
 */
public class EncodingFilter implements Filter {

    private static final String UTF8 = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getCharacterEncoding() == null || !UTF8.equalsIgnoreCase(request.getCharacterEncoding())) {
            request.setCharacterEncoding(UTF8);
        }
        response.setCharacterEncoding(UTF8);
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) response;
            String contentType = httpResp.getContentType();
            if (contentType != null && contentType.startsWith("text/")) {
                httpResp.setContentType(contentType.split(";")[0] + "; charset=" + UTF8);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to clean up.
    }
}
