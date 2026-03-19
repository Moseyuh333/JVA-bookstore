-- ======================================
-- 🌟 Bookish Bliss Haven - Admin Seed Data
-- ======================================

-- 2️⃣ CATEGORIES
INSERT INTO categories (name, slug)
VALUES
('Văn học', 'van-hoc'),
('Khoa học', 'khoa-hoc'),
('Kinh tế', 'kinh-te'),
('Thiếu nhi', 'thieu-nhi')
ON CONFLICT DO NOTHING;

-- 3️⃣ SHOPS
INSERT INTO shops (owner_id, name, description, logo_url, status, commission_rate)
VALUES
(2, 'Book Haven', 'Cửa hàng chuyên sách văn học và truyện ngắn', 'https://i.imgur.com/abc123.png', 'active', 10.0),
(2, 'Science World', 'Cửa hàng sách khoa học và công nghệ', 'https://i.imgur.com/xyz789.png', 'active', 8.5)
ON CONFLICT DO NOTHING;

-- 4️⃣ STORE DISCOUNTS
INSERT INTO store_discounts (shop_id, discount_rate, start_date, end_date, active, description)
VALUES
(1, 15.00, NOW(), NOW() + INTERVAL '30 days', TRUE, 'Ưu đãi giảm 15% cho Book Haven'),
(2, 10.00, NOW(), NOW() + INTERVAL '60 days', TRUE, 'Ưu đãi giảm 10% cho Science World');

-- 5️⃣ COUPONS
INSERT INTO coupons (code, description, type, discount_value, max_discount, min_order, usage_limit, used_count, start_at, end_at, active, apply_to)
VALUES
('BOOKISH10', 'Giảm 10% cho đơn hàng từ 100k', 'percentage', 10, 50000, 100000, 100, 0, NOW(), NOW() + INTERVAL '30 days', TRUE, 'product'),
('SHIPFREE', 'Miễn phí vận chuyển cho đơn từ 200k', 'shipping', 100, 30000, 200000, 200, 0, NOW(), NOW() + INTERVAL '45 days', TRUE, 'shipping');

-- 6️⃣ SHIPPERS
INSERT INTO shippers (name, phone, email, base_fee, service_area, estimated_time, status, created_at)
VALUES
('GiaoNhan247', '0901 234 567', 'support@giaonhan247.vn', 25000, 'TP.HCM, Bình Dương, Đồng Nai', '1-2 ngày', 'active', NOW()),
('ShipFast Express', '0912 345 678', 'contact@shipfast.vn', 30000, 'Toàn quốc', '2-4 ngày', 'active', NOW()),
('BookShip Co.', '0923 456 789', 'bookship@gmail.com', 20000, 'TP.HCM nội thành', 'Trong ngày', 'active', NOW());

-- 7️⃣ BOOKS (mẫu hiển thị)
INSERT INTO books (title, author, price, stock, publisher, category, shop_name, highlights, specifications, description, cover_image, book_url)
VALUES
('Chí Phèo', 'Nam Cao', 75000, 100, 'NXB Văn Học', 'Văn học', 'Book Haven', 'Tác phẩm kinh điển Việt Nam', 'Bìa mềm, 250 trang', 'Phản ánh hiện thực xã hội Việt Nam xưa.', 'https://i.imgur.com/book1.png', '#'),
('Vũ trụ trong vỏ hạt dẻ', 'Stephen Hawking', 155000, 80, 'NXB Tri Thức', 'Khoa học', 'Science World', 'Giải thích vật lý vũ trụ dễ hiểu', 'Bìa cứng, 350 trang', 'Khám phá vật lý lượng tử và thuyết tương đối', 'https://i.imgur.com/book2.png', '#');

-- ===========================
-- ✅ VERIFY COUNT
-- ===========================
SELECT '✅ USERS', COUNT(*) FROM users;
SELECT '✅ CATEGORIES', COUNT(*) FROM categories;
SELECT '✅ SHOPS', COUNT(*) FROM shops;
SELECT '✅ STORE DISCOUNTS', COUNT(*) FROM store_discounts;
SELECT '✅ COUPONS', COUNT(*) FROM coupons;
SELECT '✅ SHIPPERS', COUNT(*) FROM shippers;
SELECT '✅ BOOKS', COUNT(*) FROM books;
