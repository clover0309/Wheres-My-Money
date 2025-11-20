package com.byul.wheresmymoney.backend.Stock.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stockmaster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterEntity {
	
	@Id
	@Column(name = "stockmaster_code", length = 6)
	private String stockmasterCode;
	
	@Column(name = "stockmaster_name", length = 100, nullable = false)
	private String stockmasterName;
	
	@Column(name = "stockmaster_market", length = 10)
	private String stockmasterMarket;
	
	@Column(name = "stockmaster_sector", length = 50)
	private String stockmasterSector;
	
	@Column(name = "stockmaster_isactive", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
	private Boolean stockmasterIsactive = true;
	
	@Column(name = "stockmaster_updatedat", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime stockmasterUpdatedat = LocalDateTime.now();
}
