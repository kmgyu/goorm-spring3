DELETE FROM products;
ALTER TABLE products AUTO_INCREMENT = 1;

INSERT INTO products (
    code, name, description, category_seq, supplier_seq,
    unit_price, unit_cost, unit, sku, barcode, weight, dimensions,
    status, image_url, created_at, updated_at, created_seq, updated_seq
) VALUES
-- 전자제품
('PROD-001', '갤럭시 S24', '삼성 갤럭시 S24 스마트폰 256GB',
 (SELECT category_seq FROM categories WHERE name='전자제품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='삼성전자'),
 1200000, 950000, 'EA', 'SKU-001', '8801234567001', 0.168, '70.6 x 147.0 x 7.6 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-002', 'iPhone 15', '애플 아이폰 15 128GB',
 (SELECT category_seq FROM categories WHERE name='전자제품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='애플'),
 1100000, 850000, 'EA', 'SKU-002', '8801234567002', 0.171, '71.6 x 147.6 x 7.8 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-003', 'LG 그램 17', 'LG 그램 17인치 노트북 i7 16GB',
 (SELECT category_seq FROM categories WHERE name='전자제품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='LG전자'),
 1800000, 1500000, 'EA', 'SKU-003', '8801234567003', 1.350, '378.0 x 258.8 x 17.8 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-004', '에어팟 프로', '애플 에어팟 프로 2세대',
 (SELECT category_seq FROM categories WHERE name='전자제품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='애플'),
 350000, 280000, 'EA', 'SKU-004', '8801234567004', 0.050, '45.2 x 60.9 x 21.7 mm', 'INACTIVE', NULL, NOW(), NOW(), 1, 1),

-- 의류
('PROD-005', '유니클로 히트텍', '유니클로 히트텍 긴팔 티셔츠',
 (SELECT category_seq FROM categories WHERE name='의류'),
 (SELECT supplier_seq FROM suppliers  WHERE name='패션플러스'),
 29000, 18000, 'EA', 'SKU-005', '8801234567005', 0.200, 'FREE SIZE', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-006', '나이키 에어맥스', '나이키 에어맥스 운동화',
 (SELECT category_seq FROM categories WHERE name='의류'),
 (SELECT supplier_seq FROM suppliers  WHERE name='나이키'),
 180000, 120000, 'SET', 'SKU-006', '8801234567006', 0.800, '280mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-007', 'H&M 청바지', 'H&M 슬림핏 청바지',
 (SELECT category_seq FROM categories WHERE name='의류'),
 (SELECT supplier_seq FROM suppliers  WHERE name='패션플러스'),
 59000, 35000, 'EA', 'SKU-007', '8801234567007', 0.500, '32인치', 'DISCONTINUED', NULL, NOW(), NOW(), 1, 1),

-- 도서
('PROD-008', '스프링 부트 완벽가이드', 'Spring Boot 3.x 실전 가이드북',
 (SELECT category_seq FROM categories WHERE name='도서'),
 (SELECT supplier_seq FROM suppliers  WHERE name='북스토어'),
 45000, 32000, 'EA', 'SKU-008', '8801234567008', 0.800, '188 x 257 x 35 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-009', '클린코드', '로버트 C. 마틴의 클린코드',
 (SELECT category_seq FROM categories WHERE name='도서'),
 (SELECT supplier_seq FROM suppliers  WHERE name='북스토어'),
 33000, 23000, 'EA', 'SKU-009', '8801234567009', 0.600, '152 x 225 x 25 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

-- 생활용품
('PROD-010', '다이슨 청소기', '다이슨 V15 무선청소기',
 (SELECT category_seq FROM categories WHERE name='생활용품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='다이슨'),
 890000, 650000, 'EA', 'SKU-010', '8801234567010', 2.200, '1257 x 250 x 166 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-011', '샤오미 공기청정기', '샤오미 미에어 공기청정기 3H',
 (SELECT category_seq FROM categories WHERE name='생활용품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='샤오미'),
 280000, 200000, 'EA', 'SKU-011', '8801234567011', 4.800, '240 x 240 x 520 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

-- 식품
('PROD-012', '동원참치', '동원 라이트스탠다드 참치캔 150g',
 (SELECT category_seq FROM categories WHERE name='식품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='프레시마트'),
 2500, 1800, 'EA', 'SKU-012', '8801234567012', 0.150, '85 x 110 x 25 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-013', '농심 신라면', '농심 신라면 5개입',
 (SELECT category_seq FROM categories WHERE name='식품'),
 (SELECT supplier_seq FROM suppliers  WHERE name='프레시마트'),
 4200, 3000, 'PACK', 'SKU-013', '8801234567013', 0.600, '180 x 120 x 140 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

-- 스포츠/레저
('PROD-014', '윌슨 테니스라켓', '윌슨 프로스태프 테니스라켓',
 (SELECT category_seq FROM categories WHERE name='스포츠/레저'),
 (SELECT supplier_seq FROM suppliers  WHERE name='윌슨'),
 320000, 220000, 'EA', 'SKU-014', '8801234567014', 0.310, '685 x 280 x 25 mm', 'ACTIVE', NULL, NOW(), NOW(), 1, 1),

('PROD-015', '나이키 축구공', '나이키 프리미어리그 공식구',
 (SELECT category_seq FROM categories WHERE name='스포츠/레저'),
 (SELECT supplier_seq FROM suppliers  WHERE name='나이키'),
 85000, 60000, 'EA', 'SKU-015', '8801234567015', 0.450, '직경 22cm', 'INACTIVE', NULL, NOW(), NOW(), 1, 1);
