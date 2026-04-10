package web;

import com.google.gson.Gson;
import utils.GsonUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "XmlPingServlet", urlPatterns = {"/api/xml-ping"})
public class XmlPingServlet extends HttpServlet {

    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read raw body
            StringBuilder xmlBuilder = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    xmlBuilder.append(line);
                }
            }
            String xmlData = xmlBuilder.toString();

            if (xmlData.trim().isEmpty()) {
                sendError(response, "XML payload is required");
                return;
            }

            // VULNERABLE: XXE Configuration
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // LỖ HỔNG: KHÔNG cấu hình bảo mật. Cho phép phân giải External Entities (DTD)
            // Các thiết lập bảo mật BỊ THIẾU dưới đây:
            // dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            // dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            // Trích xuất nội dung thẻ <ping>...</ping>
            String pingResult = "Vui lòng truyền thẻ <ping>Nội dung</ping>";
            if (doc.getElementsByTagName("ping").getLength() > 0) {
                Element pingElement = (Element) doc.getElementsByTagName("ping").item(0);
                pingResult = pingElement.getTextContent();
            }

            // Trả về JSON 
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Ping received");
            result.put("echo", pingResult);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error parsing XML: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(error));
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(error));
    }
}
