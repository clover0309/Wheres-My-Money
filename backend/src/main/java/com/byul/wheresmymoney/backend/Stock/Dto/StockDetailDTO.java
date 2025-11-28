package com.byul.wheresmymoney.backend.Stock.Dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDetailDTO {
	private String stockCode;
	private String stockName;
	private Integer quantity;
	private String purchaseDate;
	private BigDecimal averagePrice;
	private BigDecimal currentPrice;
	private BigDecimal evaluationAmount;
	private BigDecimal profitLoss;
	private Double profitRate;
}
