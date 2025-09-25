-- 회사 마스터 테이블 생성 및 기초 데이터

-- 회사 마스터 테이블 생성
CREATE TABLE IF NOT EXISTS companies (
    company_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL COMMENT '회사명',
    business_number VARCHAR(20) COMMENT '사업자번호',
    representative VARCHAR(50) COMMENT '대표자명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기존 데이터 삭제 후 AUTO_INCREMENT 리셋
DELETE FROM companies;
ALTER TABLE companies AUTO_INCREMENT = 1;

-- 회사 기초 데이터
INSERT INTO companies (company_name, business_number, representative) VALUES
('삼성전자', '123-45-67890', '이재용'),
('LG전자', '098-76-54321', '조성진'),
('현대자동차', '555-66-77888', '장재훈');

-- 기존 users 테이블의 company_seq를 NULL로 초기화 (외래키 제약조건 추가 전)
UPDATE users SET company_seq = NULL WHERE company_seq IS NOT NULL;

-- 외래키 제약 조건 추가 (이미 존재하는 경우 스킵)
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'goorm_db'
    AND TABLE_NAME = 'users'
    AND CONSTRAINT_NAME = 'fk_users_company');

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_company FOREIGN KEY (company_seq) REFERENCES companies(company_seq)',
    'SELECT "Constraint already exists" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 바이어 계정 생성 (비밀번호: 1234) - 중복 방지
INSERT IGNORE INTO users (email, password, nickname, role, company_seq, created_at) VALUES
('buyer1@samsung.com', '$2a$10$Zk63iE9f2BM1bff87n7gO.VU9kXav8kHfMciH/SyOWixVDOEGMicq', '김바이어', 'BUYER', 1, NOW()),
('buyer2@lg.com', '$2a$10$Zk63iE9f2BM1bff87n7gO.VU9kXav8kHfMciH/SyOWixVDOEGMicq', '박바이어', 'BUYER', 2, NOW()),
('buyer3@hyundai.com', '$2a$10$Zk63iE9f2BM1bff87n7gO.VU9kXav8kHfMciH/SyOWixVDOEGMicq', '최바이어', 'BUYER', 3, NOW());