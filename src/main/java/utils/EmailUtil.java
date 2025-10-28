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
    private static boolean emailEnabled;
    private static boolean debugMode;

    static {
        props = new Properties();
        emailEnabled = false;
        debugMode = Boolean.parseBoolean(System.getenv("EMAIL_DEBUG"));
        boolean disableRequested = Boolean.parseBoolean(System.getenv("EMAIL_DISABLED"));

        try {
            String host = System.getenv("SMTP_HOST");
            if (host != null && !host.isEmpty()) {
                props.setProperty("mail.smtp.host", host);
                String port = System.getenv("SMTP_PORT");
                props.setProperty("mail.smtp.port", port != null ? port : "587");
                props.setProperty("mail.smtp.auth", "true");
                props.setProperty("mail.smtp.starttls.enable", "true");
                props.setProperty("mail.smtp.starttls.required", "true");

                // 2 dòng bắt buộc cho SendGrid
                props.setProperty("mail.smtp.ssl.trust", "smtp.sendgrid.net");
                props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

                smtpUser = System.getenv("SMTP_USER");
                smtpPass = System.getenv("SMTP_PASS");
                smtpFrom = System.getenv("SMTP_FROM");
            } else {
                try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
                    if (input == null)
                        throw new IOException("email.properties not found in classpath");
                    Properties tempProps = new Properties();
                    tempProps.load(input);
                    disableRequested = disableRequested
                            || Boolean.parseBoolean(tempProps.getProperty("email.disabled", "false"));
                    debugMode = debugMode || Boolean.parseBoolean(tempProps.getProperty("mail.debug", "false"));

                    props.setProperty("mail.smtp.host", tempProps.getProperty("smtp.host"));
                    props.setProperty("mail.smtp.port", tempProps.getProperty("smtp.port", "587"));
                    props.setProperty("mail.smtp.auth", "true");
                    props.setProperty("mail.smtp.starttls.enable", "true");
                    props.setProperty("mail.smtp.starttls.required", "true");
                    props.setProperty("mail.smtp.ssl.trust", "smtp.sendgrid.net");
                    props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

                    smtpUser = tempProps.getProperty("smtp.user");
                    smtpPass = tempProps.getProperty("smtp.pass");
                    smtpFrom = tempProps.getProperty("smtp.from");
                }
            }

            if (disableRequested) {
                System.out.println("EmailUtil - Email delivery disabled by configuration.");
                emailEnabled = false;
            } else if (isBlank(smtpUser) || isBlank(smtpPass) || isBlank(smtpFrom)) {
                System.err.println("EmailUtil - Missing SMTP credentials. Email notifications disabled.");
                emailEnabled = false;
            } else {
                session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUser, smtpPass);
                    }
                });
                session.setDebug(debugMode);
                emailEnabled = true;

                System.out.println("=== Email Configuration (SendGrid SMTP) ===");
                System.out.println("SMTP Host: " + props.getProperty("mail.smtp.host"));
                System.out.println("SMTP Port: " + props.getProperty("mail.smtp.port"));
                System.out.println("SMTP Username: " + smtpUser);
                System.out.println("SMTP From: " + smtpFrom);
                System.out.println("Debug Mode: " + debugMode);
                System.out.println("==========================================");
            }
        } catch (IOException e) {
            System.err.println("EmailUtil - Failed to load email configuration: " + e.getMessage());
            emailEnabled = false;
        }

        if (!emailEnabled) {
            System.out.println("EmailUtil - Running in DEV mode, emails will be logged to the console only.");
        }
    }

    // ========================= EMAIL TYPES =========================

    public static void sendVerificationEmail(String toEmail, String token, String username) {
        String subject = "Xác nhận tài khoản - Bookish Bliss Haven";
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

        String body = "Chào " + username + ",\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản tại Bookish Bliss Haven!\n\n" +
                "Vui lòng nhấn vào liên kết bên dưới để xác nhận email:\n" +
                verificationUrl + "\n\n" +
                "Liên kết này có hiệu lực trong 24 giờ.\n" +
                "Trân trọng,\nĐội ngũ Bookish Bliss Haven 📚";
        sendEmail(toEmail, subject, body);
    }

    public static void sendOTPEmail(String toEmail, String otp) {
        String subject = "🔐 Mã xác nhận đăng ký - Bookish Bliss Haven";
        String body = "Chào bạn,\n\n" +
                "Mã OTP của bạn là: " + otp + "\n\n" +
                "⏰ Mã có hiệu lực trong 10 phút.\n🔒 Vui lòng KHÔNG chia sẻ mã này với ai.\n\n" +
                "Trân trọng,\nĐội ngũ Bookish Bliss Haven 📚";
        sendEmail(toEmail, subject, body);
    }

    public static void sendWelcomeEmail(String toEmail, String username) {
        String subject = "🎉 Chào mừng đến với Bookish Bliss Haven!";
        String body = "Chào " + username + ",\n\n" +
                "🎉 Tài khoản của bạn đã được tạo thành công!\n" +
                "Hãy đăng nhập và bắt đầu khám phá kho sách khổng lồ của Bookish Bliss Haven.\n\n" +
                "Trân trọng,\nĐội ngũ Bookish Bliss Haven 📚";
        sendEmail(toEmail, subject, body);
    }

    public static void sendResetEmail(String toEmail, String token, String username) {
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
        String resetUrl = baseUrl + "/reset-password.jsp?token=" + token;

        String subject = "Đặt lại mật khẩu - Bookish Bliss Haven";
        String body = "Chào " + username + ",\n\n" +
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại Bookish Bliss Haven.\n\n" +
                "Nhấn vào liên kết bên dưới để tạo mật khẩu mới:\n" +
                resetUrl + "\n\n" +
                "⚠️ Liên kết này có hiệu lực trong 1 giờ.\n\n" +
                "Trân trọng,\nĐội ngũ Bookish Bliss Haven 📚";
        sendEmail(toEmail, subject, body);
    }

    // ========================= CORE EMAIL METHOD =========================

    private static void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            System.out.println("[Email disabled] " + subject + " -> " + to);
            System.out.println(body);
            return;
        }
        try {
            System.out.println("=== Sending Email ===");
            System.out.println("From: " + smtpFrom + " | To: " + to + " | Subject: " + subject);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // ✅ dùng Transport.send() có session
            Transport transport = session.getTransport("smtp");
            transport.connect(props.getProperty("mail.smtp.host"), smtpUser, smtpPass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            System.out.println("✅ Email sent successfully to " + to + " via SendGrid SMTP");
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to " + to);
            e.printStackTrace();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // =====================================================
    // Hàm test kết nối và gửi mail thử qua SendGrid SMTP
    // =====================================================
    public static void testEmailConnection(String testEmail) {
        String subject = "📧 Test Email - Bookish Bliss Haven";
        String body = "Xin chào,\n\n"
                + "Đây là email test được gửi từ hệ thống Bookish Bliss Haven.\n"
                + "Nếu bạn nhận được email này, cấu hình SendGrid SMTP đã hoạt động thành công 🎉\n\n"
                + "Trân trọng,\nĐội ngũ Bookish Bliss Haven 📚";

        if (!emailEnabled) {
            System.out.println("[Test Email] Email sending is currently disabled.");
            System.out.println("Would send to: " + testEmail);
            System.out.println("Body:\n" + body);
            return;
        }

        try {
            System.out.println("=== Testing SendGrid SMTP connection ===");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(testEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport transport = session.getTransport("smtp");
            transport.connect(props.getProperty("mail.smtp.host"), smtpUser, smtpPass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            System.out.println("✅ Test email sent successfully to " + testEmail);
        } catch (Exception e) {
            System.err.println("❌ Test email failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
