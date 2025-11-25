-- userstock_stk 컬럼 크기를 6에서 12로 변경
ALTER TABLE userstock 
MODIFY COLUMN userstock_stk VARCHAR(12) NOT NULL;

-- 변경 확인
SHOW COLUMNS FROM userstock WHERE Field = 'userstock_stk';
