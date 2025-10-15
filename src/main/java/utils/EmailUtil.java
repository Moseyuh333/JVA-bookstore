package utils;

import java.util.Properties;
import java.io.InputStream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

public class EmailUtil {
    private static Properties props;
    private static Session session;
    private static String smtpUser;
    private static String smtpPass;

    static {
        props = new Properties();
        String host = System.getenv("SMTP_HOST");
        if (host != null) {
            // Use environment variables (for Heroku)
            props.setProperty("mail.smtp.host", host);
            String port = System.getenv("SMTP_PORT");
            props.setProperty("mail.smtp.port", port != null ? port : "587");
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.starttls.required", "true");
            smtpUser = System.getenv("SMTP_USER");
            smtpPass = System.getenv("SMTP_PASS");
        } else {
            // Use email.properties file (for local)
            try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
                Properties tempProps = new Properties();
                tempProps.load(input);
                
                // Convert smtp.* to mail.smtp.* format
                props.setProperty("mail.smtp.host", tempProps.getProperty("smtp.host"));
                props.setProperty("mail.smtp.port", tempProps.getProperty("smtp.port", "587"));
                props.setProperty("mail.smtp.auth", tempProps.getProperty("mail.smtp.auth", "true"));
                props.setProperty("mail.smtp.starttls.enable", tempProps.getProperty("mail.smtp.starttls.enable", "true"));
                props.setProperty("mail.smtp.starttls.required", "true");
                
                smtpUser = tempProps.getProperty("smtp.user");
                smtpPass = tempProps.getProperty("smtp.pass");
            } catch (IOException e) {
                System.err.println("Failed to load email.properties: " + e.getMessage());
            }
        }
        
        // Debug: print configuration
        System.out.println("=== Email Configuration ===");
        System.out.println("SMTP Host: " + props.getProperty("mail.smtp.host"));
        System.out.println("SMTP Port: " + props.getProperty("mail.smtp.port"));
        System.out.println("SMTP Username: " + (smtpUser != null ? smtpUser.substring(0, Math.min(8, smtpUser.length())) + "..." : "NULL"));
        System.out.println("SMTP Password: " + (smtpPass != null ? "***" + smtpPass.substring(Math.max(0, smtpPass.length()-4)) : "NULL"));

        session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPass);
            }
        });
        
        // Enable debug mode for JavaMail (will show SMTP conversation)
        session.setDebug(true);
    }

    public static void sendVerificationEmail(String toEmail, String token, String username) {
        String subject = "Xác nhận tài khoản NKBookstore - Verification Required";
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
        
        String body = "Chào " + username + ",\n\n" +
                      "Cảm ơn bạn đã đăng ký tài khoản tại NKBookstore!\n\n" +
                      "Để hoàn tất quá trình đăng ký, vui lòng click vào liên kết bên dưới để xác nhận email:\n" +
                      verificationUrl + "\n\n" +
                      "Liên kết này có hiệu lực trong 24 giờ.\n" +
                      "Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.\n\n" +
                      "Trân trọng,\n" +
                      "Đội ngũ NKBookstore\n" +
                      "📚 Kho sách trực tuyến hàng đầu Việt Nam";

        sendEmail(toEmail, subject, body);
    }

    public static void sendResetEmail(String toEmail, String token, String username) {
        String subject = "Đặt lại mật khẩu NKBookstore - Password Reset Request";
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
        String resetUrl = baseUrl + "/reset-password.jsp?token=" + token;
        
        String body = "Chào " + username + ",\n\n" +
                      "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại NKBookstore.\n\n" +
                      "Để tạo mật khẩu mới, vui lòng click vào liên kết bên dưới:\n" +
                      resetUrl + "\n\n" +
                      "⚠️  Liên kết này có hiệu lực trong 1 giờ.\n" +
                      "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n" +
                      "Để bảo mật tài khoản, không chia sẻ liên kết này với bất kỳ ai.\n\n" +
                      "Trân trọng,\n" +
                      "Đội ngũ NKBookstore\n" +
                      "📚 Kho sách trực tuyến hàng đầu Việt Nam";

        sendEmail(toEmail, subject, body);
    }

    public static void testEmailConnection(String testEmail) {
        String subject = "NKBookstore - Test Email Configuration";
        String body = "Chào bạn,\n\n" +
                      "Đây là email test để kiểm tra cấu hình MailerToGo.\n" +
                      "Nếu bạn nhận được email này, cấu hình email đã hoạt động thành công!\n\n" +
                      "Trân trọng,\n" +
                      "Đội ngũ NKBookstore";
        
        sendEmail(testEmail, subject, body);
    }

    private static void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("=== Starting Email Send ===");
            System.out.println("SMTP Host: " + props.getProperty("mail.smtp.host"));
            System.out.println("SMTP Port: " + props.getProperty("mail.smtp.port"));
            System.out.println("SMTP Auth: " + props.getProperty("mail.smtp.auth"));
            System.out.println("SMTP TLS: " + props.getProperty("mail.smtp.starttls.enable"));
            
            Message message = new MimeMessage(session);
            String from = System.getenv("SMTP_FROM") != null ? System.getenv("SMTP_FROM") : props.getProperty("smtp.from");
            System.out.println("From: " + from);
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            System.out.println("Sending email via Transport...");
            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + to + " via MailerToGo SMTP");
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to " + to);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }
}
