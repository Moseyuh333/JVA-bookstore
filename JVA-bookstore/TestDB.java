public class TestDB {
    public static void main(String[] args) {
        try {
            utils.DBUtil.getConnection();
            System.out.println("DB Connection successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
