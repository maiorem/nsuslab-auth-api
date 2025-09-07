package com.nsuslab.application.user.dto;

import com.nsuslab.domain.user.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class UserInfo {
    private final Long id;
    private final String email;
    private final UserStatus status;
    private final boolean emailVerified;
    private final LocalDateTime lastLoginAt;
    private final ZonedDateTime createdAt;
}