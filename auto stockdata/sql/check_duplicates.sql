-- 표준코드를 단축코드로 변환했을 때 중복이 발생하는지 확인

-- 1. 변환 후 중복될 종목 확인
SELECT 
    SUBSTRING(stockmaster_code, 4, 6) as short_code,
    COUNT(*) as count,
    GROUP_CONCAT(stockmaster_code) as original_codes,
    GROUP_CONCAT(stockmaster_name) as names
FROM stockmaster
WHERE LENGTH(stockmaster_code) = 12 AND stockmaster_code LIKE 'KR7%'
GROUP BY SUBSTRING(stockmaster_code, 4, 6)
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- 2. 특정 중복 케이스 상세 조회 (33626K 예시)
SELECT 
    stockmaster_code,
    stockmaster_name,
    stockmaster_market,
    stockmaster_sector,
    SUBSTRING(stockmaster_code, 4, 6) as short_code
FROM stockmaster
WHERE SUBSTRING(stockmaster_code, 4, 6) = '33626K'
   OR stockmaster_code LIKE '%33626K%';

-- 3. 이미 6자리 코드가 있는지 확인
SELECT 
    stockmaster_code,
    stockmaster_name,
    LENGTH(stockmaster_code) as code_length
FROM stockmaster
WHERE LENGTH(stockmaster_code) = 6
LIMIT 20;

-- 4. 총 데이터 현황
SELECT 
    LENGTH(stockmaster_code) as code_length,
    COUNT(*) as count
FROM stockmaster
GROUP BY LENGTH(stockmaster_code)
ORDER BY code_length;
