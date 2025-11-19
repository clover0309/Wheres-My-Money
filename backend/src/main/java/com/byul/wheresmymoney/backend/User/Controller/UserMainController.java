package com.byul.wheresmymoney.backend.User.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.byul.wheresmymoney.backend.User.Dto.ApiResponse;
import com.byul.wheresmymoney.backend.User.Dto.LoginRequest;
import com.byul.wheresmymoney.backend.User.Dto.RegisterRequest;
import com.byul.wheresmymoney.backend.User.Entity.UserEntity;
import com.byul.wheresmymoney.backend.User.Service.Login.LoginService;
import com.byul.wheresmymoney.backend.User.Service.Register.RegisterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserMainController {

    private final RegisterService registerService;
    private final LoginService loginService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            UserEntity user = registerService.Register(
                request.getId(),
                request.getPassword(),
                request.getName(),
                request.getEmail()
            );
            return ResponseEntity.ok(new ApiResponse(true, "회원가입이 완료되었습니다.", user.getUserinfo_ID()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "회원가입 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/check-id")
    public ResponseEntity<ApiResponse> checkId(@RequestParam String id) {
        try {
            UserEntity existingUser = loginService.findById(id);
            System.out.println("아이디 조회: " + id + ", 결과: " + (existingUser != null ? "존재함" : "없음"));
            
            if (existingUser != null) {
                return ResponseEntity.ok(new ApiResponse(false, "이미 사용 중인 아이디입니다."));
            } else {
                return ResponseEntity.ok(new ApiResponse(true, "사용 가능한 아이디입니다."));
            }
        } catch (Exception e) {
            System.out.println("아이디 확인 중 에러: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "아이디 확인 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            UserEntity user = loginService.login(request.getId(), request.getPassword());
            if (user != null) {
                return ResponseEntity.ok(new ApiResponse(true, "로그인 성공", user.getUserinfo_ID()));
            } else {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "아이디 또는 비밀번호가 일치하지 않습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "로그인 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        try {
            // 추후 JWT로 구현.
            return ResponseEntity.ok(new ApiResponse(true, "로그아웃 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "로그아웃 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/findpw")
    public String UserFindPW() {
        return "여기에 아이디 비밀번호 찾기 로직 추가.";
    }

    @GetMapping("/running")
    public String test() {
        return "구동중.";
    }
}
