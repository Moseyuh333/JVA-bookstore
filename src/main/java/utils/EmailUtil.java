package utils;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.InputStream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;

public class EmailUtil {
    private static Properties props;
    private static Session session;
    private static String smtpUser;
    private static String smtpPass;
    // EmailJS config (optional)
    private static String EMAILJS_SERVICE_ID;
    private static String EMAILJS_PUBLIC_KEY;
    private static String EMAILJS_TEMPLATE_REGISTER;
    private static String EMAILJS_TEMPLATE_RESET;
    private static String EMAILJS_FROM_NAME;

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
        
        // Load EmailJS configuration (if present)
        EMAILJS_SERVICE_ID = getenvOrProp("EMAILJS_SERVICE_ID", null);
        EMAILJS_PUBLIC_KEY = getenvOrProp("EMAILJS_PUBLIC_KEY", null);
        EMAILJS_TEMPLATE_REGISTER = getenvOrProp("EMAILJS_TEMPLATE_REGISTER", "template_pf8qw9d");
        EMAILJS_TEMPLATE_RESET = getenvOrProp("EMAILJS_TEMPLATE_RESET", "template_sjv9tjr");
        EMAILJS_FROM_NAME = getenvOrProp("EMAILJS_FROM_NAME", "Nkbookstore");

        // Debug: print configuration
        System.out.println("=== Email Configuration ===");
        System.out.println("SMTP Host: " + props.getProperty("mail.smtp.host"));
        System.out.println("SMTP Port: " + props.getProperty("mail.smtp.port"));
        System.out.println("SMTP Username: " + (smtpUser != null ? smtpUser.substring(0, Math.min(8, smtpUser.length())) + "..." : "NULL"));
        System.out.println("SMTP Password: " + (smtpPass != null ? "***" + smtpPass.substring(Math.max(0, smtpPass.length()-4)) : "NULL"));
        System.out.println("EmailJS Enabled: " + (isEmailJSConfigured() ? "YES" : "NO"));
        if (isEmailJSConfigured()) {
            System.out.println("EmailJS Service: " + EMAILJS_SERVICE_ID + ", RegisterTpl: " + EMAILJS_TEMPLATE_REGISTER + ", ResetTpl: " + EMAILJS_TEMPLATE_RESET);
        }

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
        if (isEmailJSConfigured()) {
            Map<String, String> params = new HashMap<>();
            params.put("email", toEmail);
            params.put("passcode", otp);
            // 10-minute expiry to match backend logic
            String time = LocalDateTime.now().plusMinutes(10)
                    .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            params.put("time", time);
            sendEmailJS(EMAILJS_TEMPLATE_REGISTER, params);
            return;
        }

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

        if (isEmailJSConfigured()) {
            Map<String, String> params = new HashMap<>();
            params.put("email", toEmail);
            params.put("link", resetUrl);
            sendEmailJS(EMAILJS_TEMPLATE_RESET, params);
            return;
        }

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
        if (isEmailJSConfigured()) {
            // For test or generic messages you can create a general template or fallback to SMTP
            // Here we fallback to SMTP to avoid requiring an extra EmailJS template.
        }
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

    private static boolean isEmailJSConfigured() {
        return EMAILJS_SERVICE_ID != null && !EMAILJS_SERVICE_ID.isEmpty()
                && EMAILJS_PUBLIC_KEY != null && !EMAILJS_PUBLIC_KEY.isEmpty();
    }

    private static String getenvOrProp(String key, String defaultVal) {
        String v = System.getenv(key);
        if (v != null && !v.isEmpty()) return v;
        if (props != null) {
            String dotted = key.toLowerCase().replace('_', '.'); // e.g. EMAILJS_SERVICE_ID -> emailjs.service.id
            // try exact dotted key
            String v1 = props.getProperty(dotted);
            if (v1 != null && !v1.isEmpty()) return v1;
            // try with 'emailjs.' prefix if not already prefixed
            String withoutPrefix = dotted.startsWith("emailjs.") ? dotted.substring("emailjs.".length()) : dotted;
            String v2 = props.getProperty("emailjs." + withoutPrefix);
            if (v2 != null && !v2.isEmpty()) return v2;
        }
        return defaultVal;
    }

    private static void sendEmailJS(String templateId, Map<String, String> templateParams) {
        try {
            URL url = new URL("https://api.emailjs.com/api/v1.0/email/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("service_id", EMAILJS_SERVICE_ID);
            payload.put("template_id", templateId);
            payload.put("user_id", EMAILJS_PUBLIC_KEY); // public key
            // Add a default from_name if template supports it
            templateParams.putIfAbsent("from_name", EMAILJS_FROM_NAME);
            payload.put("template_params", templateParams);

            String json = new Gson().toJson(payload);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                System.out.println("✅ EmailJS send success (template=" + templateId + ") to " + templateParams.get("email"));
            } else {
                System.err.println("❌ EmailJS send failed with HTTP " + code + ". Falling back to SMTP if applicable.");
                throw new RuntimeException("EmailJS send failed: HTTP " + code);
            }
        } catch (Exception ex) {
            throw new RuntimeException("EmailJS error: " + ex.getMessage(), ex);
        }
    }
}
