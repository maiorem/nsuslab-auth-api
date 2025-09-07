package com.nsuslab.interfaces.api.user.dto;

import com.nsuslab.application.user.dto.LoginInfo;
import com.nsuslab.application.user.dto.RefreshTokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "토큰 응답")
@Getter
@AllArgsConstructor
public class TokenResponse {
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "만료 시간 (초)", example = "900")
    private Long expiresIn;

    public TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public static TokenResponse convertFromLoginInfo(LoginInfo loginInfo) {
        return new TokenResponse(
                loginInfo.getAccessToken(),
                loginInfo.getRefreshToken(),
                loginInfo.getTokenType(),
                loginInfo.getExpiresIn()
        );
    }

    public static TokenResponse convertFromRefreshInfo(RefreshTokenInfo refreshInfo) {
        return new TokenResponse(
                refreshInfo.getAccessToken(),
                refreshInfo.getRefreshToken(),
                refreshInfo.getTokenType(),
                refreshInfo.getExpiresIn()
        );
    }
}