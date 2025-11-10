package com.byul.wheresmymoney.backend.User.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserMainController {

    @GetMapping("")
    public String UserLogin() {
        return "여기에 로그인 로직 추가.";
    }

    @GetMapping("/findpw")
    public String UserFindPW() {
        return "여기에 아이디 비밀번호 찾기 로직 추가.";
    }

    @GetMapping("/register")
    public String register() {
        return "여기에 회원가입 로직 추가.";
    }

    @GetMapping("/running")
    public String test() {
        return "구동중.";
    }
}
