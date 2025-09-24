-- 재고 샘플 데이터 삽입 (AUTO_INCREMENT 리셋)
DELETE FROM inventory;
ALTER TABLE inventory AUTO_INCREMENT = 1;

-- 상품별 재고 데이터 삽입
INSERT INTO inventory (
    product_seq, location, current_stock, reserved_stock,
    min_stock_level, max_stock_level, reorder_point,
    last_stock_check, created_at, updated_at, created_seq, updated_seq
) VALUES
-- 전자제품
((SELECT product_seq FROM products WHERE code='PROD-001'), 'MAIN_WAREHOUSE', 150, 10, 20, 500, 50, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-002'), 'MAIN_WAREHOUSE', 200, 15, 30, 600, 80, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-003'), 'MAIN_WAREHOUSE', 80, 5, 10, 200, 30, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-004'), 'MAIN_WAREHOUSE', 0, 0, 20, 300, 50, NOW(), NOW(), NOW(), 1, 1), -- 품절

-- 의류
((SELECT product_seq FROM products WHERE code='PROD-005'), 'MAIN_WAREHOUSE', 500, 50, 100, 2000, 200, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-006'), 'MAIN_WAREHOUSE', 120, 8, 25, 400, 60, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-007'), 'MAIN_WAREHOUSE', 5, 0, 0, 0, 0, NOW(), NOW(), NOW(), 1, 1), -- 단종 예정

-- 도서
((SELECT product_seq FROM products WHERE code='PROD-008'), 'MAIN_WAREHOUSE', 300, 20, 50, 1000, 100, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-009'), 'MAIN_WAREHOUSE', 250, 15, 40, 800, 80, NOW(), NOW(), NOW(), 1, 1),

-- 생활용품
((SELECT product_seq FROM products WHERE code='PROD-010'), 'MAIN_WAREHOUSE', 45, 3, 5, 100, 15, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-011'), 'MAIN_WAREHOUSE', 60, 4, 8, 150, 20, NOW(), NOW(), NOW(), 1, 1),

-- 식품
((SELECT product_seq FROM products WHERE code='PROD-012'), 'MAIN_WAREHOUSE', 1000, 50, 200, 5000, 500, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-013'), 'MAIN_WAREHOUSE', 800, 40, 150, 3000, 300, NOW(), NOW(), NOW(), 1, 1),

-- 스포츠/레저
((SELECT product_seq FROM products WHERE code='PROD-014'), 'MAIN_WAREHOUSE', 25, 2, 5, 80, 15, NOW(), NOW(), NOW(), 1, 1),
((SELECT product_seq FROM products WHERE code='PROD-015'), 'MAIN_WAREHOUSE', 0, 0, 10, 200, 30, NOW(), NOW(), NOW(), 1, 1); -- 품절