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

    static {
        props = new Properties();
        String host = System.getenv("SMTP_HOST");
        if (host != null) {
            props.setProperty("mail.smtp.host", host);
            String port = System.getenv("SMTP_PORT");
            props.setProperty("mail.smtp.port", port != null ? port : "587");
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.starttls.enable", "true");
        } else {
            try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Failed to load email.properties: " + e.getMessage());
            }
        }

        final String username = System.getenv("SMTP_USER") != null ? System.getenv("SMTP_USER") : props.getProperty("smtp.user");
        final String password = System.getenv("SMTP_PASS") != null ? System.getenv("SMTP_PASS") : props.getProperty("smtp.pass");

        session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public static void sendVerificationEmail(String toEmail, String token, String username) {
        String subject = "Verify your NKbookstore account";
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "https://jva-bookstore.herokuapp.com";
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
        String body = "Hello " + username + ",\n\n" +
                      "Please click the following link to verify your email:\n" +
                      verificationUrl + "\n\n" +
                      "If you did not create an account, ignore this email.\n\n" +
                      "Best regards,\nNKbookstore Team";

        sendEmail(toEmail, subject, body);
    }

    public static void sendResetEmail(String toEmail, String token, String username) {
        String subject = "Reset your NKbookstore password";
        String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "https://jva-bookstore.herokuapp.com";
        String resetUrl = baseUrl + "/reset-password.jsp?token=" + token;
        String body = "Hello " + username + ",\n\n" +
                      "You requested a password reset. Click the link below to set a new password:\n" +
                      resetUrl + "\n\n" +
                      "This link expires in 1 hour.\n" +
                      "If you did not request this, ignore this email.\n\n" +
                      "Best regards,\nNKbookstore Team";

        sendEmail(toEmail, subject, body);
    }

    private static void sendEmail(String to, String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            String from = System.getenv("SMTP_FROM") != null ? System.getenv("SMTP_FROM") : props.getProperty("smtp.from");
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
