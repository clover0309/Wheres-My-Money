package com.byul.wheresmymoney.backend.User.Repository.Login;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.byul.wheresmymoney.backend.User.Entity.UserEntity;

@Repository
public interface LogoutRepository extends JpaRepository<UserEntity, Long> {
    
}
