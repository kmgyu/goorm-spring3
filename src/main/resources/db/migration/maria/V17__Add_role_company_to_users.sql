-- 사용자 테이블에 역할과 회사 정보 추가

-- role 컬럼 존재 여부 확인 후 추가
SET @role_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'goorm_db'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'role');

SET @role_sql = IF(@role_exists = 0,
    'ALTER TABLE users ADD COLUMN role ENUM(''ADMIN'', ''BUYER'') DEFAULT ''BUYER'' COMMENT ''사용자 역할 (ADMIN: 관리자, BUYER: 바이어)''',
    'SELECT "Role column already exists" AS message');

PREPARE stmt FROM @role_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- company_seq 컬럼 존재 여부 확인 후 추가
SET @company_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'goorm_db'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'company_seq');

SET @company_sql = IF(@company_exists = 0,
    'ALTER TABLE users ADD COLUMN company_seq BIGINT NULL COMMENT ''소속 회사 번호 (바이어만 해당)''',
    'SELECT "Company_seq column already exists" AS message');

PREPARE stmt FROM @company_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 기존 사용자를 관리자로 설정 (role 컬럼이 존재할 때만)
UPDATE users SET role = 'ADMIN' WHERE user_seq <= 2 AND EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'goorm_db'
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role'
);