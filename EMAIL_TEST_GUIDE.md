# 📧 Hướng dẫn Test Email với MailerToGo

## ✅ Đã cấu hình:
- SMTP Host: smtp.us-west-1.mailertogo.net
- SMTP Port: 587
- SMTP User: 72b02002a3a21dbec45a9b32e49ba19e
- Plan: Micro (25,000 emails/tháng)

## 🚀 Các bước test:

### 1. Deploy code mới:
```bash
cd "c:\Users\MINH KHOI\Downloads\LTPython-main\LTPython-main\JVA-bookstore"
git add .
git commit -m "Add email test functionality with detailed logging"
git push heroku homepage:main
```

### 2. Test gửi email:
Truy cập: **https://jva-bookstore-17d2d34519f8.herokuapp.com/test-email.html**

- Nhập email của bạn
- Click "Gửi Email Test"
- Kiểm tra email inbox (và spam folder)

### 3. Xem logs trên Heroku:
```bash
heroku logs --tail -a jva-bookstore
```

Logs sẽ hiển thị:
```
=== Starting Email Send ===
SMTP Host: smtp.us-west-1.mailertogo.net
SMTP Port: 587
From: noreply@nkbookstore.com
To: your-email@example.com
...
✅ Email sent successfully
```

### 4. Kiểm tra MailerToGo Dashboard:
- Truy cập: https://mailertogo.com/dashboard
- Refresh trang
- Số "Emails sent" sẽ tăng từ 0 → 1 nếu email gửi thành công

## 🐛 Nếu có lỗi:

### Lỗi "Authentication failed":
- Kiểm tra SMTP credentials trên Heroku:
  ```bash
  heroku config -a jva-bookstore | findstr SMTP
  ```

### Lỗi "Connection timeout":
- Server không thể kết nối đến SMTP
- Kiểm tra firewall/network

### Email không nhận được:
- Kiểm tra spam folder
- Kiểm tra email có đúng không
- Đợi vài phút (có thể delay)

## 📊 Theo dõi:
- **MailerToGo Dashboard**: Số email đã gửi
- **Heroku Logs**: Chi tiết quá trình gửi
- **Email Inbox**: Email nhận được

## ⚠️ Lưu ý:
- Mỗi lần test sẽ tính vào quota 25,000 emails/tháng
- Email gửi từ: noreply@nkbookstore.com
- Nếu muốn dùng domain riêng, cần add domain vào MailerToGo
