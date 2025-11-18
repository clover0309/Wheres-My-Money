package com.byul.wheresmymoney.backend.User.Service.Login;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.byul.wheresmymoney.backend.User.Entity.UserEntity;
import com.byul.wheresmymoney.backend.User.Repository.Login.LoginRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LoginService {
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity login(String id, String password) {
        UserEntity user = loginRepository.findByUserinfo_ID(id);
        
        if (user != null && passwordEncoder.matches(password, user.getUserinfo_PW())) {
            return user;
        }
        
        return null;
    }
}
