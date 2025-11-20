package com.byul.wheresmymoney.backend.Stock.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.byul.wheresmymoney.backend.Stock.Dto.StockDTO.StockSearchResponse;
import com.byul.wheresmymoney.backend.Stock.Entity.StockMasterEntity;
import com.byul.wheresmymoney.backend.Stock.Repository.StockMasterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockSearchService {
	
	private final StockMasterRepository stockMasterRepository;
	
	public List<StockSearchResponse> searchStocks(String keyword) {
		List<StockMasterEntity> stocks = stockMasterRepository.searchByKeyword(keyword);
		
		return stocks.stream()
			.limit(10)
			.map(stock -> new StockSearchResponse(
				stock.getStockmasterCode(),
				stock.getStockmasterName(),
				stock.getStockmasterMarket(),
				stock.getStockmasterSector()
			))
			.collect(Collectors.toList());
	}
}
