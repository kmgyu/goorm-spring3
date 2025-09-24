-- V25__orders_and_order_items_procurement_update.sql
-- 목적:
--   1) orders 테이블: 발주용 필드 추가 + status ENUM을 'PENDING','APPROVED' 로 축소
--   2) order_items 테이블: 할인율/라인합계 등 추가
-- 주의:
--   - 실습 환경 가정. 운영 데이터가 있다면 ENUM 축소 전에 백업 권장.
--   - 기존 인덱스/컬럼은 유지. "새 컬럼" 관련 인덱스만 추가.

/* ===========================
   0) 사전 정리: status 값 정규화
   (ENUM 축소 전에 기존 값을 두 값 체계로 맵핑)
   =========================== */

-- 배송/출하 완료 → 승인으로 승격, 배송상태에 분리 저장
UPDATE orders
SET status = 'APPROVED'
WHERE status IN ('CONFIRMED','PROCESSING','SHIPPED','DELIVERED');

-- 취소건은 결제상태에 'CANCELLED' 기록 후 상태는 대기로 통일(실습 정책)
UPDATE orders
SET payment_status = 'CANCELLED'
WHERE status = 'CANCELLED' AND (payment_status IS NULL OR payment_status <> 'CANCELLED');

UPDATE orders
SET status = 'PENDING'
WHERE status = 'CANCELLED';


/* ===========================
   1) ORDERS: 컬럼 추가 (새 필드만)
   =========================== */

-- 배송 상태 컬럼(새로 추가)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_status ENUM('ORDER_COMPLETED','DELIVERY_COMPLETED')
        DEFAULT 'ORDER_COMPLETED' COMMENT '배송 상태' AFTER status;

-- 발주 관련 컬럼(새로 추가)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS company_seq BIGINT NULL COMMENT '발주 회사' AFTER delivery_status,
    ADD COLUMN IF NOT EXISTS user_seq BIGINT NULL COMMENT '발주자' AFTER company_seq,
    ADD COLUMN IF NOT EXISTS discount_rate DECIMAL(5,2) DEFAULT 0 COMMENT '적용된 할인율 (%)' AFTER discount_amount,
    ADD COLUMN IF NOT EXISTS final_amount DECIMAL(15,2) DEFAULT 0 COMMENT '최종 금액' AFTER total_amount,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(50) NULL COMMENT '승인자' AFTER payment_status,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP NULL COMMENT '승인일시' AFTER approved_by,
    ADD COLUMN IF NOT EXISTS notes TEXT NULL COMMENT '비고' AFTER approved_at,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 COMMENT '낙관적 락' AFTER updated_seq;


/* ===========================
   2) ORDERS: ENUM 축소(변경)
   =========================== */

ALTER TABLE orders
    MODIFY COLUMN status ENUM('PENDING','APPROVED')
        DEFAULT 'PENDING'
        COMMENT '발주 상태';


/* ===========================
   3) ORDERS: FK 추가 (새 FK만)
   =========================== */

-- 회사 FK (companies.company_seq)
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_company_seq
        FOREIGN KEY (company_seq) REFERENCES companies(company_seq)
        ON DELETE RESTRICT ON UPDATE CASCADE;

-- 발주자 FK (users.user_seq) - 기존 created_seq FK와 별개
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user_seq_2
        FOREIGN KEY (user_seq) REFERENCES users(user_seq)
        ON DELETE RESTRICT ON UPDATE CASCADE;


/* ===========================
   4) ORDERS: 신규 컬럼용 인덱스만 추가
   (기존 인덱스는 유지)
   =========================== */

-- company_seq + status로 조회 최적화
CREATE INDEX IF NOT EXISTS idx_company_status ON orders (company_seq, status);

-- 배송 상태 필터링
CREATE INDEX IF NOT EXISTS idx_delivery_status ON orders (delivery_status);

-- 승인일시 검색이 잦다면(선택)
-- CREATE INDEX IF NOT EXISTS idx_orders_approved_at ON orders (approved_at);


/* ===========================
   5) ORDER_ITEMS: 컬럼 추가 (새 필드만)
   =========================== */

-- order_items 테이블 컬럼 정리: 일관된 네이밍과 불필요한 컬럼 제거
ALTER TABLE order_items
    DROP COLUMN IF EXISTS discount_amount,
    DROP COLUMN IF EXISTS total_amount;


/* ===========================
   6) ORDER_ITEMS: 신규 인덱스만 추가
   (기존 인덱스는 유지)
   =========================== */

-- 조합 조회 최적화용 복합 인덱스
CREATE INDEX IF NOT EXISTS idx_order_product ON order_items (order_seq, product_seq);
