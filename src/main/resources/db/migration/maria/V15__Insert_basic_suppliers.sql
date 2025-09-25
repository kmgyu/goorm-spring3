
DELETE FROM suppliers;
ALTER TABLE suppliers AUTO_INCREMENT = 1;

-- Insert basic suppliers data
INSERT INTO suppliers (name, contact_person, email, phone, address, description, is_active, created_seq, updated_seq) VALUES
('삼성전자', '김삼성', 'contact@samsung.com', '02-1234-5678', '서울특별시 강남구', '전자제품 공급업체', TRUE, 1, 1),
('LG전자', '이엘지', 'contact@lg.com', '02-2345-6789', '서울특별시 서초구', '가전제품 공급업체', TRUE, 1, 1),
('패션플러스', '박패션', 'contact@fashionplus.com', '02-3456-7890', '서울특별시 강서구', '의류 공급업체', TRUE, 1, 1),
('북스토어', '최도서', 'contact@bookstore.com', '02-4567-8901', '서울특별시 마포구', '도서 공급업체', TRUE, 1, 1),
('라이프굿즈', '정생활', 'contact@lifegoods.com', '02-5678-9012', '서울특별시 용산구', '생활용품 공급업체', TRUE, 1, 1),
('프레시마트', '김신선', 'contact@freshmart.com', '02-6789-0123', '서울특별시 송파구', '식품 공급업체', TRUE, 1, 1),

-- 신규 추가
('애플', '팀 애플', 'contact@apple.com', '02-7890-1234', '서울특별시 종로구', '애플 공식 공급업체', TRUE, 1, 1),
('다이슨', '존 다이슨', 'contact@dyson.com', '02-8901-2345', '서울특별시 용산구', '청소기 및 생활가전 공급업체', TRUE, 1, 1),
('샤오미', '리샤오미', 'contact@xiaomi.com', '02-9012-3456', '서울특별시 구로구', '가전 및 전자제품 공급업체', TRUE, 1, 1),
('나이키', '박나이키', 'contact@nike.com', '02-0123-4567', '서울특별시 강동구', '스포츠/의류 공급업체', TRUE, 1, 1),
('윌슨', '이윌슨', 'contact@wilson.com', '02-2345-6780', '서울특별시 성동구', '스포츠용품 공급업체', TRUE, 1, 1);
