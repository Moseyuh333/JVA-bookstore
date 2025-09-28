import utils.DBUtil;
import java.sql.Connection;

public class DBTest {
    public static void main(String[] args) {
        try {
            Connection conn = DBUtil.getConnection();
            System.out.println("Database connection successful!");
            conn.close();
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
