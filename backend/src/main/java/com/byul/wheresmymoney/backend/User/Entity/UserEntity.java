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
    @Column(name = "userinfo_idx")
    private long userinfo_idx;

    // ID는 15자 제한.
    @Column(unique = true, nullable = true, length = 15)
    private String userinfo_ID;

    // 비밀번호는 20자 이상.
    @Column(unique = true, nullable = true, length = 20)
    private String userinfo_PW;

    // 이름은 10자 이상.
    @Column(unique = true, nullable = true, length = 10)
    private String userinfo_Name;

    // 이메일은 50자 이상.
    @Column(unique = true, nullable = true, length = 50)
    private String userinfo_Email;

    // Rule은 user는 유저, admin은 관리자.
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserRole userRole;

    // status는 ACTIVE는 활성화 상태, BLOCEKD는 탈퇴 상태.
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserStatus userStatus;

}
