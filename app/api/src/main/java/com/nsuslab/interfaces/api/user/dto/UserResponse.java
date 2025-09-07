package com.nsuslab.interfaces.api.user.dto;

import com.nsuslab.application.user.dto.UserInfo;
import com.nsuslab.domain.user.model.User;
import com.nsuslab.domain.user.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Schema(description = "사용자 정보 응답")
@Getter
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private final Long id;
    
    @Schema(description = "이메일 주소", example = "user@example.com")
    private final String email;
    
    @Schema(description = "사용자 상태", example = "ACTIVE")
    private final UserStatus status;
    
    @Schema(description = "이메일 인증 여부", example = "true")
    private final boolean emailVerified;
    
    @Schema(description = "마지막 로그인 시간", example = "2023-12-01T10:00:00")
    private final LocalDateTime lastLoginAt;
    
    @Schema(description = "계정 생성 시간", example = "2023-12-01T09:00:00")
    private final ZonedDateTime createdAt;
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.emailVerified = user.isEmailVerified();
        this.lastLoginAt = user.getLastLoginAt();
        this.createdAt = user.getCreatedAt();
    }

    public UserResponse(Long id, String email, UserStatus status, boolean emailVerified, 
                       LocalDateTime lastLoginAt, ZonedDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.emailVerified = emailVerified;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    public static UserResponse convertFromInfo(UserInfo userInfo) {
        return new UserResponse(
                userInfo.getId(),
                userInfo.getEmail(),
                userInfo.getStatus(),
                userInfo.isEmailVerified(),
                userInfo.getLastLoginAt(),
                userInfo.getCreatedAt()
        );
    }
}