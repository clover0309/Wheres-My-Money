-- 1. Foreign Key 제약 조건 임시 해제
SET FOREIGN_KEY_CHECKS = 0;

-- 2. stockmaster_code를 표준코드에서 단축코드로 변환
UPDATE stockmaster
SET stockmaster_code = SUBSTRING(stockmaster_code, 4, 6)
WHERE LENGTH(stockmaster_code) = 12 AND stockmaster_code LIKE 'KR7%';

-- 3. Foreign Key 제약 조건 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 4. 변환 결과 확인
SELECT stockmaster_code, stockmaster_name, LENGTH(stockmaster_code) as code_length
FROM stockmaster
LIMIT 10;
