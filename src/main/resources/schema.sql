-- Create users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    birth_date DATE,
    address TEXT,
    verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    reset_token VARCHAR(255),
    reset_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users(reset_token);

-- Create books table
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    isbn VARCHAR(20),
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    stock_quantity INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_number VARCHAR(40),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal_amount DECIMAL(10, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    shipping_fee DECIMAL(10, 2) DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    status VARCHAR(50) DEFAULT 'pending',
    payment_status VARCHAR(30) DEFAULT 'pending',
    payment_method VARCHAR(50),
    payment_reference VARCHAR(120),
    shipping_full_name VARCHAR(120),
    shipping_phone VARCHAR(30),
    shipping_email VARCHAR(120),
    shipping_address TEXT,
    shipping_city VARCHAR(120),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(120),
    shipping_notes TEXT,
    customer_message TEXT,
    cart_snapshot TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Additional indexes for performance
CREATE INDEX IF NOT EXISTS idx_books_category ON books(category);
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date);
CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_order_number ON orders(order_number);
CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_payment_reference ON orders(payment_reference) WHERE payment_reference IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_book_id ON order_items(book_id);

-- Track favorite (liked) books per user
CREATE TABLE IF NOT EXISTS book_favorites (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_book_favorites UNIQUE (user_id, book_id)
);

CREATE INDEX IF NOT EXISTS idx_book_favorites_book_id ON book_favorites(book_id);

-- Store product reviews and ratings
CREATE TABLE IF NOT EXISTS book_reviews (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(255),
    content TEXT,
    media_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'published'
);

CREATE INDEX IF NOT EXISTS idx_book_reviews_book_id ON book_reviews(book_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_user_id ON book_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_status ON book_reviews(status);

-- OTP verification storage
CREATE TABLE IF NOT EXISTS otp_verifications (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    attempts INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_otp_email ON otp_verifications(email);
CREATE INDEX IF NOT EXISTS idx_otp_code ON otp_verifications(otp_code);

-- Synthetic engagement metrics
CREATE TABLE IF NOT EXISTS book_metrics (
    book_id INTEGER PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
    total_sold INTEGER DEFAULT 0,
    avg_rating DOUBLE PRECISION DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_book_metrics_popularity ON book_metrics(total_sold DESC, favorite_count DESC);

-- Shopping cart tables
CREATE TABLE IF NOT EXISTS carts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(64),
    status VARCHAR(20) DEFAULT 'active',
    currency VARCHAR(10) DEFAULT 'VND',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_carts_user ON carts(user_id);
CREATE INDEX IF NOT EXISTS idx_carts_session ON carts(session_id);
CREATE INDEX IF NOT EXISTS idx_carts_status ON carts(status);

CREATE TABLE IF NOT EXISTS cart_items (
    id SERIAL PRIMARY KEY,
    cart_id INTEGER NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_items_cart_book ON cart_items(cart_id, book_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);

CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    method VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'pending',
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'VND',
    transaction_code VARCHAR(120),
    raw_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_transaction_code ON payments(transaction_code) WHERE transaction_code IS NOT NULL;

CREATE TABLE IF NOT EXISTS order_status_history (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order ON order_status_history(order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_status ON order_status_history(status);
