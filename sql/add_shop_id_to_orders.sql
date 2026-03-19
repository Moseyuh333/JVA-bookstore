-- Migration: Thêm shop_id vào bảng orders
-- Chạy script này để thêm cột shop_id vào bảng orders

-- 1. Thêm cột shop_id vào bảng orders
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shop_id INTEGER;

-- 2. Tạo index cho shop_id để tăng hiệu suất truy vấn
CREATE INDEX IF NOT EXISTS idx_orders_shop_id ON orders(shop_id);

-- 3. Cập nhật shop_id cho các đơn hàng hiện có
-- Lấy shop_id từ order_items (lấy shop_id của item đầu tiên trong mỗi đơn)
UPDATE orders o
SET shop_id = (
    SELECT oi.shop_id 
    FROM order_items oi 
    WHERE oi.order_id = o.id 
    ORDER BY oi.id
    LIMIT 1
)
WHERE o.shop_id IS NULL;

-- 4. Kiểm tra kết quả
SELECT COUNT(*) as total_orders, 
       COUNT(shop_id) as orders_with_shop_id,
       COUNT(*) - COUNT(shop_id) as orders_without_shop_id
FROM orders;
