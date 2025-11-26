package com.byul.wheresmymoney.backend.Stock.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
		
		String host = "https://api.kiwoom.com";
		String endpoint = "/oauth2/token";
		String urlString = host + endpoint;
		
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
		connection.setDoOutput(true);
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		
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
			
			// 키움 API는 "token" 필드 사용 (access_token 아님)
			accessToken = (String) responseMap.get("token");
			
			// expires_in이 null일 경우 기본값 사용 (1시간)
			Integer expiresIn = (Integer) responseMap.get("expires_in");
			if (expiresIn == null) {
				log.warn("expires_in이 null입니다. 기본값 3600초(1시간) 사용");
				expiresIn = 3600;
			}				// 토큰 만료 시간 설정 (현재 시간 + expires_in - 60초 여유)
				tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
				
				log.info("토큰 발급 성공. 만료 시간: {}초", expiresIn);
				return accessToken;
			}
		} else {
			// 에러 응답 본문 읽기
			String errorBody = "";
			try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
				errorBody = scanner.useDelimiter("\\A").next();
			} catch (Exception e) {
				// 에러 스트림을 읽을 수 없는 경우 무시
			}
			log.error("토큰 발급 실패: HTTP {}, 에러 응답: {}", responseCode, errorBody);
			throw new Exception("토큰 발급 실패: HTTP " + responseCode + ", 응답: " + errorBody);
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
			log.info("주가 조회 시작: stockCode={}, purchaseDate={}", stockCode, purchaseDate);
			
			// 표준코드를 단축코드로 변환
			String shortCode = extractShortCode(stockCode);
			log.info("단축코드 변환 완료: {} -> {}", stockCode, shortCode);
			
			String token = getAccessToken();
			log.info("액세스 토큰 발급 완료");
			
		// 키움 API: 일별주가요청 (ka10086)
		String host = "https://api.kiwoom.com";
		String endpoint = "/api/dostk/mrkcond";
		
		// 날짜 포맷: YYYYMMDD
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String dateStr = purchaseDate.format(formatter);
		log.info("날짜 포맷 변환: {} -> {}", purchaseDate, dateStr);
		
		// 키움 API는 Body로 파라미터 전송 (JSON 형식)
		// 종목코드는 KRX: 접두사 없이 6자리만 전송
		String jsonBody = String.format(
			"{\"stk_cd\":\"%s\",\"qry_dt\":\"%s\",\"indc_tp\":\"0\"}",
			shortCode,
			dateStr
		);
		
		log.info("API 호출 URL: {}, Body: {}", host + endpoint, jsonBody);
		
		URL url = new URL(host + endpoint);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
		connection.setRequestProperty("authorization", "Bearer " + token);
		connection.setRequestProperty("cont-yn", "N");
		connection.setRequestProperty("next-key", "");
		connection.setRequestProperty("api-id", "ka10086");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		
		// Body 데이터 전송
		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = jsonBody.getBytes("utf-8");
			os.write(input, 0, input.length);
		}			int responseCode = connection.getResponseCode();
			log.info("API 응답 코드: {}", responseCode);
			
			if (responseCode == 200) {
				try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
					String responseBody = scanner.useDelimiter("\\A").next();
					
					log.info("주가 조회 응답: {}", responseBody);
					
					ObjectMapper objectMapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
					
					log.info("API 응답 파싱 완료: {}", responseMap);
					
					// daly_stkpc 배열에서 첫 번째 항목의 종가(close_pric) 추출
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> dalyStkpc = (List<Map<String, Object>>) responseMap.get("daly_stkpc");
					
				if (dalyStkpc != null && !dalyStkpc.isEmpty()) {
					Map<String, Object> priceData = dalyStkpc.get(0);
					String closePriceStr = (String) priceData.get("close_pric");
					
					if (closePriceStr != null && !closePriceStr.isEmpty()) {
						// +/- 부호 제거 (키움 API는 전일 대비 부호 포함)
						closePriceStr = closePriceStr.replace("+", "").replace("-", "");
						BigDecimal closePrice = new BigDecimal(closePriceStr);
						log.info("종가 조회 성공: 종목={}, 날짜={}, 종가={}", shortCode, dateStr, closePrice);
						return closePrice;
					} else {
						log.warn("종가 데이터가 비어있음: 종목={}, 날짜={}", shortCode, dateStr);
					}
				} else {
					log.warn("daly_stkpc 배열이 비어있음: 종목={}, 날짜={}", shortCode, dateStr);
				}					log.warn("주가 데이터를 찾을 수 없습니다. 종목코드: {}, 날짜: {}, 응답: {}", stockCode, dateStr, responseBody);
					return null;
				}
			} else {
				// 에러 응답 본문 읽기
				String errorBody = "";
				try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
					errorBody = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
					// 에러 스트림을 읽을 수 없는 경우 무시
					log.debug("에러 스트림 읽기 실패", e);
				}
				log.error("주가 조회 API 호출 실패: HTTP {}, 종목코드: {}, 날짜: {}, 에러: {}", responseCode, stockCode, dateStr, errorBody);
				return null;
			}
			
		} catch (Exception e) {
			log.error("주가 조회 중 오류 발생: 종목코드={}, 날짜={}, 오류={}", stockCode, purchaseDate, e.getMessage(), e);
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
			
			// 키움 API: 주식호가요청 (ka10004) - 현재가 포함
			// 실전투자 URL
			String host = "https://api.kiwoom.com";
			String endpoint = "/tr/market/quot";
			
			String jsonBody = String.format(
				"{\"stk_cd\":\"KRX:%s\"}",
				shortCode
			);
			
			URL url = new URL(host + endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestProperty("authorization", "Bearer " + token);
			connection.setRequestProperty("api-id", "ka10004");
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			
			// Body 데이터 전송
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonBody.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			
			int responseCode = connection.getResponseCode();
			
			if (responseCode == 200) {
				try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
					String responseBody = scanner.useDelimiter("\\A").next();
					
					ObjectMapper objectMapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
					
					// 키움 API 응답 구조에 따라 현재가 추출 (API 문서 확인 필요)
					@SuppressWarnings("unchecked")
					Map<String, Object> output = (Map<String, Object>) responseMap.get("output");
					if (output != null && output.containsKey("curr_pric")) {
						String currentPriceStr = (String) output.get("curr_pric");
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
