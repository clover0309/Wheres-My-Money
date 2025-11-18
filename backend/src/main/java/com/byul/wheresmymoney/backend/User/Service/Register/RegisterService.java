package com.byul.wheresmymoney.backend.User.Service.Register;

import org.springframework.stereotype.Service;

import com.byul.wheresmymoney.backend.User.Entity.UserEntity;
import com.byul.wheresmymoney.backend.User.Entity.UserRole;
import com.byul.wheresmymoney.backend.User.Entity.UserStatus;
import com.byul.wheresmymoney.backend.User.Repository.Register.RegisterRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RegisterService {
    private final RegisterRepository registerRepository;

    public UserEntity Register(String id, String pw, String name, String email) {
        // reg 객체 생성후 db에 추가할 값들 파라미터로 받아옴. 혹시몰라 순서를 맞춰놓음.
        UserEntity reg = new UserEntity();
        reg.setUserinfo_ID(id);
        reg.setUserinfo_PW(pw);
        reg.setUserinfo_Name(name);
        reg.setUserinfo_Email(email);
        reg.setUserRole(UserRole.user);
        reg.setUserStatus(UserStatus.ACTIVE);

        //회원가입 로직 완료.
        this.registerRepository.save(reg);

        return reg;
    }

}
