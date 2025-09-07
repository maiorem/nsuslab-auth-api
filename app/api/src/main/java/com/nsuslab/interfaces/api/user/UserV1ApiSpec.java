package com.nsuslab.interfaces.api.user;

import com.nsuslab.interfaces.api.common.ApiResponse;
import com.nsuslab.interfaces.api.user.dto.*;
import com.nsuslab.supports.ratelimit.RateLimit;
import com.nsuslab.supports.ratelimit.RateLimitType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "인증", description = "사용자 인증 관련 API")
public interface UserV1ApiSpec {

    @Operation(summary = "회원가입", description = "이메일과 비밀번호를 사용하여 새 계정을 생성합니다.")
    @RateLimit(key = "register", time = 300, count = 3, limitType = RateLimitType.IP,
            message = "회원가입 요청이 너무 많습니다. 5분 후 다시 시도해주세요.")
    ApiResponse register(
            @Parameter(description = "회원가입 정보", required = true)
            @Valid @RequestBody RegisterRequest request);


    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @RateLimit(key = "login", time = 300, count = 5, limitType = RateLimitType.IP,
            message = "로그인 시도가 너무 많습니다. 5분 후 다시 시도해주세요.")
    ApiResponse<TokenResponse> login(
            @Parameter(description = "로그인 정보", required = true)
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest);

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @RateLimit(key = "refresh", time = 60, count = 10, limitType = RateLimitType.IP,
            message = "토큰 갱신 요청이 너무 많습니다. 1분 후 다시 시도해주세요.")
    ApiResponse<TokenResponse> refresh(
            @Parameter(description = "토큰 갱신 정보", required = true)
            @Valid @RequestBody RefreshTokenRequest request);


    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃합니다.")
    @RateLimit(key = "logout", time = 60, count = 5, limitType = RateLimitType.IP,
            message = "로그아웃 요청이 너무 많습니다. 1분 후 다시 시도해주세요.")
    ApiResponse<String> logout(
            @Parameter(description = "로그아웃할 리프레시 토큰", required = true)
            @Valid @RequestBody RefreshTokenRequest request);



    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @RateLimit(key = "profile", time = 60, count = 30, limitType = RateLimitType.USER,
            message = "회원정보 조회 요청이 너무 많습니다. 1분 후 다시 시도해주세요.")
    ApiResponse<UserResponse> getMyProfile(HttpServletRequest request);


}
