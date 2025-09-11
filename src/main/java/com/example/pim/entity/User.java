package com.example.pim.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column
    private Integer age;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String role = "ROLE_USER"; // 默认角色为普通用户

    @Column
    private Boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // 登录失败次数
    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;
    
    // 账户锁定状态
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    // 账户锁定时间
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    // 邮箱验证状态
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    // 邮箱验证令牌
    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    // 验证令牌过期时间
    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
