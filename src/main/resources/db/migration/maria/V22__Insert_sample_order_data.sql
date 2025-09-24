-- 전년도 발주 집계 샘플 데이터 및 할인율 이력 생성

-- 누락된 회사 정보만 추가 (ID 1, 2, 3이 없는 경우에만)
INSERT INTO companies (company_name, business_number, representative)
SELECT * FROM (
    SELECT '삼성전자' as company_name, '123-45-67890' as business_number, '이재용' as representative
    UNION ALL SELECT 'LG전자', '098-76-54321', '조성진'
    UNION ALL SELECT '현대자동차', '555-66-77888', '장재훈'
) t
WHERE NOT EXISTS (
    SELECT 1 FROM companies
    WHERE company_name IN ('삼성전자', 'LG전자', '현대자동차')
);

-- 현재 적용 중인 할인율 이력 (2025년부터 적용)
INSERT INTO company_discount_history (company_seq, apply_year, previous_year_amount, discount_rate, effective_from, reason, created_by)
SELECT c.company_seq, '2025', t.previous_year_amount, t.discount_rate, '2025-01-01', t.reason, 'SYSTEM'
FROM companies c
JOIN (
    SELECT '삼성전자' as company_name, 120000000 as previous_year_amount, 10.00 as discount_rate, '삼성전자 - 전년도 누적 발주액 1.2억원 달성' as reason
    UNION ALL SELECT 'LG전자', 80000000, 7.00, 'LG전자 - 전년도 누적 발주액 8천만원 달성'
    UNION ALL SELECT '현대자동차', 40000000, 5.00, '현대자동차 - 전년도 누적 발주액 4천만원 달성'
) t ON c.company_name = t.company_name;

-- 전년도(2024년) 월별 발주 집계 데이터
INSERT INTO order_summary_monthly (company_seq, summary_year, summary_month, order_count, total_amount, discount_amount, final_amount)
SELECT c.company_seq, d.summary_year, d.summary_month, d.order_count, d.total_amount, d.discount_amount, d.final_amount
FROM companies c
JOIN (
    SELECT '삼성전자' as company_name, '2024' as summary_year, '01' as summary_month, 8 as order_count, 12000000 as total_amount, 1200000 as discount_amount, 10800000 as final_amount
    UNION ALL SELECT '삼성전자', '2024', '02', 6, 9500000, 950000, 8550000
    UNION ALL SELECT '삼성전자', '2024', '03', 10, 15000000, 1500000, 13500000
    UNION ALL SELECT '삼성전자', '2024', '04', 7, 8500000, 850000, 7650000
    UNION ALL SELECT '삼성전자', '2024', '05', 9, 11000000, 1100000, 9900000
    UNION ALL SELECT '삼성전자', '2024', '06', 12, 18000000, 1800000, 16200000
    UNION ALL SELECT '삼성전자', '2024', '07', 5, 6000000, 600000, 5400000
    UNION ALL SELECT '삼성전자', '2024', '08', 8, 9000000, 900000, 8100000
    UNION ALL SELECT '삼성전자', '2024', '09', 11, 14000000, 1400000, 12600000
    UNION ALL SELECT '삼성전자', '2024', '10', 9, 12000000, 1200000, 10800000
    UNION ALL SELECT '삼성전자', '2024', '11', 7, 8500000, 850000, 7650000
    UNION ALL SELECT '삼성전자', '2024', '12', 6, 6500000, 650000, 5850000
    UNION ALL SELECT 'LG전자', '2024', '01', 5, 6000000, 420000, 5580000
    UNION ALL SELECT 'LG전자', '2024', '02', 4, 5500000, 385000, 5115000
    UNION ALL SELECT 'LG전자', '2024', '03', 7, 8500000, 595000, 7905000
    UNION ALL SELECT 'LG전자', '2024', '04', 6, 7000000, 490000, 6510000
    UNION ALL SELECT 'LG전자', '2024', '05', 8, 9500000, 665000, 8835000
    UNION ALL SELECT 'LG전자', '2024', '06', 9, 11000000, 770000, 10230000
    UNION ALL SELECT 'LG전자', '2024', '07', 3, 4000000, 280000, 3720000
    UNION ALL SELECT 'LG전자', '2024', '08', 5, 6500000, 455000, 6045000
    UNION ALL SELECT 'LG전자', '2024', '09', 6, 7500000, 525000, 6975000
    UNION ALL SELECT 'LG전자', '2024', '10', 7, 8000000, 560000, 7440000
    UNION ALL SELECT 'LG전자', '2024', '11', 4, 5000000, 350000, 4650000
    UNION ALL SELECT 'LG전자', '2024', '12', 3, 2000000, 140000, 1860000
    UNION ALL SELECT '현대자동차', '2024', '01', 3, 3000000, 150000, 2850000
    UNION ALL SELECT '현대자동차', '2024', '02', 2, 2500000, 125000, 2375000
    UNION ALL SELECT '현대자동차', '2024', '03', 4, 4500000, 225000, 4275000
    UNION ALL SELECT '현대자동차', '2024', '04', 3, 3500000, 175000, 3325000
    UNION ALL SELECT '현대자동차', '2024', '05', 5, 5000000, 250000, 4750000
    UNION ALL SELECT '현대자동차', '2024', '06', 6, 6500000, 325000, 6175000
    UNION ALL SELECT '현대자동차', '2024', '07', 2, 2000000, 100000, 1900000
    UNION ALL SELECT '현대자동차', '2024', '08', 3, 3000000, 150000, 2850000
    UNION ALL SELECT '현대자동차', '2024', '09', 4, 4000000, 200000, 3800000
    UNION ALL SELECT '현대자동차', '2024', '10', 4, 4500000, 225000, 4275000
    UNION ALL SELECT '현대자동차', '2024', '11', 2, 2500000, 125000, 2375000
    UNION ALL SELECT '현대자동차', '2024', '12', 2, 2500000, 125000, 2375000
) d ON c.company_name = d.company_name;