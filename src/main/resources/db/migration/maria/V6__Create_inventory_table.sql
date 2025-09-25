-- Create inventory table (재고)
CREATE TABLE IF NOT EXISTS inventory (
    inventory_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_seq BIGINT NOT NULL,
    location VARCHAR(100) DEFAULT 'MAIN_WAREHOUSE',
    current_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    available_stock INT GENERATED ALWAYS AS (current_stock - reserved_stock) STORED,
    min_stock_level INT DEFAULT 0,
    max_stock_level INT DEFAULT 0,
    reorder_point INT DEFAULT 0,
    last_stock_check TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    UNIQUE KEY uk_inventory_product_location (product_seq, location),
    INDEX idx_inventory_product_seq (product_seq),
    INDEX idx_inventory_location (location),
    INDEX idx_inventory_current_stock (current_stock),
    INDEX idx_inventory_available_stock (available_stock),

    CONSTRAINT fk_inventory_product_seq
        FOREIGN KEY (product_seq)
        REFERENCES products(product_seq)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;