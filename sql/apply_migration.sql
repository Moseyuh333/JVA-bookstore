-- Migration: Thêm shop_id và shop_name
-- Chạy file này trên database của bạn

-- Bước 1: Thêm cột vào bảng books
ALTER TABLE books ADD COLUMN IF NOT EXISTS shop_id INTEGER;
ALTER TABLE books ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255);

-- Bước 2: Thêm cột vào bảng order_items (FIX LỖI)
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS shop_id INTEGER;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255);

-- Bước 3: Tạo index
CREATE INDEX IF NOT EXISTS idx_books_shop_id ON books(shop_id);

-- Bước 4: Cập nhật shop mặc định cho sách hiện có
UPDATE books SET shop_id = 1, shop_name = 'BookStore Default' WHERE shop_id IS NULL;

-- Bước 5: (Tùy chọn) Cập nhật order_items cũ với shop info từ books
UPDATE order_items oi
SET shop_id = b.shop_id, shop_name = b.shop_name
FROM books b
WHERE oi.book_id = b.id AND oi.shop_id IS NULL;

-- Hoàn tất!
SELECT 'Migration completed successfully!' as status;
