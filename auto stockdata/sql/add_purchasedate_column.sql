-- userstock 테이블의 매수일 컬럼 업데이트 (기존 컬럼이 있는 경우)

-- 1단계: 기존 데이터에 대해 createdat을 purchasedate로 설정 (NULL인 경우만)
UPDATE userstock 
SET userstock_purchasedate = DATE(userstock_createdat)
WHERE userstock_purchasedate IS NULL;

-- 2단계: NOT NULL 제약조건 추가
ALTER TABLE userstock
MODIFY COLUMN userstock_purchasedate DATE NOT NULL COMMENT '주식 매수 날짜';

-- 3단계: 추가된 컬럼 확인
DESCRIBE userstock;
