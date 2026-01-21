-- V13: Add anonymous permission system
-- 익명 권한 시스템 추가

-- 1. users 테이블에 익명 권한 컬럼 추가
ALTER TABLE users
ADD COLUMN can_access_anonymous BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. 익명 권한 비밀번호 테이블 생성
CREATE TABLE anonymous_permission_passwords (
    id UUID PRIMARY KEY,
    password VARCHAR(100) NOT NULL
);

-- 3. 기본 비밀번호 삽입 (관리자가 SQL로 직접 변경)
INSERT INTO anonymous_permission_passwords (id, password)
VALUES ('00000000-0000-0000-0000-000000000001', 'change_this_password');

COMMENT ON TABLE anonymous_permission_passwords IS '익명 권한 비밀번호 테이블 (관리자가 SQL로 직접 관리)';
COMMENT ON COLUMN anonymous_permission_passwords.password IS '평문 비밀번호 - 관리자가 직접 변경 필요';
COMMENT ON COLUMN users.can_access_anonymous IS '익명 작성/조회 권한 보유 여부';
