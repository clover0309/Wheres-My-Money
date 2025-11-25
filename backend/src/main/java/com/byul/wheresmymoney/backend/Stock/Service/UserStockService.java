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

@Service
@RequiredArgsConstructor
public class UserStockService {
	
	private final UserStockRepository userStockRepository;
	
	public boolean addStock(String userId, AddStockRequest request) {
		try {
			// 기존에 동일한 종목이 있는지 확인
			UserStockEntity existing = userStockRepository.findByUserIdAndStockCode(userId, request.getStockCode());
			
			if (existing != null) {
				// 기존 종목이 있으면 수량과 평균단가 업데이트
				int totalQuantity = existing.getUserstockQuantity() + request.getQuantity();
				double totalValue = existing.getUserstockAvgprice().doubleValue() * existing.getUserstockQuantity() 
						+ request.getAveragePrice() * request.getQuantity();
				double newAvgPrice = totalValue / totalQuantity;
				
				existing.setUserstockQuantity(totalQuantity);
				existing.setUserstockAvgprice(BigDecimal.valueOf(newAvgPrice));
				userStockRepository.save(existing);
			} else {
				// 신규 종목 추가
				UserStockEntity newStock = new UserStockEntity();
				newStock.setUserstockFidx(userId);
				newStock.setUserstockStk(request.getStockCode());
				newStock.setUserstockName(request.getStockName());
				newStock.setUserstockQuantity(request.getQuantity());
				newStock.setUserstockAvgprice(BigDecimal.valueOf(request.getAveragePrice()));
				userStockRepository.save(newStock);
			}
			
			return true;
		} catch (Exception e) {
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
