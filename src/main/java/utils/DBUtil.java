package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class DBUtil {
    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            String databaseUrl = System.getenv("DATABASE_URL");
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                // Expected format: postgres://user:pass@host:port/db
                URI dbUri = new URI(databaseUrl);
                username = dbUri.getUserInfo().split(":")[0];
                password = dbUri.getUserInfo().split(":")[1];
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + (dbUri.getPort() != -1 ? ":" + dbUri.getPort() : "") + dbUri.getPath();
                // Ensure SSL for Heroku
                url = jdbcUrl + "?sslmode=require";
            } else {
                try (InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
                    Properties prop = new Properties();
                    prop.load(input);
                    url = prop.getProperty("db.url");
                    username = prop.getProperty("db.username");
                    password = prop.getProperty("db.password");
                }
            }
            Class.forName("org.postgresql.Driver");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid DATABASE_URL", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}