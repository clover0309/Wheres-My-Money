package com.byul.wheresmymoney.backend.User.Entity;

import jakarta.persistence.*;
import lombok.*;


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
    private int userinfo_idx;

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
