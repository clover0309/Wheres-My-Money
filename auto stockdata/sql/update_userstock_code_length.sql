-- userstock 테이블의 userstock_stk 컬럼 길이를 12에서 6으로 변경
-- 이 변경은 stockmaster 테이블의 기본 키(6자리 단축코드)와 일치시키기 위함입니다.

-- 1. 먼저 기존 데이터가 6자리인지 확인
SELECT userstock_stk, LENGTH(userstock_stk) as code_length, COUNT(*) as count
FROM userstock
GROUP BY LENGTH(userstock_stk)
ORDER BY code_length;

-- 2. 12자리 코드가 있다면 6자리로 변환 (KR7XXXXXX000 -> XXXXXX)
-- 이 쿼리는 필요한 경우에만 실행하세요
UPDATE userstock
SET userstock_stk = SUBSTRING(userstock_stk, 4, 6)
WHERE LENGTH(userstock_stk) = 12 AND userstock_stk LIKE 'KR7%';

-- 3. 컬럼 길이 변경
ALTER TABLE userstock
MODIFY COLUMN userstock_stk VARCHAR(6) NOT NULL;

-- 4. 변경 확인
DESCRIBE userstock;

-- 5. 데이터 확인
SELECT * FROM userstock LIMIT 10;
