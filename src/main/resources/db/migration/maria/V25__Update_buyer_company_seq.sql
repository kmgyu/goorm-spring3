-- 바이어 계정의 회사 정보 업데이트

-- buyer1@samsung.com → 삼성전자 (company_seq = 1)
UPDATE users
SET company_seq = (SELECT company_seq FROM companies WHERE company_name = '삼성전자' LIMIT 1)
WHERE email = 'buyer1@samsung.com' AND role = 'BUYER';

-- buyer2@lg.com → LG전자 (company_seq = 2)
UPDATE users
SET company_seq = (SELECT company_seq FROM companies WHERE company_name = 'LG전자' LIMIT 1)
WHERE email = 'buyer2@lg.com' AND role = 'BUYER';

-- buyer3@hyundai.com → 현대자동차 (company_seq = 3)
UPDATE users
SET company_seq = (SELECT company_seq FROM companies WHERE company_name = '현대자동차' LIMIT 1)
WHERE email = 'buyer3@hyundai.com' AND role = 'BUYER';