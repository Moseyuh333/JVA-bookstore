-- Sample Data for JVA Bookstore E-Commerce
-- Run this AFTER the main schema.sql to populate test data

-- Insert Test Books
INSERT INTO books (title, author, isbn, price, description, category, stock_quantity, image_url, average_rating, rating_count, views_count, sales_count, created_at, updated_at) VALUES
('Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '9780132350884', 450000, 'A Handbook of Agile Software Craftsmanship - Practical guide to writing clean, maintainable code.', 'Programming', 50, 'https://via.placeholder.com/300x450?text=Clean+Code', 4.8, 125, 250, 45, NOW(), NOW()),
('The Pragmatic Programmer', 'David Thomas, Andrew Hunt', '9780201616224', 380000, 'Your Journey to Mastery - Essential knowledge for software developers covering best practices and techniques.', 'Programming', 40, 'https://via.placeholder.com/300x450?text=Pragmatic+Programmer', 4.7, 98, 180, 32, NOW(), NOW()),
('Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', '9780201633610', 520000, 'The definitive guide to design patterns in software design and architecture.', 'Programming', 30, 'https://via.placeholder.com/300x450?text=Design+Patterns', 4.6, 87, 220, 28, NOW(), NOW()),
('Refactoring: Improving the Design of Existing Code', 'Martin Fowler', '9780201485677', 420000, 'Structured Techniques for Improving Existing Code - Master the art of code improvement.', 'Programming', 35, 'https://via.placeholder.com/300x450?text=Refactoring', 4.5, 76, 190, 25, NOW(), NOW()),
('The C Programming Language', 'Brian W. Kernighan, Dennis M. Ritchie', '9780131103627', 350000, 'The definitive guide to the C programming language. Essential reading for every programmer.', 'Programming', 45, 'https://via.placeholder.com/300x450?text=C+Programming', 4.9, 142, 310, 52, NOW(), NOW()),
('Java: The Complete Reference', 'Herbert Schildt', '9781260440249', 680000, 'Comprehensive guide to Java programming - From basics to advanced topics. Perfect for Java developers.', 'Programming', 55, 'https://via.placeholder.com/300x450?text=Java+Reference', 4.4, 95, 280, 38, NOW(), NOW()),
('Spring in Action', 'Craig Walls', '9781617294945', 520000, 'Spring Framework in-depth - Learn enterprise Java development with Spring.', 'Programming', 32, 'https://via.placeholder.com/300x450?text=Spring+Action', 4.6, 68, 160, 22, NOW(), NOW()),
('The Art of Computer Programming', 'Donald E. Knuth', '9780201631883', 890000, 'The bible of computer science - Mathematical foundations and algorithms.', 'Computer Science', 20, 'https://via.placeholder.com/300x450?text=Art+Programming', 4.9, 54, 180, 15, NOW(), NOW()),
('Introduction to Algorithms', 'Thomas H. Cormen et al.', '9780262033848', 750000, 'Comprehensive study of algorithms and their design. Essential for computer science students.', 'Computer Science', 28, 'https://via.placeholder.com/300x450?text=Intro+Algorithms', 4.8, 112, 250, 35, NOW(), NOW()),
('Discrete Mathematics and Its Applications', 'Kenneth H. Rosen', '9780073383095', 650000, 'Foundation of discrete mathematics for computer science and engineering.', 'Computer Science', 25, 'https://via.placeholder.com/300x450?text=Discrete+Math', 4.5, 61, 140, 18, NOW(), NOW()),
('Artificial Intelligence: A Modern Approach', 'Stuart Russell, Peter Norvig', '9780136042594', 850000, 'Comprehensive introduction to artificial intelligence - State of the art AI techniques.', 'Artificial Intelligence', 15, 'https://via.placeholder.com/300x450?text=AI+Modern', 4.7, 73, 200, 20, NOW(), NOW()),
('Deep Learning', 'Ian Goodfellow et al.', '9780262035613', 920000, 'The deep learning bible - Theory and practice of neural networks.', 'Artificial Intelligence', 12, 'https://via.placeholder.com/300x450?text=Deep+Learning', 4.8, 89, 220, 24, NOW(), NOW()),
('Hands-On Machine Learning', 'Aurélien Géron', '9781491962282', 680000, 'Practical machine learning with scikit-learn and TensorFlow.', 'Artificial Intelligence', 18, 'https://via.placeholder.com/300x450?text=ML+Hands-On', 4.6, 76, 190, 19, NOW(), NOW()),
('Web Development with Django', 'Antonio Melé', '9781788472455', 580000, 'Build powerful web applications with Django framework.', 'Web Development', 40, 'https://via.placeholder.com/300x450?text=Django+Web', 4.5, 68, 150, 21, NOW(), NOW()),
('Vue.js: Up and Running', 'Callum Macrae', '9781491997529', 450000, 'Learn to build interactive web applications with Vue.js.', 'Web Development', 50, 'https://via.placeholder.com/300x450?text=Vue+JS', 4.4, 54, 130, 16, NOW(), NOW()),
('React in Action', 'Mark Tielens Thomas', '9781617294259', 520000, 'Comprehensive guide to building user interfaces with React.', 'Web Development', 45, 'https://via.placeholder.com/300x450?text=React+Action', 4.7, 82, 210, 26, NOW(), NOW()),
('The Definitive Guide to MongoDB', 'Shannon Bradshaw et al.', '9781491954454', 580000, 'Master NoSQL database design with MongoDB.', 'Databases', 30, 'https://via.placeholder.com/300x450?text=MongoDB', 4.5, 61, 140, 17, NOW(), NOW()),
('PostgreSQL: Up and Running', 'Regina O. Obe, Leo S. Hsu', '9781491963548', 520000, 'Practical guide to PostgreSQL database design and optimization.', 'Databases', 35, 'https://via.placeholder.com/300x450?text=PostgreSQL', 4.6, 71, 160, 20, NOW(), NOW()),
('SQL Performance Explained', 'Markus Winand', '9783950307825', 450000, 'Deep dive into SQL optimization and performance tuning.', 'Databases', 28, 'https://via.placeholder.com/300x450?text=SQL+Performance', 4.8, 58, 120, 13, NOW(), NOW()),
('Mastering Regular Expressions', 'Jeffrey E. F. Friedl', '9780596528126', 380000, 'Comprehensive guide to regular expressions in programming.', 'Programming', 40, 'https://via.placeholder.com/300x450?text=RegEx', 4.6, 73, 170, 24, NOW(), NOW());

-- Insert Test User (Test account)
INSERT INTO users (username, email, password, full_name, phone, birth_date, address, verified, created_at) VALUES
('testuser', 'testuser@example.com', '$2a$10$nOQZf8.vDZg3CwW8h5J3N.U8q8ZK.YLyLk8K1M7vQJNjRxnSKKg.e', 'Test User', '0123456789', '1990-01-01', '123 Test Street, District 1, HCMC', true, NOW()),
('customer1', 'customer1@example.com', '$2a$10$nOQZf8.vDZg3CwW8h5J3N.U8q8ZK.YLyLk8K1M7vQJNjRxnSKKg.e', 'John Doe', '0987654321', '1992-03-15', '456 Main St, District 2, HCMC', true, NOW());

-- Insert Test Delivery Addresses
INSERT INTO delivery_addresses (user_id, recipient_name, phone_number, province, district, ward, address_detail, is_default, created_at, updated_at) VALUES
(1, 'Test User', '0123456789', 'Ho Chi Minh', 'District 1', 'Ben Nghe', '123 Nguyen Hue Blvd', true, NOW(), NOW()),
(1, 'Test User', '0123456789', 'Ha Noi', 'District 3', 'Ba Dinh', '456 Le Duan St', false, NOW(), NOW()),
(2, 'John Doe', '0987654321', 'Ho Chi Minh', 'District 7', 'Tan Phu', '789 Ly Thuong Kiet St', true, NOW(), NOW());

-- Insert Test Coupons
INSERT INTO coupons (code, description, discount_type, discount_value, min_purchase_amount, max_usage_count, valid_from, valid_until, is_active, created_at, updated_at) VALUES
('WELCOME10', 'Welcome discount - 10% off', 'percent', 10, 0, 1000, NOW() - INTERVAL '7 days', NOW() + INTERVAL '90 days', true, NOW(), NOW()),
('SUMMER50K', 'Summer sale - Flat 50,000 VND off', 'fixed', 50000, 200000, 500, NOW() - INTERVAL '7 days', NOW() + INTERVAL '30 days', true, NOW(), NOW()),
('LOYAL20', 'Loyalty discount - 20% off', 'percent', 20, 500000, 100, NOW() - INTERVAL '7 days', NOW() + INTERVAL '60 days', true, NOW(), NOW()),
('FREESHIP', 'Free shipping - 30,000 VND off', 'fixed', 30000, 100000, 1000, NOW() - INTERVAL '7 days', NOW() + INTERVAL '45 days', true, NOW(), NOW());

-- Insert Test Ratings
INSERT INTO ratings (user_id, book_id, rating, review, is_verified_purchase, created_at, updated_at) VALUES
(1, 1, 5, 'Excellent book! Very useful for improving code quality.', true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(1, 2, 4, 'Good practical guide for programmers.', true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(2, 1, 5, 'Best book about writing clean code. Highly recommended!', true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(2, 3, 4, 'Comprehensive resource on design patterns.', true, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

-- Insert Test Comments
INSERT INTO comments (book_id, user_id, comment_text, image_url, is_verified_purchase, created_at, updated_at) VALUES
(1, 1, 'This book transformed my approach to software development. Every chapter is packed with valuable insights that I could immediately apply to my projects. The examples are clear and practical.', NULL, true, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
(1, 2, 'Definitely a must-read for senior developers. The section on refactoring techniques was particularly enlightening and saved me countless hours debugging complex code.', NULL, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(2, 1, 'The pragmatic approach in this book is exactly what I needed. The tips and tricks have made me a better programmer. Great investment!', NULL, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(5, 2, 'Classic reference that belongs on every programmer''s shelf. Written by the creators of C, so you know it''s authoritative and comprehensive.', NULL, true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Insert Test Shopping Cart for Test User
INSERT INTO shopping_cart (user_id, created_at, updated_at) VALUES
(1, NOW(), NOW());

-- Insert Test Cart Items
INSERT INTO cart_items (cart_id, book_id, quantity, added_at) VALUES
(1, 1, 2, NOW() - INTERVAL '1 day'),
(1, 5, 1, NOW() - INTERVAL '12 hours');

-- Insert Test Wishlist Items
INSERT INTO wishlist (user_id, book_id, added_at) VALUES
(1, 7, NOW() - INTERVAL '5 days'),
(1, 11, NOW() - INTERVAL '3 days'),
(1, 15, NOW() - INTERVAL '1 day'),
(2, 1, NOW() - INTERVAL '4 days'),
(2, 12, NOW() - INTERVAL '2 days');

-- Insert Test Product Views
INSERT INTO product_views (user_id, book_id, viewed_at) VALUES
(1, 1, NOW() - INTERVAL '1 hour'),
(1, 5, NOW() - INTERVAL '50 minutes'),
(1, 11, NOW() - INTERVAL '30 minutes'),
(1, 15, NOW() - INTERVAL '15 minutes'),
(2, 1, NOW() - INTERVAL '45 minutes'),
(2, 7, NOW() - INTERVAL '20 minutes');

-- Insert Test Orders
INSERT INTO orders (user_id, order_date, total_amount, status, delivery_address_id, shipping_address, payment_method, coupon_id, discount_amount, final_total, created_at, updated_at) VALUES
(1, NOW() - INTERVAL '5 days', 900000, 'delivered', 1, '123 Nguyen Hue Blvd, Ben Nghe, District 1, Ho Chi Minh', 'COD', 1, 90000, 810000, NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'),
(2, NOW() - INTERVAL '2 days', 1200000, 'shipping', 3, '789 Ly Thuong Kiet St, Tan Phu, District 7, Ho Chi Minh', 'VNPAY', 2, 50000, 1150000, NOW() - INTERVAL '2 days', NOW() - INTERVAL '6 hours');

-- Insert Test Order Items
INSERT INTO order_items (order_id, book_id, quantity, unit_price, total_price, created_at) VALUES
(1, 1, 1, 450000, 450000, NOW() - INTERVAL '5 days'),
(1, 5, 1, 350000, 350000, NOW() - INTERVAL '5 days'),
(2, 2, 2, 380000, 760000, NOW() - INTERVAL '2 days'),
(2, 6, 1, 680000, 680000, NOW() - INTERVAL '2 days');

-- Insert Test Payment Transactions
INSERT INTO payment_transactions (order_id, payment_method, amount, transaction_code, status, created_at, updated_at) VALUES
(1, 'COD', 810000, 'COD-001-20240115', 'completed', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'),
(2, 'VNPAY', 1150000, 'VNPAY-2024011500123', 'completed', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day');

-- Verify data was inserted
SELECT COUNT(*) as total_books FROM books;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_orders FROM orders;
SELECT COUNT(*) as total_ratings FROM ratings;
SELECT COUNT(*) as total_comments FROM comments;
SELECT COUNT(*) as total_coupons FROM coupons;

-- Sample queries to test data
-- Get homepage data
SELECT title, price, average_rating, rating_count FROM books ORDER BY created_at DESC LIMIT 20;
SELECT title, price, average_rating, rating_count FROM books WHERE rating_count > 0 ORDER BY average_rating DESC LIMIT 20;
SELECT title, price FROM books ORDER BY sales_count DESC LIMIT 20;

-- Get user orders with details
SELECT o.id, o.order_date, o.total_amount, o.status, o.final_total, da.recipient_name, da.phone_number, da.address_detail
FROM orders o
LEFT JOIN delivery_addresses da ON o.delivery_address_id = da.id
WHERE o.user_id = 1
ORDER BY o.order_date DESC;

-- Get ratings for a book
SELECT r.rating, r.review, u.username, r.created_at
FROM ratings r
JOIN users u ON r.user_id = u.id
WHERE r.book_id = 1
ORDER BY r.created_at DESC;

-- Get comments for a book
SELECT c.comment_text, u.username, c.created_at
FROM comments c
JOIN users u ON c.user_id = u.id
WHERE c.book_id = 1
ORDER BY c.created_at DESC;

-- Get user wishlist
SELECT b.id, b.title, b.price, b.average_rating, b.rating_count
FROM wishlist w
JOIN books b ON w.book_id = b.id
WHERE w.user_id = 1
ORDER BY w.added_at DESC;

-- Get user cart
SELECT ci.id, b.id, b.title, b.price, ci.quantity, (b.price * ci.quantity) as line_total
FROM shopping_cart sc
JOIN cart_items ci ON sc.id = ci.cart_id
JOIN books b ON ci.book_id = b.id
WHERE sc.user_id = 1;
