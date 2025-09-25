-- Create discounts table (할인)
CREATE TABLE IF NOT EXISTS discounts (
    discount_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(12,2) DEFAULT 0.00,
    max_discount_amount DECIMAL(10,2),
    usage_limit INT,
    used_count INT DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    applicable_to ENUM('ALL', 'CATEGORY', 'PRODUCT', 'CUSTOMER') DEFAULT 'ALL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    INDEX idx_discounts_name (name),
    INDEX idx_discounts_type (discount_type),
    INDEX idx_discounts_dates (start_date, end_date),
    INDEX idx_discounts_active (is_active),
    INDEX idx_discounts_applicable_to (applicable_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;