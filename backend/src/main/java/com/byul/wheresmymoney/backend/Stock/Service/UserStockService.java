package com.byul.wheresmymoney.backend.Stock.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.byul.wheresmymoney.backend.Stock.Dto.StockDTO.AddStockRequest;
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
			// 표준코드를 단축코드로 변환 (API 호출용)
			String shortCode = extractShortCode(request.getStockCode());
			log.info("API 호출용 단축코드: {}", shortCode);
			
			// DB 저장은 원본 표준코드 사용 (Foreign Key 매칭)
			String codeForDB = request.getStockCode();
			log.info("DB 저장용 종목코드: {}", codeForDB);
			
			// 매수가가 없고 매수 날짜가 있으면 API에서 가격 조회
			Double averagePrice = request.getAveragePrice();
			if ((averagePrice == null || averagePrice == 0) && request.getPurchaseDate() != null) {
				log.info("매수 날짜로 주가 조회 시작: 종목={}, 날짜={}", request.getStockCode(), request.getPurchaseDate());
				
				try {
					BigDecimal fetchedPrice = stockPriceService.getStockPriceByDate(request.getStockCode(), request.getPurchaseDate());
					
					if (fetchedPrice != null) {
						averagePrice = fetchedPrice.doubleValue();
						log.info("주가 조회 성공: 종목={}, 가격={}", request.getStockCode(), averagePrice);
					} else {
						log.warn("주가 조회 실패: 종목={}, 날짜={} - API 응답 없음", request.getStockCode(), request.getPurchaseDate());
						return false;
					}
				} catch (Exception e) {
					log.error("주가 조회 API 호출 실패: 종목={}, 날짜={}, 오류={}", request.getStockCode(), request.getPurchaseDate(), e.getMessage());
					return false;
				}
			}
			
			// averagePrice가 여전히 null이면 실패
			if (averagePrice == null) {
				log.error("매수가를 확인할 수 없습니다: 종목={}", request.getStockCode());
				return false;
			}
			
			// 기존에 동일한 종목이 있는지 확인 (표준코드로 조회)
			UserStockEntity existing = userStockRepository.findByUserIdAndStockCode(userId, codeForDB);
			
			if (existing != null) {
				// 기존 종목이 있으면 수량과 평균단가 업데이트
				int totalQuantity = existing.getUserstockQuantity() + request.getQuantity();
				double totalValue = existing.getUserstockAvgprice().doubleValue() * existing.getUserstockQuantity() 
						+ averagePrice * request.getQuantity();
				double newAvgPrice = totalValue / totalQuantity;
				
				existing.setUserstockQuantity(totalQuantity);
				existing.setUserstockAvgprice(BigDecimal.valueOf(newAvgPrice));
				userStockRepository.save(existing);
			} else {
				// 신규 종목 추가
				UserStockEntity newStock = new UserStockEntity();
				newStock.setUserstockFidx(userId);
				newStock.setUserstockStk(codeForDB);  // 표준코드 그대로 저장 (Foreign Key 매칭)
				newStock.setUserstockName(request.getStockName());
				newStock.setUserstockQuantity(request.getQuantity());
				newStock.setUserstockAvgprice(BigDecimal.valueOf(averagePrice));
				userStockRepository.save(newStock);
			}
			
			return true;
		} catch (Exception e) {
			log.error("주식 추가 중 오류 발생: userId={}, stockCode={}, error={}", userId, request.getStockCode(), e.getMessage());
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
}
