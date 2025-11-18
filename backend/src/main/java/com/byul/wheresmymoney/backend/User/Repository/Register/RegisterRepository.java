package com.byul.wheresmymoney.backend.User.Repository.Register;

import org.springframework.data.jpa.repository.JpaRepository;

import com.byul.wheresmymoney.backend.User.Entity.UserEntity;

public interface RegisterRepository extends JpaRepository<UserEntity, Long> {
}
