package com.byul.wheresmymoney.backend.Stock.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.byul.wheresmymoney.backend.Stock.Dto.StockDTO.AddStockRequest;
import com.byul.wheresmymoney.backend.Stock.Dto.StockDetailDTO;
import com.byul.wheresmymoney.backend.Stock.Dto.UserStockDTO;
import com.byul.wheresmymoney.backend.Stock.Entity.UserStockEntity;
import com.byul.wheresmymoney.backend.Stock.Repository.UserStockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStockService {
	
	private final UserStockRepository userStockRepository;
	private final StockPriceService stockPriceService;
	
	/**
	 * 표준코드(ISIN)를 단축코드로 변환
	 * 예: KR7005490008 -> 005490
	 * 
	 * 현재는 검색 결과가 이미 6자리 단축코드를 반환하므로
	 * 이 함수는 예외적인 경우를 처리하기 위한 것입니다.
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
	
	public boolean addStock(String userId, AddStockRequest request) {
		try {
			log.info("주식 추가 시작: userId={}, stockCode={}, quantity={}, averagePrice={}, purchaseDate={}", 
				userId, request.getStockCode(), request.getQuantity(), request.getAveragePrice(), request.getPurchaseDate());
			
			// 표준코드를 단축코드로 변환 (일반적으로 이미 6자리 코드가 들어옴)
			String shortCode = extractShortCode(request.getStockCode());
			log.info("사용할 종목코드: {}", shortCode);
			
			// 매수가가 없고 매수 날짜가 있으면 API에서 가격 조회
			Double averagePrice = request.getAveragePrice();
			if ((averagePrice == null || averagePrice == 0) && request.getPurchaseDate() != null) {
				log.info("매수 날짜로 주가 조회 시작: 종목={}, 날짜={}", shortCode, request.getPurchaseDate());
				
				try {
					// API 호출 시에도 단축코드 사용
					BigDecimal fetchedPrice = stockPriceService.getStockPriceByDate(shortCode, request.getPurchaseDate());
					
					if (fetchedPrice != null) {
						averagePrice = fetchedPrice.doubleValue();
						log.info("주가 조회 성공: 종목={}, 가격={}", shortCode, averagePrice);
					} else {
						log.warn("주가 조회 실패: 종목={}, 날짜={} - API 응답 없음", shortCode, request.getPurchaseDate());
						return false;
					}
				} catch (Exception e) {
					log.error("주가 조회 API 호출 실패: 종목={}, 날짜={}, 오류={}", shortCode, request.getPurchaseDate(), e.getMessage(), e);
					return false;
				}
			}
			
			// averagePrice가 여전히 null이면 실패
			if (averagePrice == null || averagePrice <= 0) {
				log.error("매수가를 확인할 수 없습니다: 종목={}, averagePrice={}", shortCode, averagePrice);
				return false;
			}
			
			// 매수일 결정
			LocalDate purchaseDate = request.getPurchaseDate();
			if (purchaseDate == null) {
				purchaseDate = LocalDate.now();  // 날짜가 없으면 오늘 날짜
			}
			
			// 신규 종목 추가 (같은 종목이라도 매수 날짜가 다르면 별도로 저장)
			log.info("신규 매수 내역 추가 시작: 종목={}, 날짜={}", shortCode, purchaseDate);
			
			UserStockEntity newStock = new UserStockEntity();
			newStock.setUserstockFidx(userId);
			newStock.setUserstockStk(shortCode);  // 6자리 단축코드 저장
			newStock.setUserstockName(request.getStockName());
			newStock.setUserstockQuantity(request.getQuantity());
			newStock.setUserstockAvgprice(BigDecimal.valueOf(averagePrice));
			newStock.setUserstockPurchasedate(purchaseDate);
			
			userStockRepository.save(newStock);
			
			log.info("신규 종목 추가 완료: stockCode={}, name={}, quantity={}, avgPrice={}, purchaseDate={}", 
				shortCode, request.getStockName(), request.getQuantity(), averagePrice, purchaseDate);
			return true;
		} catch (Exception e) {
			log.error("주식 추가 중 오류 발생: userId={}, stockCode={}, error={}", userId, request.getStockCode(), e.getMessage(), e);
			return false;
		}
	}
	
	public List<UserStockDTO> getUserStocks(String userId) {
		List<UserStockEntity> stocks = userStockRepository.findByUserId(userId);
		
		return stocks.stream()
			.map(stock -> new UserStockDTO(
				stock.getUserstockIdx(),
				stock.getUserstockStk(),
				stock.getUserstockName(),
				stock.getUserstockQuantity(),
				stock.getUserstockAvgprice(),
				stock.getUserstockCreatedat(),
				stock.getUserstockUpdatedat()
			))
			.collect(Collectors.toList());
	}
	
	public boolean deleteStock(Integer stockIdx, String userId) {
		try {
			if (stockIdx == null) {
				return false;
			}
			
			// 주식이 존재하고 해당 사용자의 것인지 확인
			UserStockEntity stock = userStockRepository.findById(stockIdx).orElse(null);
			
			if (stock == null) {
				return false;
			}
			
			// 사용자 확인
			if (!stock.getUserstockFidx().equals(userId)) {
				return false;
			}
			
			userStockRepository.deleteById(stockIdx);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 주식 상세 정보 조회 (수익률 포함)
	 * @param userId 사용자 ID
	 * @param stockCode 종목코드 (6자리)
	 * @return 주식 상세 정보 DTO
	 */
	public StockDetailDTO getStockDetail(String userId, String stockCode) {
		try {
			log.info("주식 상세 정보 조회: userId={}, stockCode={}", userId, stockCode);
			
			// 6자리 단축코드로 변환
			String shortCode = extractShortCode(stockCode);
			
			// 사용자의 해당 종목 조회
			UserStockEntity stock = userStockRepository.findByUserstockFidxAndUserstockStk(userId, shortCode);
			
			if (stock == null) {
				log.warn("보유 주식을 찾을 수 없음: userId={}, stockCode={}", userId, shortCode);
				return null;
			}
			
			// 현재가 조회
			BigDecimal currentPrice = stockPriceService.getCurrentStockPrice(shortCode);
			
			if (currentPrice == null) {
				log.warn("현재가 조회 실패: stockCode={}", shortCode);
				return null;
			}
			
			log.info("현재가 조회 성공: stockCode={}, currentPrice={}", shortCode, currentPrice);
			
			// 수익률 계산
			BigDecimal averagePrice = stock.getUserstockAvgprice();
			Integer quantity = stock.getUserstockQuantity();
			
			// 평가금액 = 현재가 × 보유수량
			BigDecimal evaluationAmount = currentPrice.multiply(BigDecimal.valueOf(quantity));
			
			// 평가손익 = (현재가 - 평균매수가) × 보유수량
			BigDecimal profitLoss = currentPrice.subtract(averagePrice).multiply(BigDecimal.valueOf(quantity));
			
			// 수익률 = ((현재가 - 평균매수가) / 평균매수가) × 100
			Double profitRate = 0.0;
			if (averagePrice.compareTo(BigDecimal.ZERO) > 0) {
				profitRate = currentPrice.subtract(averagePrice)
					.divide(averagePrice, 4, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100))
					.doubleValue();
			}
			
		// 매수일 포맷팅
		String purchaseDate = null;
		if (stock.getUserstockPurchasedate() != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			purchaseDate = stock.getUserstockPurchasedate().format(formatter);
		} else {
			// purchaseDate가 없으면 createdat 사용 (하위 호환성)
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			purchaseDate = stock.getUserstockCreatedat().format(formatter);
		}			StockDetailDTO detail = new StockDetailDTO();
			detail.setStockCode(shortCode);
			detail.setStockName(stock.getUserstockName());
			detail.setQuantity(quantity);
			detail.setPurchaseDate(purchaseDate);
			detail.setAveragePrice(averagePrice);
			detail.setCurrentPrice(currentPrice);
			detail.setEvaluationAmount(evaluationAmount);
			detail.setProfitLoss(profitLoss);
			detail.setProfitRate(profitRate);
			
			log.info("주식 상세 정보 조회 완료: 수익률={}%", profitRate);
			
			return detail;
		} catch (Exception e) {
			log.error("주식 상세 정보 조회 중 오류: userId={}, stockCode={}, 오류={}", userId, stockCode, e.getMessage(), e);
			return null;
		}
	}
	
	public List<StockDetailDTO> getStockDetailList(String userId, String stockCode) {
		try {
			// 단축코드 변환
			String shortCode = stockCode.length() > 6 ? stockCode.substring(1) : stockCode;
			
			// 해당 종목의 모든 매수내역 조회 (날짜별로 분리됨)
			List<UserStockEntity> stocks = userStockRepository.findAllByUserIdAndStockCode(userId, shortCode);
			
			if (stocks == null || stocks.isEmpty()) {
				log.warn("보유 주식을 찾을 수 없음: userId={}, stockCode={}", userId, shortCode);
				return null;
			}
			
			// 현재가 조회 (한 번만 조회)
			BigDecimal currentPrice = stockPriceService.getCurrentStockPrice(shortCode);
			
			if (currentPrice == null) {
				log.warn("현재가 조회 실패: stockCode={}", shortCode);
				return null;
			}
			
			log.info("현재가 조회 성공: stockCode={}, currentPrice={}", shortCode, currentPrice);
			
			// 각 매수내역에 대해 StockDetailDTO 생성
			List<StockDetailDTO> detailList = new ArrayList<>();
			
			for (UserStockEntity stock : stocks) {
				// 수익률 계산
				BigDecimal averagePrice = stock.getUserstockAvgprice();
				Integer quantity = stock.getUserstockQuantity();
				
				// 평가금액 = 현재가 × 보유수량
				BigDecimal evaluationAmount = currentPrice.multiply(BigDecimal.valueOf(quantity));
				
				// 평가손익 = (현재가 - 평균매수가) × 보유수량
				BigDecimal profitLoss = currentPrice.subtract(averagePrice).multiply(BigDecimal.valueOf(quantity));
				
				// 수익률 = ((현재가 - 평균매수가) / 평균매수가) × 100
				Double profitRate = 0.0;
				if (averagePrice.compareTo(BigDecimal.ZERO) > 0) {
					profitRate = currentPrice.subtract(averagePrice)
						.divide(averagePrice, 4, RoundingMode.HALF_UP)
						.multiply(BigDecimal.valueOf(100))
						.doubleValue();
				}
				
				// 매수일 포맷팅
				String purchaseDate = null;
				if (stock.getUserstockPurchasedate() != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					purchaseDate = stock.getUserstockPurchasedate().format(formatter);
				} else {
					// purchaseDate가 없으면 createdat 사용 (하위 호환성)
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					purchaseDate = stock.getUserstockCreatedat().format(formatter);
				}
				
				StockDetailDTO detail = new StockDetailDTO();
				detail.setStockCode(shortCode);
				detail.setStockName(stock.getUserstockName());
				detail.setQuantity(quantity);
				detail.setPurchaseDate(purchaseDate);
				detail.setAveragePrice(averagePrice);
				detail.setCurrentPrice(currentPrice);
				detail.setEvaluationAmount(evaluationAmount);
				detail.setProfitLoss(profitLoss);
				detail.setProfitRate(profitRate);
				
				detailList.add(detail);
			}
			
			log.info("주식 상세 정보 리스트 조회 완료: 총 {}건", detailList.size());
			
			return detailList;
		} catch (Exception e) {
			log.error("주식 상세 정보 리스트 조회 중 오류: userId={}, stockCode={}, 오류={}", userId, stockCode, e.getMessage(), e);
			return null;
		}
	}
}
