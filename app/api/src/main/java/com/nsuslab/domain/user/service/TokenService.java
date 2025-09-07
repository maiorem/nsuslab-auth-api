package com.nsuslab.domain.user.service;

import com.nsuslab.auth.jwt.JwtTokenProvider;
import com.nsuslab.domain.user.model.User;
import com.nsuslab.domain.user.repository.UserRepository;
import com.nsuslab.infrastructure.cache.RefreshTokenCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    
    private final RefreshTokenCache refreshTokenCache;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public void saveRefreshToken(User user, String refreshToken) {
        Duration expiration = Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMs());
        
        // Redis에 토큰 저장
        refreshTokenCache.saveToken(refreshToken, user.getId(), expiration);
        
        // DB에도 저장 (백업용)
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiration.getSeconds());
        user.updateRefreshToken(refreshToken, expiresAt);
        userRepository.save(user);
        
        log.debug("Refresh token saved for user: {}", user.getId());
    }

    public Optional<User> findUserByRefreshToken(String refreshToken) {
        // 1. Redis에서 먼저 조회
        Optional<Long> userIdFromRedis = refreshTokenCache.getUserIdByToken(refreshToken);
        if (userIdFromRedis.isPresent()) {
            Optional<User> userOpt = userRepository.findById(userIdFromRedis.get());
            if (userOpt.isPresent()) {
                log.debug("Found user by refresh token from Redis: {}", userIdFromRedis.get());
                return userOpt;
            }
        }
        
        // 2. Redis에 없으면 DB에서 조회
        Optional<User> userFromDb = userRepository.findByRefreshToken(refreshToken);
        if (userFromDb.isPresent()) {
            User user = userFromDb.get();
            // DB에서 찾았으면 Redis에도 다시 저장 (복구)
            if (user.getRefreshTokenExpiresAt() != null && 
                user.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now())) {
                
                Duration remainingTime = Duration.between(LocalDateTime.now(), user.getRefreshTokenExpiresAt());
                refreshTokenCache.saveToken(refreshToken, user.getId(), remainingTime);
                log.debug("Restored token to Redis for user: {}", user.getId());
            }
            return userFromDb;
        }
        
        log.debug("Refresh token not found: {}", refreshToken);
        return Optional.empty();
    }

    public void clearRefreshToken(User user) {
        // Redis에서 삭제
        refreshTokenCache.deleteTokenByUserId(user.getId());
        
        // DB에서도 삭제
        user.clearRefreshToken();
        userRepository.save(user);
        
        log.debug("Refresh token cleared for user: {}", user.getId());
    }

    public boolean isValidRefreshToken(String refreshToken) {
        // Redis에서 먼저 확인
        if (refreshTokenCache.existsToken(refreshToken)) {
            return jwtTokenProvider.validateToken(refreshToken);
        }
        
        // Redis에 없으면 DB 확인
        Optional<User> userOpt = userRepository.findByRefreshToken(refreshToken);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRefreshTokenExpiresAt() != null && 
                user.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now())) {
                return jwtTokenProvider.validateToken(refreshToken);
            }
        }
        
        return false;
    }
}