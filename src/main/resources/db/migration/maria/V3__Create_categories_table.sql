-- Create categories table (상품 카테고리)
CREATE TABLE IF NOT EXISTS categories (
    category_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    parent_category_seq BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    INDEX idx_categories_name (name),
    INDEX idx_categories_parent_seq (parent_category_seq),
    INDEX idx_categories_active (is_active),

    CONSTRAINT fk_categories_parent_seq
        FOREIGN KEY (parent_category_seq)
        REFERENCES categories(category_seq)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;