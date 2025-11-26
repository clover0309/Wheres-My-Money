-- stockmaster 테이블 재구성 가이드
-- 문제: 12자리 표준코드를 6자리로 변환 시 중복 발생
-- 해결: CSV의 단축코드 컬럼을 사용하여 재생성

-- ============================================
-- 1단계: 현재 데이터 백업 (선택사항)
-- ============================================
CREATE TABLE stockmaster_backup AS SELECT * FROM stockmaster;

-- ============================================
-- 2단계: 기존 테이블 데이터 삭제
-- ============================================
-- Foreign Key 제약 조건 확인
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'stockmaster';

-- userstock 테이블과 연결된 경우 처리 방법:
-- 옵션 A: userstock 데이터도 삭제 (간단)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE stockmaster;
TRUNCATE TABLE userstock;  -- 사용자 주식 데이터도 삭제 (주의!)
SET FOREIGN_KEY_CHECKS = 1;

-- 옵션 B: userstock 데이터 보존 (복잡)
-- 1) userstock의 12자리 코드를 6자리로 변환
-- 2) Foreign Key 제약 조건 임시 해제
-- 3) stockmaster 데이터 삭제
-- 4) 새 데이터 삽입
-- 5) Foreign Key 제약 조건 재설정

-- ============================================
-- 3단계: 새로운 SQL 파일 생성 및 실행
-- ============================================
-- Windows CMD에서 실행:
-- cd e:\Side_Project\a\Wheres-My-Money\auto stockdata
-- node generate_sql_fixed.js

-- 그 다음 MySQL Workbench에서:
-- File > Open SQL Script > insert_stockmaster.sql 선택
-- Execute 버튼 클릭

-- ============================================
-- 4단계: 데이터 검증
-- ============================================
-- 총 레코드 수 확인
SELECT COUNT(*) as total_records FROM stockmaster;

-- 코드 길이 확인 (모두 6자리여야 함)
SELECT 
    LENGTH(stockmaster_code) as code_length,
    COUNT(*) as count
FROM stockmaster
GROUP BY LENGTH(stockmaster_code);

-- 샘플 데이터 확인
SELECT * FROM stockmaster LIMIT 10;

-- 중복 확인 (중복이 없어야 함)
SELECT 
    stockmaster_code,
    COUNT(*) as count
FROM stockmaster
GROUP BY stockmaster_code
HAVING COUNT(*) > 1;

-- ============================================
-- 5단계: userstock 테이블 처리
-- ============================================
-- userstock에 12자리 코드가 있다면 6자리로 변환
UPDATE userstock
SET userstock_stk = SUBSTRING(userstock_stk, 4, 6)
WHERE LENGTH(userstock_stk) = 12 AND userstock_stk LIKE 'KR7%';

-- Foreign Key 제약 조건 확인
SELECT 
    u.userstock_idx,
    u.userstock_stk,
    u.userstock_name,
    s.stockmaster_code,
    s.stockmaster_name
FROM userstock u
LEFT JOIN stockmaster s ON u.userstock_stk = s.stockmaster_code
WHERE s.stockmaster_code IS NULL;  -- 매칭되지 않는 항목 찾기

-- ============================================
-- 백업 복원 (문제 발생 시)
-- ============================================
-- TRUNCATE TABLE stockmaster;
-- INSERT INTO stockmaster SELECT * FROM stockmaster_backup;
-- DROP TABLE stockmaster_backup;
