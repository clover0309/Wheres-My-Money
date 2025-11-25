package com.byul.wheresmymoney.backend.Stock.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceService {
	
	@Value("${kiwoom.appkey}")
	private String appkey;
	
	@Value("${kiwoom.secretkey}")
	private String secretkey;
	
	private String accessToken = null;
	private long tokenExpireTime = 0;
	
	/**
	 * Access Token을 가져오는 메서드 (캐싱 적용)
	 */
	private String getAccessToken() throws Exception {
		// 토큰이 유효하면 재사용
		if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
			return accessToken;
		}
		
		String host = "https://openapi.kiwoom.com:9443";
		String endpoint = "/oauth2/Approval";
		String urlString = host + endpoint;
		
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
		connection.setDoOutput(true);
		connection.setConnectTimeout(10000); // 연결 타임아웃 10초
		connection.setReadTimeout(10000);    // 읽기 타임아웃 10초
		
		String jsonData = String.format(
			"{\"grant_type\":\"client_credentials\",\"appkey\":\"%s\",\"secretkey\":\"%s\"}",
			appkey,
			secretkey
		);
		
		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = jsonData.getBytes("utf-8");
			os.write(input, 0, input.length);
		}
		
		int responseCode = connection.getResponseCode();
		
		if (responseCode == 200) {
			try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
				String responseBody = scanner.useDelimiter("\\A").next();
				
				log.info("토큰 발급 응답: {}", responseBody);
				
				ObjectMapper objectMapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
				
				accessToken = (String) responseMap.get("access_token");
				
				// expires_in이 null일 경우 기본값 사용 (1시간)
				Integer expiresIn = (Integer) responseMap.get("expires_in");
				if (expiresIn == null) {
					log.warn("expires_in이 null입니다. 기본값 3600초(1시간) 사용");
					expiresIn = 3600;
				}
				
				// 토큰 만료 시간 설정 (현재 시간 + expires_in - 60초 여유)
				tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
				
				log.info("토큰 발급 성공. 만료 시간: {}초", expiresIn);
				return accessToken;
			}
		} else {
			throw new Exception("토큰 발급 실패: HTTP " + responseCode);
		}
	}
	
	/**
	 * 표준코드(ISIN)를 단축코드로 변환
	 * 예: KR7005490008 -> 005490
	 */
	private String extractShortCode(String stockCode) {
		if (stockCode == null) {
			return stockCode;
		}
		
		// 표준코드(12자리): KR7 + 종목코드(6자리) + 체크디지트(3자리)
		if (stockCode.length() == 12 && stockCode.startsWith("KR7")) {
			String shortCode = stockCode.substring(3, 9);
			log.info("표준코드를 단축코드로 변환: {} -> {}", stockCode, shortCode);
			return shortCode;
		}
		
		// 이미 6자리 단축코드인 경우
		if (stockCode.length() == 6) {
			return stockCode;
		}
		
		log.warn("알 수 없는 종목코드 형식: {}", stockCode);
		return stockCode;
	}
	
	/**
	 * 특정 날짜의 주식 종가를 조회하는 메서드
	 * @param stockCode 주식 단축코드 (6자리, 예: "005930") 또는 표준코드 (12자리, 예: "KR7005490008")
	 * @param purchaseDate 매수 날짜 (LocalDate)
	 * @return 해당 날짜의 종가 (BigDecimal), 조회 실패 시 null
	 */
	public BigDecimal getStockPriceByDate(String stockCode, LocalDate purchaseDate) {
		try {
			// 표준코드를 단축코드로 변환
			String shortCode = extractShortCode(stockCode);
			
			String token = getAccessToken();
			
			// 키움 API: 국내주식 기간별 시세 조회
			String host = "https://openapi.kiwoom.com:9443";
			String endpoint = "/0167/v1/quotations/inquire-daily-itemchartprice";
			
			// 날짜 포맷: YYYYMMDD
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			String dateStr = purchaseDate.format(formatter);
			
			// 쿼리 파라미터 구성
			String queryParams = String.format(
				"?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=%s&FID_INPUT_DATE_1=%s&FID_INPUT_DATE_2=%s&FID_PERIOD_DIV_CODE=D&FID_ORG_ADJ_PRC=0",
				shortCode,
				dateStr,  // 시작일
				dateStr   // 종료일 (같은 날짜로 설정하여 해당 날짜만 조회)
			);
			
			String urlString = host + endpoint + queryParams;
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestProperty("authorization", "Bearer " + token);
			connection.setRequestProperty("appkey", appkey);
			connection.setRequestProperty("appsecret", secretkey);
			connection.setRequestProperty("tr_id", "FHKST03010100");
			
			int responseCode = connection.getResponseCode();
			
			if (responseCode == 200) {
				try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
					String responseBody = scanner.useDelimiter("\\A").next();
					
					log.debug("주가 조회 응답: {}", responseBody);
					
					ObjectMapper objectMapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
					
					// 응답 데이터 파싱
					@SuppressWarnings("unchecked")
					Map<String, Object> output2 = (Map<String, Object>) responseMap.get("output2");
					if (output2 != null && output2.containsKey("stck_clpr")) {
						String closePriceStr = (String) output2.get("stck_clpr");
						return new BigDecimal(closePriceStr);
					}
					
					log.warn("주가 데이터를 찾을 수 없습니다. 종목코드: {}, 날짜: {}, 응답: {}", stockCode, dateStr, responseBody);
					return null;
				}
			} else {
				// 에러 응답 본문 읽기
				String errorBody = "";
				try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
					errorBody = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
					// 에러 스트림을 읽을 수 없는 경우 무시
				}
				log.error("주가 조회 API 호출 실패: HTTP {}, 종목코드: {}, 날짜: {}, 에러: {}", responseCode, stockCode, dateStr, errorBody);
				return null;
			}
			
		} catch (Exception e) {
			log.error("주가 조회 중 오류 발생: 종목코드={}, 날짜={}, 오류={}", stockCode, purchaseDate, e.getMessage());
			return null;
		}
	}
	
	/**
	 * 현재 주식 가격을 조회하는 메서드 (실시간)
	 * @param stockCode 주식 단축코드 (6자리) 또는 표준코드 (12자리, 예: "KR7005490008")
	 * @return 현재가 (BigDecimal), 조회 실패 시 null
	 */
	public BigDecimal getCurrentStockPrice(String stockCode) {
		try {
			// 표준코드를 단축코드로 변환
			String shortCode = extractShortCode(stockCode);
			
			String token = getAccessToken();
			
			// 키움 API: 국내주식 현재가 시세 조회
			String host = "https://openapi.kiwoom.com:9443";
			String endpoint = "/0118/v1/quotations/inquire-price";
			
			String queryParams = String.format(
				"?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=%s",
				shortCode
			);
			
			String urlString = host + endpoint + queryParams;
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestProperty("authorization", "Bearer " + token);
			connection.setRequestProperty("appkey", appkey);
			connection.setRequestProperty("appsecret", secretkey);
			connection.setRequestProperty("tr_id", "FHKST01010100");
			
			int responseCode = connection.getResponseCode();
			
			if (responseCode == 200) {
				try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
					String responseBody = scanner.useDelimiter("\\A").next();
					
					ObjectMapper objectMapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
					
					@SuppressWarnings("unchecked")
					Map<String, Object> output = (Map<String, Object>) responseMap.get("output");
					if (output != null && output.containsKey("stck_prpr")) {
						String currentPriceStr = (String) output.get("stck_prpr");
						return new BigDecimal(currentPriceStr);
					}
					
					log.warn("현재 주가 데이터를 찾을 수 없습니다. 종목코드: {}, 응답: {}", stockCode, responseBody);
					return null;
				}
			} else {
				String errorBody = "";
				try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
					errorBody = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
					// 무시
				}
				log.error("현재가 조회 API 호출 실패: HTTP {}, 종목코드: {}, 에러: {}", responseCode, stockCode, errorBody);
				return null;
			}
			
		} catch (Exception e) {
			log.error("현재가 조회 중 오류 발생: 종목코드={}, 오류={}", stockCode, e.getMessage());
			return null;
		}
	}
}
