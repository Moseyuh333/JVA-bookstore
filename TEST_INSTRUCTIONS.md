# 🧪 Test Instructions - Homepage v258

## 🚀 Deployed Version
- **Version**: v258
- **URL**: https://jva-bookstore-17d2d34519f8.herokuapp.com/
- **Deploy Time**: 16/10/2025 23:15
- **Changes**: Added debug console.log to track API calls

---

## 🔍 How to Test

### 1. Clear Browser Cache (QUAN TRỌNG!)
**Để thấy thay đổi mới, BẮT BUỘC phải clear cache:**

**Chrome/Edge**:
```
1. Mở trang: https://jva-bookstore-17d2d34519f8.herokuapp.com/
2. Nhấn F12 để mở DevTools
3. Click chuột phải vào nút Refresh
4. Chọn "Empty Cache and Hard Reload" (hoặc "Xóa bộ nhớ đệm và tải lại trang")
```

**Firefox**:
```
1. Nhấn Ctrl + Shift + Delete
2. Chọn "Cache"
3. Chọn "Last hour"
4. Click "Clear Now"
5. Reload trang (F5)
```

**Hoặc dùng Incognito/Private mode**:
```
- Chrome: Ctrl + Shift + N
- Firefox: Ctrl + Shift + P
- Edge: Ctrl + Shift + N
```

---

### 2. Mở DevTools Console
Nhấn **F12** hoặc **Ctrl+Shift+I** để mở Developer Tools, chọn tab **Console**.

---

### 3. Kiểm tra Debug Logs

Khi trang load, bạn sẽ thấy console logs như sau:

**✅ ĐÚNG** (Version mới):
```javascript
[NK Bookstore] API Base: /api
[NK Bookstore] Loading: books/newest
[NK Bookstore] Loading: books/best-selling
[NK Bookstore] Loading: books/top-rated  
[NK Bookstore] Loading: books/favorites
```

**❌ SAI** (Version cũ - nếu vẫn thấy):
```
Không có log gì
hoặc
Failed to load books/newest: 404
```

---

### 4. Kiểm tra Network Tab

Trong DevTools, chuyển sang tab **Network**:

**✅ ĐÚNG - Các request này phải thành công (200 OK)**:
```
GET /api/books/newest?limit=12&offset=0     → 200 OK
GET /api/books/best-selling?limit=12&offset=0 → 200 OK
GET /api/books/top-rated?limit=12&offset=0   → 200 OK
GET /api/books/favorites?limit=12&offset=0   → 200 OK
```

**❌ SAI - Nếu thấy các request này**:
```
GET /?limit=12&offset=0  → 404 NOT FOUND
GET /books/?limit=12     → 404 NOT FOUND
```

---

### 5. Kiểm tra UI

**Navbar** (chưa đăng nhập):
- ✅ Phải thấy: "Đăng nhập" + "Đăng ký"
- ❌ Không thấy chỉ 1 nút "Tài khoản"

**Sections sách**:
- ✅ "Sách Mới Nhất" - hiển thị sách (không phải "Không thể tải dữ liệu sách")
- ✅ "Bán Chạy Nhất" - hiển thị sách
- ✅ "Đánh Giá Cao Nhất" - hiển thị sách
- ✅ "Yêu Thích Nhất" - hiển thị sách

**Footer**:
- ✅ Nằm ở cuối trang (không nằm giữa)
- ✅ Màu nâu gradient đẹp

---

### 6. Test Login Flow

1. **Đăng ký user mới** (nếu chưa có):
   ```
   URL: https://jva-bookstore-17d2d34519f8.herokuapp.com/register.jsp
   Username: test123
   Password: test1234
   Email: test@example.com
   ```

2. **Đăng nhập**:
   ```
   URL: https://jva-bookstore-17d2d34519f8.herokuapp.com/login.jsp
   Username: shino113399 (hoặc user bạn tạo)
   Password: shino0908
   ```

3. **Kiểm tra sau login**:
   - ✅ Navbar hiện "Tài khoản" + "Đăng xuất"
   - ✅ Console log: `[NK Bookstore] User logged in: shino113399`
   - ✅ localStorage có `token` và `username`

4. **Click "Đăng xuất"**:
   - ✅ Navbar quay lại "Đăng nhập" + "Đăng ký"
   - ✅ localStorage bị clear

---

## 🐛 Nếu vẫn thấy lỗi

### Lỗi 1: "Không thể tải dữ liệu sách"
**Nguyên nhân**: Database chưa có sách
**Giải pháp**:
```bash
# Import 500 sách từ CSV
URL: https://jva-bookstore-17d2d34519f8.herokuapp.com/admin/import-books
File: books_full_500.csv
```

### Lỗi 2: Vẫn thấy giao diện cũ
**Nguyên nhân**: Browser cache
**Giải pháp**:
1. Hard refresh: Ctrl + F5
2. Clear cache theo hướng dẫn bước 1
3. Dùng Incognito mode
4. Restart browser

### Lỗi 3: Console không có log "[NK Bookstore]"
**Nguyên nhân**: Heroku chưa deploy version mới
**Kiểm tra**:
```bash
heroku releases --num 5
# Phải thấy v258 là version mới nhất
```

### Lỗi 4: API trả về 404
**Kiểm tra backend**:
```bash
# Test API trực tiếp
curl https://jva-bookstore-17d2d34519f8.herokuapp.com/api/books/newest?limit=3

# Phải trả về JSON array của books
[{"id":1,"title":"...","author":"...",...}]
```

---

## 📊 Expected Results

**Sau khi clear cache và reload**, trang homepage phải:

1. ✅ Hiển thị 4 sections sách (mỗi section 12 books)
2. ✅ Navbar dynamic (login/logout based on session)
3. ✅ Footer ở cuối trang
4. ✅ Console logs hiển thị API calls
5. ✅ Network tab không có 404 errors
6. ✅ Có thể login/logout successfully
7. ✅ Cart badge hiển thị số lượng
8. ✅ Buttons "Thêm vào giỏ" hoạt động (cần login)

---

## 🆘 Support

Nếu vẫn gặp vấn đề, gửi cho tôi:

1. **Screenshot Console tab** (F12 → Console)
2. **Screenshot Network tab** (F12 → Network, filter: Fetch/XHR)
3. **Browser name + version** (Chrome 120, Firefox 119, etc.)
4. **Describe the issue**: Giao diện như thế nào? Có lỗi gì trong console?

---

**Version**: v258  
**Last Updated**: 16/10/2025 23:15  
**Status**: ✅ Deployed and ready for testing
