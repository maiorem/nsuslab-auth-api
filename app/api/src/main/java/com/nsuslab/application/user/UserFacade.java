package com.nsuslab.application.user;

import com.nsuslab.application.user.dto.*;
import com.nsuslab.auth.jwt.JwtTokenProvider;
import com.nsuslab.domain.user.model.User;
import com.nsuslab.domain.user.service.UserService;
import com.nsuslab.interfaces.api.user.dto.RefreshTokenRequest;
import com.nsuslab.supports.error.CoreException;
import com.nsuslab.supports.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacade {
    
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public void register(String email, String password) {
        userService.register(email, password);
    }

    public LoginInfo login(String email, String password, String clientIP) {
        User user = userService.authenticateUser(email, password);
        
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        
        userService.updateRefreshToken(user, refreshToken);
        user.updateLastLogin(clientIP);
        
        return new LoginInfo(
            accessToken, 
            refreshToken, 
            "Bearer",
            jwtTokenProvider.getRefreshTokenExpirationMs() / 1000
        );
    }

    public RefreshTokenInfo refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CoreException(ErrorType.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다");
        }
        
        User user = userService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CoreException(ErrorType.TOKEN_NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다"));
        
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        
        userService.updateRefreshToken(user, newRefreshToken);
        
        return new RefreshTokenInfo(
            newAccessToken, 
            newRefreshToken, 
            "Bearer",
            jwtTokenProvider.getRefreshTokenExpirationMs() / 1000
        );
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            userService.findByRefreshToken(refreshToken)
                    .ifPresent(userService::logout);
        }
    }

    public UserInfo getUserProfile(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND, "사용자를 찾을 수 없습니다"));
        
        return new UserInfo(
            user.getId(),
            user.getEmail(),
            user.getStatus(),
            user.isEmailVerified(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}