-- Create stock_movements table (재고 이동 이력)
CREATE TABLE IF NOT EXISTS stock_movements (
    stock_movement_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_seq BIGINT NOT NULL,
    location VARCHAR(100) NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER') NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10,2),
    reference_type ENUM('PURCHASE', 'SALE', 'ADJUSTMENT', 'TRANSFER', 'RETURN') NOT NULL,
    reference_id BIGINT,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    INDEX idx_stock_movements_product_seq (product_seq),
    INDEX idx_stock_movements_location (location),
    INDEX idx_stock_movements_type (movement_type),
    INDEX idx_stock_movements_reference (reference_type, reference_id),
    INDEX idx_stock_movements_user_id (created_seq),
    INDEX idx_stock_movements_created_at (created_at),

    CONSTRAINT fk_stock_movements_product_seq
        FOREIGN KEY (product_seq)
        REFERENCES products(product_seq)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_stock_movements_user_seq
        FOREIGN KEY (created_seq)
        REFERENCES users(user_seq)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;