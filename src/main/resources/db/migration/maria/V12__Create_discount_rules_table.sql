-- Create discount_rules table (할인 적용 규칙)
CREATE TABLE IF NOT EXISTS discount_rules (
    discount_rule_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    discount_seq BIGINT NOT NULL,
    target_type ENUM('CATEGORY', 'PRODUCT', 'CUSTOMER') NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    UNIQUE KEY uk_discount_rules (discount_seq, target_type, target_id),
    INDEX idx_discount_rules_discount_seq (discount_seq),
    INDEX idx_discount_rules_target (target_type, target_id),

    CONSTRAINT fk_discount_rules_discount_seq
        FOREIGN KEY (discount_seq)
        REFERENCES discounts(discount_seq)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;