-- stockmaster 테이블 확인 쿼리

-- 1. 삽입된 총 레코드 수 확인
SELECT COUNT(*) as total_records FROM stockmaster;

-- 2. 샘플 데이터 확인 (처음 10개)
SELECT * FROM stockmaster LIMIT 10;

-- 3. 한글이 제대로 저장되었는지 확인
SELECT stockmaster_code, stockmaster_name, stockmaster_market, stockmaster_sector 
FROM stockmaster 
WHERE stockmaster_name LIKE '%삼성%' 
OR stockmaster_name LIKE '%LG%'
LIMIT 10;

-- 4. NULL 값이 있는 레코드 확인
SELECT COUNT(*) as null_sector_count 
FROM stockmaster 
WHERE stockmaster_sector IS NULL;

-- 5. 중복 코드 확인
SELECT stockmaster_code, COUNT(*) as duplicate_count 
FROM stockmaster 
GROUP BY stockmaster_code 
HAVING COUNT(*) > 1;

-- 6. 빈 이름 확인
SELECT COUNT(*) as empty_name_count 
FROM stockmaster 
WHERE stockmaster_name = '' OR stockmaster_name IS NULL;

-- 7. 시장 구분별 통계
SELECT stockmaster_market, COUNT(*) as count 
FROM stockmaster 
GROUP BY stockmaster_market 
ORDER BY count DESC;

-- 8. 소속부별 통계
SELECT stockmaster_sector, COUNT(*) as count 
FROM stockmaster 
GROUP BY stockmaster_sector 
ORDER BY count DESC 
LIMIT 20;
