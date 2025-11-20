package com.byul.wheresmymoney.backend.Stock.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.byul.wheresmymoney.backend.Stock.Entity.UserStockEntity;

@Repository
public interface UserStockRepository extends JpaRepository<UserStockEntity, Integer> {
	
	@Query("SELECT u FROM UserStockEntity u WHERE u.userstockFidx = :userId")
	List<UserStockEntity> findByUserId(@Param("userId") String userId);
	
	@Query("SELECT u FROM UserStockEntity u WHERE u.userstockFidx = :userId AND u.userstockStk = :stockCode")
	UserStockEntity findByUserIdAndStockCode(@Param("userId") String userId, @Param("stockCode") String stockCode);
}
