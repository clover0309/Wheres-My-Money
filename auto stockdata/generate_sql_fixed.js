const fs = require('fs');
const iconv = require('iconv-lite');
const path = require('path');

console.log('SQL 스크립트 생성 시작...');

// CSV 파일 경로 확인
const csvPath = path.join(__dirname, 'data_5704_20251125.csv');
if (!fs.existsSync(csvPath)) {
    console.error('❌ CSV 파일을 찾을 수 없습니다:', csvPath);
    process.exit(1);
}

// 다양한 인코딩으로 시도
const encodings = ['utf-8', 'euc-kr', 'cp949', 'utf-16le'];
let csvContent = null;
let usedEncoding = null;

for (const encoding of encodings) {
    try {
        const buffer = fs.readFileSync(csvPath);
        const content = iconv.decode(buffer, encoding);
        
        // 한글이 제대로 보이는지 확인
        if (content.includes('표준코드') && content.includes('한글')) {
            csvContent = content;
            usedEncoding = encoding;
            console.log(`✓ 사용된 인코딩: ${encoding}`);
            break;
        }
    } catch (e) {
        continue;
    }
}

if (!csvContent) {
    console.error('❌ CSV 파일의 인코딩을 감지할 수 없습니다.');
    process.exit(1);
}

// 개선된 CSV 파싱 함수
function parseCSVLine(line) {
    const result = [];
    let current = '';
    let inQuotes = false;
    
    for (let i = 0; i < line.length; i++) {
        const char = line[i];
        
        if (char === '"') {
            if (inQuotes && line[i + 1] === '"') {
                // 이스케이프된 큰따옴표
                current += '"';
                i++;
            } else {
                // 큰따옴표 토글
                inQuotes = !inQuotes;
            }
        } else if (char === ',' && !inQuotes) {
            // 필드 구분자
            result.push(current.trim());
            current = '';
        } else {
            current += char;
        }
    }
    result.push(current.trim());
    return result;
}

const lines = csvContent.split(/\r?\n/);
const sqlStatements = [];

sqlStatements.push('-- stockmaster 테이블 데이터 삽입 스크립트');
sqlStatements.push('-- 생성일: 2025-11-25');
sqlStatements.push('-- 인코딩: UTF-8\n');
sqlStatements.push('-- 기존 데이터 삭제 (필요시 주석 해제)');
sqlStatements.push('-- DELETE FROM stockmaster;\n');

let count = 0;
let skipped = 0;

console.log(`✓ 총 ${lines.length - 1}개의 행 처리 시작...`);

// 헤더 스킵
for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) continue;

    const columns = parseCSVLine(line);
    
    if (columns.length >= 9) {
        const code = columns[0];
        const name = columns[2].replace(/'/g, "''"); // SQL 이스케이프
        const market = columns[6].replace(/'/g, "''");
        let sector = columns[8].replace(/'/g, "''");
        
        // 유효성 검사
        if (!code || code.length < 5) {
            skipped++;
            continue;
        }
        
        // NULL 값 처리
        if (!sector || sector === '') {
            sector = 'NULL';
        } else {
            sector = `'${sector}'`;
        }
        
        const sql = `INSERT INTO stockmaster (stockmaster_code, stockmaster_name, stockmaster_market, stockmaster_sector, stockmaster_isactive) VALUES ('${code}', '${name}', '${market}', ${sector}, '1');`;
        sqlStatements.push(sql);
        count++;
    } else {
        skipped++;
    }
}

sqlStatements.push(`\n-- 총 ${count}개의 레코드가 삽입되었습니다.`);
if (skipped > 0) {
    sqlStatements.push(`-- (${skipped}개의 행이 스킵되었습니다)`);
}

// UTF-8로 저장
fs.writeFileSync('insert_stockmaster.sql', sqlStatements.join('\n'), 'utf-8');

console.log(`\n✓ SQL 스크립트 생성 완료!`);
console.log(`✓ 총 ${count}개의 INSERT 문이 생성되었습니다.`);
if (skipped > 0) {
    console.log(`⚠ ${skipped}개의 행이 스킵되었습니다.`);
}
console.log(`✓ 파일명: insert_stockmaster.sql`);
console.log(`✓ 출력 인코딩: UTF-8`);

// 첫 3개 레코드 샘플 출력
console.log('\n[샘플 데이터 - 처음 3개 레코드]');
for (let i = 6; i < Math.min(9, sqlStatements.length); i++) {
    console.log(sqlStatements[i].substring(0, 150) + '...');
}

console.log('\n사용 방법:');
console.log('1. MySQL Workbench를 열고 데이터베이스에 연결합니다');
console.log('2. File > Open SQL Script로 insert_stockmaster.sql을 엽니다');
console.log('3. 상단에서 사용할 데이터베이스를 선택합니다');
console.log('4. ⚡ (Execute) 버튼을 클릭하여 실행합니다');
