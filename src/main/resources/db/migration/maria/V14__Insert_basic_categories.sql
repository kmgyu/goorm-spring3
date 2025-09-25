DELETE FROM categories;
ALTER TABLE categories AUTO_INCREMENT = 1;

-- Insert basic categories data

INSERT INTO categories (name, description, sort_order, is_active, created_seq, updated_seq) VALUES
('전자제품', '컴퓨터, 스마트폰, 가전제품 등', 1, TRUE, 1, 1),
('의류', '남성복, 여성복, 아동복 등', 2, TRUE, 1, 1),
('도서', '소설, 전문서적, 교육서적 등', 3, TRUE, 1, 1),
('생활용품', '주방용품, 욕실용품, 청소용품 등', 4, TRUE, 1, 1),
('식품', '가공식품, 신선식품, 음료 등', 5, TRUE, 1, 1),
('스포츠/레저', '운동용품, 캠핑용품, 레저용품 등', 6, TRUE, 1, 1);