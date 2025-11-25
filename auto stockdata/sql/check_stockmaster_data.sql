-- stockmaster 테이블의 포스코홀딩스 데이터 확인
SELECT stockmaster_code, stockmaster_name, LENGTH(stockmaster_code) as code_length
FROM stockmaster 
WHERE stockmaster_name LIKE '%포스코홀딩스%';

-- stockmaster_code 컬럼 구조 확인
SHOW COLUMNS FROM stockmaster WHERE Field = 'stockmaster_code';

-- 샘플 데이터 확인 (코드 길이별)
SELECT 
    LENGTH(stockmaster_code) as code_length,
    COUNT(*) as count,
    GROUP_CONCAT(stockmaster_name SEPARATOR ', ') as sample_names
FROM stockmaster
GROUP BY LENGTH(stockmaster_code);
