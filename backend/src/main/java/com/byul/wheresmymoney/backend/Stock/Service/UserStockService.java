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
			
			// 기존에 동일한 종목이 있는지 확인 (단축코드로 조회)
			UserStockEntity existing = userStockRepository.findByUserIdAndStockCode(userId, shortCode);
			
			if (existing != null) {
				// 기존 종목이 있으면 수량과 평균단가 업데이트
				log.info("기존 종목 발견: stockIdx={}, 기존 수량={}, 기존 평균가={}", 
					existing.getUserstockIdx(), existing.getUserstockQuantity(), existing.getUserstockAvgprice());
				
				int totalQuantity = existing.getUserstockQuantity() + request.getQuantity();
				double totalValue = existing.getUserstockAvgprice().doubleValue() * existing.getUserstockQuantity() 
						+ averagePrice * request.getQuantity();
				double newAvgPrice = totalValue / totalQuantity;
				
				existing.setUserstockQuantity(totalQuantity);
				existing.setUserstockAvgprice(BigDecimal.valueOf(newAvgPrice));
				userStockRepository.save(existing);
				
				log.info("기존 종목 업데이트 완료: 총 수량={}, 새 평균가={}", totalQuantity, newAvgPrice);
			} else {
				// 신규 종목 추가
				log.info("신규 종목 추가 시작");
				
				UserStockEntity newStock = new UserStockEntity();
				newStock.setUserstockFidx(userId);
				newStock.setUserstockStk(shortCode);  // 6자리 단축코드 저장
				newStock.setUserstockName(request.getStockName());
				newStock.setUserstockQuantity(request.getQuantity());
				newStock.setUserstockAvgprice(BigDecimal.valueOf(averagePrice));
				userStockRepository.save(newStock);
				
				log.info("신규 종목 추가 완료: stockCode={}, name={}, quantity={}, avgPrice={}", 
					shortCode, request.getStockName(), request.getQuantity(), averagePrice);
			}
			
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
}
