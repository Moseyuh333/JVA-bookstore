# Hướng dẫn Import 500+ Sách từ CSV

## 📋 Tổng quan
File `books_full_500.csv` chứa **5,468 dòng** (khoảng 500+ cuốn sách) với đầy đủ thông tin từ Tiki.

## 🚀 Cách Import

### Phương pháp 1: Sử dụng Web Interface (Khuyến nghị)

1. **Start server local:**
   ```bash
   mvn clean package
   java -jar target/dependency/webapp-runner.jar --port 8080 target/*.war
   ```

2. **Truy cập trang import:**
   ```
   http://localhost:8080/admin/import-books
   ```

3. **Click nút "Start Import"** và chờ kết quả

4. **Kết quả hiển thị:**
   - ✓ Số sách import thành công
   - ✗ Số dòng lỗi
   - 📊 Tổng số dòng xử lý

### Phương pháp 2: Deploy lên Heroku rồi import

1. **Deploy code:**
   ```bash
   git add .
   git commit -m "Add CSV import feature + navigation fix"
   git push heroku homepage:main
   ```

2. **Copy file CSV lên server:**
   ```bash
   # Cách 1: Commit CSV vào repo (nếu file < 100MB)
   git add books_full_500.csv
   git commit -m "Add books data"
   git push heroku homepage:main
   
   # Cách 2: Upload thủ công qua SFTP/SCP
   ```

3. **Truy cập:**
   ```
   https://your-app.herokuapp.com/admin/import-books
   ```

## 📊 Dữ liệu CSV

### Các cột trong file:
```csv
id,title,author,price,original_price,discount,rating_avg,review_count,stock,
publisher,category,cover_image,shop_name,url,highlights,specifications,description,reviews
```

### Mapping vào database:
- `title` → `books.title`
- `author` → `books.author`
- `price` → `books.price` (BigDecimal)
- `rating_avg` → `books.average_rating`
- `review_count` → `books.rating_count`
- `stock` → `books.stock_quantity` (available=100, out_of_stock=0)
- `category` → `books.category`
- `cover_image` → `books.image_url`
- `description` → `books.description`

### Các field bỏ qua:
- `original_price`, `discount` - Model không có
- `publisher` - Model không có field này
- `shop_name`, `url`, `highlights`, `specifications`, `reviews` - Không cần thiết

## 🔧 Xử lý lỗi

### Servlet tự động handle:
- **CSV parsing**: Hỗ trợ dấu phẩy trong quotes (`"title, with comma"`)
- **Null values**: Set giá trị mặc định (0, null)
- **Missing fields**: Bỏ qua dòng và log warning
- **Duplicate check**: Không check, cho phép duplicate title

### Log errors:
- Hiển thị 10 lỗi đầu tiên trên màn hình
- Tổng số lỗi được count và report

## ✅ Kết quả mong đợi

Với file 5,468 dòng:
- **Success rate**: ~90-95% (450-475 sách)
- **Common errors**: 
  - Thiếu title (required field)
  - Price parsing error
  - Database constraint violation

## 🔍 Kiểm tra sau khi import

```sql
-- Đếm số sách
SELECT COUNT(*) FROM books;

-- Xem 10 sách mới nhất
SELECT id, title, author, price, category 
FROM books 
ORDER BY created_at DESC 
LIMIT 10;

-- Kiểm tra category
SELECT category, COUNT(*) as count 
FROM books 
GROUP BY category 
ORDER BY count DESC;
```

## 🎯 Next Steps

Sau khi import xong:
1. ✅ Test homepage → Xem sách hiển thị chưa
2. ✅ Test search → Tìm kiếm sách
3. ✅ Test category filter → Lọc theo danh mục
4. Deploy production và announce!

---
**Created**: 16/10/2025  
**Servlet**: `ImportBooksServlet.java`  
**CSV**: `books_full_500.csv` (5,468 lines)
