-- 회사별 할인율 샘플 데이터 삽입 (AUTO_INCREMENT 리셋)
DELETE FROM company_discount_history;
DELETE FROM order_summary_monthly;
ALTER TABLE company_discount_history AUTO_INCREMENT = 1;

-- 회사별 할인율 설정 (2024년 기준)
INSERT INTO company_discount_history (
    company_seq, apply_year, previous_year_amount, discount_rate,
    effective_from, effective_to, reason, created_by, created_at
) VALUES
-- 삼성전자: 전년도 구매액 1억 5천만원 → 5% 할인
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1),
 '2024', 150000000.00, 5.00,
 '2024-01-01', '2024-12-31', '대량 구매 고객 우대', 'admin', NOW()),

-- LG전자: 전년도 구매액 8천만원 → 3% 할인
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1),
 '2024', 80000000.00, 3.00,
 '2024-01-01', '2024-12-31', '우수 거래처 할인', 'admin', NOW()),

-- 현대자동차: 전년도 구매액 1억 2천만원 → 5% 할인
((SELECT company_seq FROM companies WHERE company_name='현대자동차' LIMIT 1),
 '2024', 120000000.00, 5.00,
 '2024-01-01', '2024-12-31', '프리미엄 고객 할인', 'admin', NOW());

-- 2025년 할인율 (현재 적용 중)
INSERT INTO company_discount_history (
    company_seq, apply_year, previous_year_amount, discount_rate,
    effective_from, effective_to, reason, created_by, created_at
) VALUES
-- 삼성전자: 전년도 실적 우수 → 6% 할인 (인상)
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1),
 '2025', 180000000.00, 6.00,
 '2025-01-01', '2025-12-31', '전년도 실적 우수로 할인율 인상', 'admin', NOW()),

-- LG전자: 기존 유지 → 3% 할인
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1),
 '2025', 85000000.00, 3.00,
 '2025-01-01', '2025-12-31', '기존 할인율 유지', 'admin', NOW()),

-- 현대자동차: 기존 유지 → 5% 할인
((SELECT company_seq FROM companies WHERE company_name='현대자동차' LIMIT 1),
 '2025', 130000000.00, 5.00,
 '2025-01-01', '2025-12-31', '프리미엄 고객 할인 유지', 'admin', NOW());

-- 전년도 월별 발주 집계 샘플 데이터 (2024년)
INSERT INTO order_summary_monthly (
    company_seq, summary_year, summary_month, order_count,
    total_amount, discount_amount, final_amount
) VALUES
-- 삼성전자 2024년 월별 데이터
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '01', 12, 15000000.00, 750000.00, 14250000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '02', 10, 12000000.00, 600000.00, 11400000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '03', 15, 18000000.00, 900000.00, 17100000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '04', 8, 10000000.00, 500000.00, 9500000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '05', 14, 16000000.00, 800000.00, 15200000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '06', 11, 13000000.00, 650000.00, 12350000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '07', 13, 15500000.00, 775000.00, 14725000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '08', 9, 11000000.00, 550000.00, 10450000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '09', 16, 19000000.00, 950000.00, 18050000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '10', 12, 14000000.00, 700000.00, 13300000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '11', 14, 16500000.00, 825000.00, 15675000.00),
((SELECT company_seq FROM companies WHERE company_name='삼성전자' LIMIT 1), '2024', '12', 10, 12000000.00, 600000.00, 11400000.00),

-- LG전자 2024년 월별 데이터 (일부)
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1), '2024', '01', 8, 7000000.00, 210000.00, 6790000.00),
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1), '2024', '02', 6, 5500000.00, 165000.00, 5335000.00),
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1), '2024', '03', 10, 8500000.00, 255000.00, 8245000.00),
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1), '2024', '06', 7, 6000000.00, 180000.00, 5820000.00),
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1), '2024', '09', 9, 7500000.00, 225000.00, 7275000.00),
((SELECT company_seq FROM companies WHERE company_name='LG전자' LIMIT 1), '2024', '12', 8, 6500000.00, 195000.00, 6305000.00);