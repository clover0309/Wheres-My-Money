package com.byul.wheresmymoney.backend.Stock.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "userstock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStockEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userstock_idx")
	private Integer userstockIdx;
	
	@Column(name = "userstock_fidx", length = 50, nullable = false)
	private String userstockFidx;
	
	@Column(name = "userstock_stk", length = 6, nullable = false)
	private String userstockStk;
	
	@Column(name = "userstock_name", length = 100, nullable = false)
	private String userstockName;
	
	@Column(name = "userstock_quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
	private Integer userstockQuantity = 0;
	
	@Column(name = "userstock_avgprice", nullable = false, columnDefinition = "DECIMAL(15, 2) DEFAULT 0.00")
	private BigDecimal userstockAvgprice = BigDecimal.ZERO;
	
	@Column(name = "userstock_purchasedate", nullable = false)
	private LocalDate userstockPurchasedate;
	
	@Column(name = "userstock_createdat", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime userstockCreatedat = LocalDateTime.now();
	
	@Column(name = "userstock_updatedat", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime userstockUpdatedat = LocalDateTime.now();
}
