package com.nsuslab.interfaces.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "리프레시 토큰 요청")
@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...", required = true)
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
}