package com.nsuslab.interfaces.api.user;

import com.nsuslab.application.user.UserFacade;
import com.nsuslab.application.user.dto.LoginInfo;
import com.nsuslab.application.user.dto.RefreshTokenInfo;
import com.nsuslab.application.user.dto.UserInfo;
import com.nsuslab.interfaces.api.common.ApiResponse;
import com.nsuslab.interfaces.api.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserV1Controller implements UserV1ApiSpec {
    
    private final UserFacade userFacade;

    @PostMapping("/register")
    @Override
    public ApiResponse register(@Valid @RequestBody RegisterRequest request) {
        userFacade.register(request.getEmail(), request.getPassword());
        return ApiResponse.success("회원가입이 완료되었습니다");
    }

    @PostMapping("/login")
    @Override
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, 
                                          HttpServletRequest httpRequest) {
        String clientIP = getClientIP(httpRequest);
        LoginInfo info = userFacade.login(request.getEmail(), request.getPassword(), clientIP);
        TokenResponse response = TokenResponse.convertFromLoginInfo(info);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    @Override
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenInfo info = userFacade.refreshToken(request);
        TokenResponse response = TokenResponse.convertFromRefreshInfo(info);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    @Override
    public ApiResponse<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        userFacade.logout(request.getRefreshToken());
        return ApiResponse.success("로그아웃 되었습니다");
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserResponse> getMyProfile(HttpServletRequest request) {
        Long userId = getCurrentUserId();
        UserInfo info = userFacade.getUserProfile(userId);
        UserResponse response = UserResponse.convertFromInfo(info);
        return ApiResponse.success(response);
    }
    
    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}