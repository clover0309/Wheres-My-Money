package com.byul.wheresmymoney.backend.Stock.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStockDTO {
	private Integer userstockIdx;
	private String userstockStk;
	private String userstockName;
	private Integer userstockQuantity;
	private BigDecimal userstockAvgprice;
	private LocalDateTime userstockCreatedat;
	private LocalDateTime userstockUpdatedat;
}
