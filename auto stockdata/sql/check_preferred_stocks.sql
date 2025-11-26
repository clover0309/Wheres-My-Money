-- 우선주와 보통주 구분 확인 쿼리

-- 1. 우선주 종목 확인 (종목명에 '우선주' 포함)
SELECT 
    stockmaster_code,
    stockmaster_name,
    stockmaster_market
FROM stockmaster
WHERE stockmaster_name LIKE '%우선주%'
   OR stockmaster_name LIKE '%1우%'
   OR stockmaster_name LIKE '%2우%'
   OR stockmaster_name LIKE '%3우%'
ORDER BY stockmaster_name
LIMIT 20;

-- 2. 동일 회사의 보통주와 우선주 비교
SELECT 
    stockmaster_code,
    stockmaster_name,
    CASE 
        WHEN stockmaster_name LIKE '%우선주%' THEN '우선주'
        WHEN stockmaster_name LIKE '%보통주%' THEN '보통주'
        ELSE '기타'
    END as stock_type
FROM stockmaster
WHERE stockmaster_name LIKE 'CJ%'
   OR stockmaster_name LIKE 'BYC%'
   OR stockmaster_name LIKE '삼성%'
ORDER BY 
    REPLACE(REPLACE(stockmaster_name, '우선주', ''), '보통주', ''),
    stock_type;

-- 3. 단축코드 길이 확인 (6자리 또는 6자리+알파벳)
SELECT 
    LENGTH(stockmaster_code) as code_length,
    COUNT(*) as count,
    GROUP_CONCAT(stockmaster_code LIMIT 5) as examples
FROM stockmaster
WHERE LENGTH(stockmaster_code) <= 6
GROUP BY LENGTH(stockmaster_code);

-- 4. 알파벳이 포함된 종목 코드 (주로 우선주)
SELECT 
    stockmaster_code,
    stockmaster_name,
    stockmaster_market
FROM stockmaster
WHERE stockmaster_code REGEXP '[A-Z]'
ORDER BY stockmaster_code
LIMIT 20;
