-- Create shops table
CREATE TABLE IF NOT EXISTS shops (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description TEXT,
    logo_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'active',
    commission_rate DECIMAL(5,2) DEFAULT 100.00,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    avatar_url VARCHAR(500),
    cover_url VARCHAR(500),
    featured_image_url VARCHAR(500),
    logo_text VARCHAR(255),
    slogan VARCHAR(255),
    banner_color VARCHAR(7) DEFAULT '#FF6B35',
    theme_color VARCHAR(7) DEFAULT '#FF8C42',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_shops_owner_id ON shops(owner_id);
CREATE INDEX IF NOT EXISTS idx_shops_status ON shops(status);
CREATE INDEX IF NOT EXISTS idx_shops_created_at ON shops(created_at);

-- Insert sample shop data
INSERT INTO shops (owner_id, name, description, logo_url, status, commission_rate, phone, email, address)
VALUES
(2, 'Book Haven', 'Cửa hàng chuyên sách văn học và truyện ngắn', 'https://i.imgur.com/abc123.png', 'active', 10.0, '0901 234 567', 'bookhaven@example.com', '123 Đường ABC, Quận 1, TP.HCM'),
(3, 'Science World', 'Cửa hàng sách khoa học và công nghệ', 'https://i.imgur.com/xyz789.png', 'active', 8.5, '0912 345 678', 'scienceworld@example.com', '456 Đường XYZ, Quận 2, TP.HCM')
ON CONFLICT DO NOTHING;
