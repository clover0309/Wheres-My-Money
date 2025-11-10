package com.byul.wheresmymoney.backend.User.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "useractivity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userActivity_idx")
    private int userActivity_idx;

    // 보유주식 추가, 보유주식 삭제, 보유현물 추가, 보유현물 삭제, 패스워드변경, 로그인 기록, 로그아웃 기록.
    @Enumerated(EnumType.STRING)
    @Column(name = "userActivity_type", nullable = false)
    private ActivityType userActivity_type;
}
