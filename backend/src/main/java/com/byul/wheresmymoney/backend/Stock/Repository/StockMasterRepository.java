package com.byul.wheresmymoney.backend.Stock.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.byul.wheresmymoney.backend.Stock.Entity.StockMasterEntity;

@Repository
public interface StockMasterRepository extends JpaRepository<StockMasterEntity, String> {
	
	@Query("SELECT s FROM StockMasterEntity s WHERE s.stockmasterName LIKE %:keyword% OR s.stockmasterCode LIKE :keyword% ORDER BY s.stockmasterName")
	List<StockMasterEntity> searchByKeyword(@Param("keyword") String keyword);
}
