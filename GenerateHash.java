import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "123456";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Verify it works
        boolean isValid = BCrypt.checkpw(password, hash);
        System.out.println("Verification: " + isValid);
        
        // Test with shino password too
        String shinoPassword = "shino123"; // Replace with actual shino password
        String shinoHash = BCrypt.hashpw(shinoPassword, BCrypt.gensalt());
        System.out.println("\nShino Password: " + shinoPassword);
        System.out.println("Shino Hash: " + shinoHash);
    }
}
