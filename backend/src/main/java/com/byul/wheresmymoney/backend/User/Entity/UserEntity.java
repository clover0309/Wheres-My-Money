package com.byul.wheresmymoney.backend.User.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table (name = "userinfo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userInfo_idx")
    private Long userInfo_idx;

    // ID는 15자 제한.
    @Column(name = "userInfo_id", unique = true, nullable = false, length = 15)
    private String userinfo_ID;

    // 비밀번호는 BCrypt 암호화를 위해 60자로 제한.
    @Column(name = "userInfo_pw", nullable = false, length = 60)
    private String userinfo_PW;

    // 이름은 10자 제한.
    @Column(name = "userInfo_name", nullable = false, length = 10)
    private String userinfo_Name;

    // 이메일은 50자 제한.
    @Column(name = "userInfo_email", unique = true, nullable = false, length = 50)
    private String userinfo_Email;

    // Rule은 user는 유저, admin은 관리자.
    @Enumerated(EnumType.STRING)
    @Column(name = "userInfo_role", nullable = true)
    private UserRole userRole;

    // status는 ACTIVE는 활성화 상태, BLOCEKD는 탈퇴 상태.
    @Enumerated(EnumType.STRING)
    @Column(name = "userInfo_status", nullable = true)
    private UserStatus userStatus;

}
