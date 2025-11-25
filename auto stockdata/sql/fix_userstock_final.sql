-- 1. 현재 userstock 테이블 확인
SELECT * FROM userstock LIMIT 5;

-- 2. userstock_stk 컬럼을 VARCHAR(12)로 변경
ALTER TABLE userstock 
MODIFY COLUMN userstock_stk VARCHAR(12) NOT NULL;

-- 3. 변경 확인
SHOW COLUMNS FROM userstock WHERE Field = 'userstock_stk';

-- 4. Foreign Key 제약조건 확인
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'wheresmymoney'
  AND TABLE_NAME = 'userstock'
  AND REFERENCED_TABLE_NAME = 'stockmaster';
