package com.nsuslab.domain.user.model;

import com.nsuslab.domain.user.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_refresh_token", columnList = "refreshToken")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    // === 기본 인증 정보 ===
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    // === JWT 토큰 관리 ===
    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

    // === 계정 보안 상태 ===
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    private boolean emailVerified = false;
    private LocalDateTime lastLoginAt;

    @Column(length = 50)
    private String lastLoginIp;

    // === Rate Limiting 관련 ===
    private int failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastFailedLoginAt;

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        this.lastFailedLoginAt = LocalDateTime.now();

        // 5회 실패 시 30분 잠금
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastFailedLoginAt = null;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    public void updateLastLogin(String clientIp) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = clientIp;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }
}
