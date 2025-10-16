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
    private static String smtpFrom;

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
            smtpFrom = System.getenv("SMTP_FROM");
        } else {
            // Use email.properties file (for local)
            try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
                if (input == null) {
                    throw new RuntimeException("email.properties not found in classpath");
                }
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
                smtpFrom = tempProps.getProperty("smtp.from");
            } catch (IOException e) {
                System.err.println("Failed to load email.properties: " + e.getMessage());
                throw new RuntimeException("Email configuration failed", e);
            }
        }

        // Debug: print configuration
        System.out.println("=== Email Configuration (SMTP only) ===");
        System.out.println("SMTP Host: " + props.getProperty("mail.smtp.host"));
        System.out.println("SMTP Port: " + props.getProperty("mail.smtp.port"));
        System.out.println("SMTP Username: " + (smtpUser != null ? smtpUser.substring(0, Math.min(8, smtpUser.length())) + "..." : "NULL"));
        System.out.println("SMTP Password: " + (smtpPass != null ? "***" + smtpPass.substring(Math.max(0, smtpPass.length()-4)) : "NULL"));
        System.out.println("SMTP From: " + smtpFrom);
        System.out.println("======================================");

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
    
    public static void sendOTPEmail(String toEmail, String otp) {
        String subject = "🔐 Mã xác nhận đăng ký NKBookstore";
        
        String body = "Chào bạn,\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản tại NKBookstore!\n\n" +
                "Mã OTP của bạn là: " + otp + "\n\n" +
                "⏰ Mã này có hiệu lực trong 10 phút.\n" +
                "🔒 Vui lòng KHÔNG chia sẻ mã này với bất kỳ ai.\n\n" +
                "Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ NKBookstore\n" +
                "📚 Kho sách trực tuyến hàng đầu Việt Nam";

        sendEmail(toEmail, subject, body);
    }
    
    public static void sendWelcomeEmail(String toEmail, String username) {
        String subject = "🎉 Chào mừng đến với NKBookstore!";
        
        String body = "Chào " + username + ",\n\n" +
                      "🎉 Chào mừng bạn đến với NKBookstore!\n\n" +
                      "Tài khoản của bạn đã được tạo thành công và đã được kích hoạt.\n" +
                      "Bạn có thể đăng nhập ngay bằng username và password đã đăng ký.\n\n" +
                      "📚 Khám phá hàng ngàn đầu sách hay tại NKBookstore\n" +
                      "🎁 Nhận ưu đãi đặc biệt cho thành viên mới\n\n" +
                      "Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.\n\n" +
                      "Trân trọng,\n" +
                      "Đội ngũ NKBookstore\n" +
                      "📚 Kho sách trực tuyến hàng đầu Việt Nam";

        sendEmail(toEmail, subject, body);
    }

    public static void sendResetEmail(String toEmail, String token, String username) {
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
        String resetUrl = baseUrl + "/reset-password.jsp?token=" + token;

        String subject = "Đặt lại mật khẩu NKBookstore - Password Reset Request";
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
            System.out.println("From: " + smtpFrom);
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            
            message.setFrom(new InternetAddress(smtpFrom));
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
