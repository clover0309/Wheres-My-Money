package com.byul.wheresmymoney.backend.User.Repository.Register;

import com.byul.wheresmymoney.backend.User.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository extends JpaRepository<UserEntity, Long> {
}
