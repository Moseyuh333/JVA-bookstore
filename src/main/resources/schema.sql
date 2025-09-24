
CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL
);

CREATE TABLE books (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255),
  category_id BIGINT,
  description CLOB,
  price DOUBLE,
  stock INT,
  image VARCHAR(500),
  CONSTRAINT fk_book_cat FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE reviews (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rating INT,
  comment CLOB,
  created_at TIMESTAMP,
  book_id BIGINT,
  CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES books(id)
);
