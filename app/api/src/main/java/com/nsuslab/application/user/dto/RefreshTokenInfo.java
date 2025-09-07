package com.nsuslab.application.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenInfo {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final Long expiresIn;
}