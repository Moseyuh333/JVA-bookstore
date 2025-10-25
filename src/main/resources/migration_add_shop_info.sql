-- Migration: Thêm shop_id và shop_name vào bảng books và order_items
-- Mô tả: Thêm thông tin shop cho mỗi cuốn sách và lưu snapshot shop info trong order_items

-- 1. Thêm cột shop_id và shop_name vào bảng books
ALTER TABLE books 
ADD COLUMN IF NOT EXISTS shop_id INTEGER;

ALTER TABLE books 
ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255);

-- Tạo index cho shop_id để tăng hiệu suất truy vấn
CREATE INDEX IF NOT EXISTS idx_books_shop_id ON books(shop_id);

-- 2. Thêm cột shop_id và shop_name vào bảng order_items (snapshot)
-- Điều này đảm bảo thông tin shop không thay đổi sau khi đặt hàng
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS shop_id INTEGER;

ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255);

-- 3. Cập nhật dữ liệu mẫu cho books (tùy chọn - có thể bỏ qua nếu muốn nhập dữ liệu thủ công)
-- Ví dụ: Gán shop mặc định cho các sách hiện tại
-- UPDATE books SET shop_id = 1, shop_name = 'BookStore Main' WHERE shop_id IS NULL;

-- 4. Cập nhật order_items hiện tại với thông tin shop từ books (nếu cần)
-- UPDATE order_items oi
-- SET shop_id = b.shop_id, shop_name = b.shop_name
-- FROM books b
-- WHERE oi.book_id = b.id AND oi.shop_id IS NULL;
