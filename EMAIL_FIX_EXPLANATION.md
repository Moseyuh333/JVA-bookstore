# 🐛 Vấn đề Email không gửi được

## ❌ Triệu chứng:
- Server logs báo: `✅ Email sent successfully`
- MailerToGo dashboard: `0 emails sent`
- Gmail không nhận được email

## 🔍 Nguyên nhân:
**JavaMail Session không được authenticate đúng** vì:

1. Properties format không đúng (`smtp.*` thay vì `mail.smtp.*`)
2. Username/Password không được load đúng cách từ properties
3. Session debug mode chưa bật → không thấy lỗi SMTP

## ✅ Giải pháp đã áp dụng:

### 1. Fix Properties Format:
```java
// ❌ SAI - JavaMail không nhận dạng
props.load(input); // Load trực tiếp smtp.host, smtp.port

// ✅ ĐÚNG - Convert sang mail.smtp.*
props.setProperty("mail.smtp.host", tempProps.getProperty("smtp.host"));
props.setProperty("mail.smtp.port", tempProps.getProperty("smtp.port", "587"));
props.setProperty("mail.smtp.auth", "true");
props.setProperty("mail.smtp.starttls.enable", "true");
props.setProperty("mail.smtp.starttls.required", "true");
```

### 2. Fix Authentication:
```java
// ❌ SAI - Username/Password từ props không load được
final String username = props.getProperty("smtp.user");
final String password = props.getProperty("smtp.pass");

// ✅ ĐÚNG - Load và lưu riêng
smtpUser = tempProps.getProperty("smtp.user");
smtpPass = tempProps.getProperty("smtp.pass");
```

### 3. Enable SMTP Debug:
```java
session.setDebug(true); // Sẽ show chi tiết SMTP conversation
```

## 🧪 Test sau khi fix:

Deploy và xem logs sẽ thấy:
```
=== Email Configuration ===
SMTP Host: smtp.us-west-1.mailertogo.net
SMTP Port: 587
SMTP Username: 72b0200...
SMTP Password: ***8328

DEBUG SMTP: trying to connect to host "smtp.us-west-1.mailertogo.net", port 587
DEBUG SMTP: connected to host "smtp.us-west-1.mailertogo.net"
220 smtp.us-west-1.mailertogo.net ESMTP ready
DEBUG SMTP: STARTTLS...
220 Ready to start TLS
DEBUG SMTP: AUTH LOGIN
334 VXNlcm5hbWU6
DEBUG SMTP: AUTH LOGIN command trace suppressed
235 2.7.0 Authentication successful
DEBUG SMTP: use8bit false
MAIL FROM:<noreply@nkbookstore.com>
250 2.1.0 Ok
RCPT TO:<your-email@gmail.com>
250 2.1.5 Ok
DATA
354 End data with <CR><LF>.<CR><LF>
250 2.0.0 Ok: queued as XXXX
```

Nếu thấy `235 2.7.0 Authentication successful` → Email sẽ được gửi thật!

## 📊 Monitoring:
Sau khi deploy, trong vòng 1-2 phút:
- ✅ MailerToGo dashboard: `1 emails sent`
- ✅ Gmail inbox: Nhận được email test

## 🚀 Commands:
```bash
git add .
git commit -m "Fix email authentication: correct JavaMail properties format and SMTP credentials loading"
git push heroku homepage:main
heroku logs --tail -a jva-bookstore
```
