-- 기존 관리자 계정 삭제 후 재생성
DELETE FROM users WHERE email = 'admin@test.com';

INSERT INTO users (email, password, nickname, created_at, updated_at)
VALUES ('admin@test.com', '$2a$10$Zk63iE9f2BM1bff87n7gO.VU9kXav8kHfMciH/SyOWixVDOEGMicq', '관리자', NOW(), NOW());