package web;

import dao.BookDAO;
import models.Book;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@WebServlet("/admin/import-books")
public class ImportBooksServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Import Books</title></head><body>");
        out.println("<h2>Import Books from CSV</h2>");
        out.println("<form method='POST'>");
        out.println("<button type='submit' style='padding: 10px 20px; font-size: 16px;'>Start Import</button>");
        out.println("</form>");
        out.println("</body></html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Importing...</title></head><body>");
        out.println("<h2>Đang import sách...</h2><pre>");
        
        // Đường dẫn file CSV
        String csvFile = getServletContext().getRealPath("/") + "../../../books_full_500.csv";
        File file = new File(csvFile);
        
        // Nếu không tìm thấy, thử path khác
        if (!file.exists()) {
            csvFile = "books_full_500.csv";
            file = new File(csvFile);
        }
        
        if (!file.exists()) {
            out.println("ERROR: Không tìm thấy file books_full_500.csv");
            out.println("Đã thử: " + file.getAbsolutePath());
            out.println("</pre></body></html>");
            return;
        }
        
        out.println("✓ Tìm thấy file: " + file.getAbsolutePath());
        out.flush();
        
        int successCount = 0;
        int errorCount = 0;
        int lineNumber = 0;
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            // Bỏ qua dòng header
            String headerLine = br.readLine();
            lineNumber++;
            out.println("✓ Header: " + headerLine);
            out.flush();
            
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                try {
                    // Parse CSV line (xử lý cả trường hợp có dấu phay trong quotes)
                    String[] fields = parseCSVLine(line);
                    
                    if (fields.length < 18) {
                        out.println("⚠ Dòng " + lineNumber + " thiếu dữ liệu (chỉ có " + fields.length + " cột)");
                        errorCount++;
                        continue;
                    }
                    
                    // Tạo Book object
                    Book book = new Book();
                    
                    // Parse các field
                    book.setTitle(cleanField(fields[1])); // title
                    book.setAuthor(cleanField(fields[2])); // author
                    
                    // Price (dùng giá hiện tại, bỏ qua original_price và discount)
                    try {
                        double priceValue = parseDouble(fields[3]);
                        book.setPrice(new java.math.BigDecimal(priceValue));
                    } catch (Exception e) {
                        book.setPrice(java.math.BigDecimal.ZERO);
                    }
                    
                    // Average rating
                    try {
                        book.setAverageRating(parseDouble(fields[6]));
                    } catch (Exception e) {
                        book.setAverageRating(0.0);
                    }
                    
                    // Rating count
                    try {
                        book.setRatingCount(parseInt(fields[7]));
                    } catch (Exception e) {
                        book.setRatingCount(0);
                    }
                    
                    // Stock quantity (available/out_of_stock)
                    String stockStatus = cleanField(fields[8]);
                    book.setStockQuantity("available".equalsIgnoreCase(stockStatus) ? 100 : 0);
                    
                    // Skip publisher (field 9) - model doesn't have this field
                    book.setCategory(cleanField(fields[10])); // category
                    book.setImageUrl(cleanField(fields[11])); // cover_image -> imageUrl
                    book.setDescription(cleanField(fields[16])); // description
                    
                    // Set timestamps
                    book.setCreatedAt(LocalDateTime.now());
                    book.setUpdatedAt(LocalDateTime.now());
                    
                    // Set default values
                    book.setViewsCount(0);
                    book.setSalesCount(0);
                    
                    // Validate required fields
                    if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                        out.println("⚠ Dòng " + lineNumber + " thiếu title");
                        errorCount++;
                        continue;
                    }
                    
                    // Insert vào database
                    int bookId = BookDAO.createBook(book);
                    
                    if (bookId > 0) {
                        successCount++;
                        if (successCount % 50 == 0) {
                            out.println("✓ Đã import " + successCount + " sách...");
                            out.flush();
                        }
                    } else {
                        errorCount++;
                        if (errorCount <= 10) {
                            out.println("✗ Lỗi dòng " + lineNumber + ": " + book.getTitle());
                        }
                    }
                    
                } catch (Exception e) {
                    errorCount++;
                    if (errorCount <= 10) {
                        out.println("✗ Lỗi dòng " + lineNumber + ": " + e.getMessage());
                    }
                }
            }
            
            out.println("\n" + "=".repeat(60));
            out.println("KẾT QUẢ:");
            out.println("✓ Thành công: " + successCount + " sách");
            out.println("✗ Lỗi: " + errorCount + " dòng");
            out.println("📊 Tổng số dòng xử lý: " + (lineNumber - 1));
            out.println("=".repeat(60));
            
        } catch (IOException e) {
            out.println("ERROR: " + e.getMessage());
            e.printStackTrace(out);
        }
        
        out.println("</pre>");
        out.println("<br><a href='/'>← Quay về trang chủ</a>");
        out.println("</body></html>");
    }
    
    /**
     * Parse CSV line with support for quoted fields containing commas
     */
    private String[] parseCSVLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
    
    private String cleanField(String field) {
        if (field == null) return null;
        field = field.trim();
        if (field.isEmpty()) return null;
        // Remove quotes if present
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }
        return field;
    }
    
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        value = cleanField(value);
        return Double.parseDouble(value);
    }
    
    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        value = cleanField(value);
        // Handle decimal strings like "0.0"
        double d = Double.parseDouble(value);
        return (int) d;
    }
}
