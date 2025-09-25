-- 인보이스 출력 이력 테이블
CREATE TABLE invoice_history (
    print_seq BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '출력 시퀀스',
    order_seq BIGINT NOT NULL COMMENT '주문 시퀀스',
    invoice_id VARCHAR(50) NOT NULL COMMENT '인보이스 ID (INV-20251224-001)',
    printed_at DATETIME NOT NULL COMMENT '출력일시',
    printed_by_seq BIGINT NOT NULL COMMENT '출력자 시퀀스',
    printed_by VARCHAR(100) NOT NULL COMMENT '출력자명',
    print_count INT DEFAULT 1 COMMENT '출력 횟수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_order_seq (order_seq),
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_printed_at (printed_at),
    INDEX idx_printed_by (printed_by_seq),

    CONSTRAINT fk_invoice_history_order
        FOREIGN KEY (order_seq) REFERENCES orders(order_seq)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='인보이스 출력 이력 관리';

-- 주문 테이블에 입금 관련 컬럼 추가
ALTER TABLE orders ADD COLUMN payment_due_date DATE COMMENT '입금 예정일';
ALTER TABLE orders ADD COLUMN payment_completed_date DATETIME COMMENT '실제 입금일';
ALTER TABLE orders ADD COLUMN invoice_generated_at DATETIME COMMENT '인보이스 확정일';
ALTER TABLE orders ADD COLUMN payment_completed_by_seq BIGINT COMMENT '입금 완료 처리자 ID';
ALTER TABLE orders ADD COLUMN payment_completed_by VARCHAR(100) COMMENT '입금 완료 처리자명';
ALTER TABLE orders ADD COLUMN delivery_completed_by_seq BIGINT COMMENT '배송 완료 처리자 ID';
ALTER TABLE orders ADD COLUMN delivery_completed_at DATETIME COMMENT '배송 완료 시점';

-- 인덱스 추가
ALTER TABLE orders ADD INDEX idx_payment_status (payment_status);
ALTER TABLE orders ADD INDEX idx_payment_due_date (payment_due_date);
ALTER TABLE orders ADD INDEX idx_invoice_generated_at (invoice_generated_at);