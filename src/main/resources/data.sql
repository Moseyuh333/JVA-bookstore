
INSERT INTO categories(name) VALUES ('Khoa học'), ('Kinh doanh'), ('Văn học');

INSERT INTO books(title, author, category_id, description, price, stock, image) VALUES
('Dế Mèn Phiêu Lưu Ký', 'Tô Hoài', 3, 'Tác phẩm thiếu nhi kinh điển.', 45000, 12, 'https://picsum.photos/seed/demen/300/450'),
('Lược Sử Thời Gian', 'Stephen Hawking', 1, 'Khám phá vũ trụ cho mọi người.', 120000, 5, 'https://picsum.photos/seed/hawking/300/450'),
('Đắc Nhân Tâm', 'Dale Carnegie', 2, 'Kỹ năng giao tiếp và ứng xử.', 90000, 9, 'https://picsum.photos/seed/dacnhantam/300/450'),
('Vũ Trụ Trong Vỏ Hạt Dẻ', 'Stephen Hawking', 1, 'Tiếp tục câu chuyện vũ trụ.', 130000, 4, 'https://picsum.photos/seed/nut/300/450');


INSERT INTO reviews(rating, comment, created_at, book_id) VALUES
(5, 'Rất hay!', CURRENT_TIMESTAMP, 2),
(4, 'Dễ hiểu, thú vị.', CURRENT_TIMESTAMP, 2),
(3, 'Ổn.', CURRENT_TIMESTAMP, 3);
