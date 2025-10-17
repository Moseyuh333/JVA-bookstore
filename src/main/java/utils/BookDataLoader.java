package utils;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility responsible for seeding the {@code books} table with demo data the first time the
 * application starts. It reads the {@code books_full_5xx.csv} dataset either from the classpath
 * (recommended) or, as a fallback, from common development filesystem locations.
 */
public final class BookDataLoader {

    private static final String[] CLASSPATH_CANDIDATES = {
        "data/books_full_5xx.csv",
        "books_full_5xx.csv"
    };

    private static final String[] FILESYSTEM_CANDIDATES = {
        "books_full_5xx.csv",
        "src/main/resources/books_full_5xx.csv",
        "src/main/resources/data/books_full_5xx.csv"
    };

    private BookDataLoader() {
    }

    /**
     * Import book records from CSV when the {@code books} table is empty. Any errors encountered
     * during parsing are logged and the seeding is aborted to avoid leaving the database in a
     * partial state.
     */
    public static void seedBooksIfEmpty(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (hasExistingBooks(connection)) {
                return;
            }
        } catch (SQLException countEx) {
            System.err.println("BookDataLoader - Failed to inspect books table: " + countEx.getMessage());
            return;
        }

        try (InputStream csvStream = locateCsvStream()) {
            if (csvStream == null) {
                System.out.println("BookDataLoader - CSV source not found, skipping automatic seeding.");
                return;
            }

            boolean originalAutoCommit = connection.getAutoCommit();
            if (originalAutoCommit) {
                connection.setAutoCommit(false);
            }

            int inserted = 0;
            try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvStream, StandardCharsets.UTF_8))
                    .withCSVParser(new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build())
                    .build();
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO books (title, author, price, description, category, stock_quantity, image_url) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?)")
            ) {
                // Skip header row
                reader.readNext();

                String[] row;
                int batchSize = 0;
                while ((row = reader.readNext()) != null) {
                    if (row.length < 2) {
                        continue;
                    }

                    try {
                        String title = sanitizeText(getValue(row, 1), 255);
                        if (title == null || title.isEmpty()) {
                            continue;
                        }

                        String author = sanitizeText(getValue(row, 2), 255);
                        BigDecimal price = parsePrice(getValue(row, 3), getValue(row, 4));
                        String description = sanitizeText(getValue(row, 16), 8000);
                        String category = sanitizeText(getValue(row, 10), 100);
                        int stockQty = resolveStockQuantity(getValue(row, 8));
                        String imageUrl = sanitizeText(getValue(row, 11), 500);

                        insert.setString(1, title);
                        insert.setString(2, author);
                        if (price != null) {
                            insert.setBigDecimal(3, price);
                        } else {
                            insert.setNull(3, java.sql.Types.NUMERIC);
                        }
                        insert.setString(4, description);
                        insert.setString(5, category);
                        insert.setInt(6, stockQty);
                        insert.setString(7, imageUrl);

                        insert.addBatch();
                        batchSize++;
                        inserted++;

                        if (batchSize >= 250) {
                            insert.executeBatch();
                            batchSize = 0;
                        }
                    } catch (Exception rowEx) {
                        System.err.println("BookDataLoader - Skipping row due to parsing error: " + rowEx.getMessage());
                    }
                }

                if (batchSize > 0) {
                    insert.executeBatch();
                }

                connection.commit();
                System.out.println("BookDataLoader - Seeded " + inserted + " books from CSV dataset.");
            } catch (Exception processingEx) {
                connection.rollback();
                System.err.println("BookDataLoader - Failed to seed books, changes rolled back: " + processingEx.getMessage());
            } finally {
                if (originalAutoCommit) {
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException | IOException ex) {
            System.err.println("BookDataLoader - Error while loading CSV: " + ex.getMessage());
        }
    }

    private static boolean hasExistingBooks(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(1) FROM books")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static InputStream locateCsvStream() throws IOException {
        ClassLoader classLoader = BookDataLoader.class.getClassLoader();
        for (String resource : CLASSPATH_CANDIDATES) {
            InputStream stream = classLoader.getResourceAsStream(resource);
            if (stream != null) {
                System.out.println("BookDataLoader - Loading books from classpath resource: " + resource);
                return stream;
            }
        }

        for (String candidate : FILESYSTEM_CANDIDATES) {
            Path path = Paths.get(candidate);
            if (Files.exists(path)) {
                System.out.println("BookDataLoader - Loading books from filesystem path: " + path.toAbsolutePath());
                return Files.newInputStream(path);
            }
        }
        return null;
    }

    private static String getValue(String[] row, int index) {
        return index >= 0 && index < row.length ? row[index] : null;
    }

    private static String sanitizeText(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        String sanitized = input.replace('\u0000', ' ').trim();
        if (sanitized.isEmpty()) {
            return null;
        }
        if (maxLength > 0 && sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength);
        }
        return sanitized;
    }

    private static BigDecimal parsePrice(String price, String fallback) {
        String candidate = price;
        if ((candidate == null || candidate.isBlank()) && fallback != null && !fallback.isBlank()) {
            candidate = fallback;
        }
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(candidate.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int resolveStockQuantity(String stockValue) {
        if (stockValue == null || stockValue.isBlank()) {
            return 0;
        }
        String normalized = stockValue.trim().toLowerCase();
        if ("available".equals(normalized) || "in_stock".equals(normalized) || "instock".equals(normalized)) {
            return 100;
        }
        try {
            int parsed = Integer.parseInt(normalized);
            return Math.max(parsed, 0);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Ensure every book record points to a valid storefront asset so the UI can render fallback
     * imagery even when the dataset does not provide explicit image URLs.
     */
    public static void refreshBookAssets(Connection connection) {
        if (connection == null) {
            return;
        }
        String placeholder = "/assets/img/nkbookstore-logo.png";
        String sql = "UPDATE books SET image_url = ? WHERE image_url IS NULL OR image_url = ''";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, placeholder);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("BookDataLoader - Applied placeholder image to " + updated + " book(s).");
            }
        } catch (SQLException ex) {
            System.err.println("BookDataLoader - Unable to refresh book assets: " + ex.getMessage());
        }
    }
}
