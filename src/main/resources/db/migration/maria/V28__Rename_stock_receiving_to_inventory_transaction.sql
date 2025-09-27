-- error fix : payment status
ALTER TABLE orders
    MODIFY COLUMN payment_status ENUM('PENDING', 'COMPLETED') NOT NULL;

-- V28: stock_receiving 테이블을 inventory_transaction으로 변경하고 입출고 통합 관리

-- 1단계: 테이블 이름 변경
ALTER TABLE stock_receiving RENAME TO inventory_transaction;

-- 2단계: 새로운 컬럼 추가
ALTER TABLE inventory_transaction
ADD COLUMN transaction_type ENUM('RECEIVING', 'ORDER_CONSUMED') NOT NULL DEFAULT 'RECEIVING' COMMENT '거래 유형 (입고/출고)';

ALTER TABLE inventory_transaction
ADD COLUMN order_seq BIGINT NULL COMMENT '주문 참조 (출고 시)';

-- 3단계: 기존 데이터 처리 (모두 RECEIVING으로 설정)
UPDATE inventory_transaction SET transaction_type = 'RECEIVING';

-- 4단계: 외래키 추가
ALTER TABLE inventory_transaction
ADD CONSTRAINT fk_inventory_transaction_order
FOREIGN KEY (order_seq) REFERENCES orders(order_seq) ON DELETE SET NULL;

-- 5단계: 인덱스 추가 (성능 최적화)
CREATE INDEX idx_inventory_transaction_type_date ON inventory_transaction (transaction_type, processed_at);
CREATE INDEX idx_inventory_transaction_product ON inventory_transaction (product_seq, processed_at);
CREATE INDEX idx_inventory_transaction_order ON inventory_transaction (order_seq);