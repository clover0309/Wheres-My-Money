package com.byul.wheresmymoney.backend.Stock.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StockDTO {
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StockSearchResponse {
		private String code;
		private String name;
		private String market;
		private String sector;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AddStockRequest {
		private String userId;
		private String stockCode;
		private String stockName;
		private Integer quantity;
		private Double averagePrice;
	}
}
