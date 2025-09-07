package com.nsuslab.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenCache {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";

    public void saveToken(String refreshToken, Long userId, Duration expiration) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        
        // 기존 토큰이 있다면 먼저 삭제
        Optional<String> existingToken = getTokenByUserId(userId);
        if (existingToken.isPresent() && !existingToken.get().equals(refreshToken)) {
            redisTemplate.delete(TOKEN_PREFIX + existingToken.get());
        }
        
        // 새 토큰 저장 (양방향 매핑)
        redisTemplate.opsForValue().set(tokenKey, userId.toString(), expiration);
        redisTemplate.opsForValue().set(userTokenKey, refreshToken, expiration);
    }

    public Optional<Long> getUserIdByToken(String refreshToken) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        return userId != null ? Optional.of(Long.valueOf(userId)) : Optional.empty();
    }

    public Optional<String> getTokenByUserId(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(userTokenKey);
        return Optional.ofNullable(token);
    }

    public void deleteToken(String refreshToken) {
        Optional<Long> userIdOpt = getUserIdByToken(refreshToken);
        if (userIdOpt.isPresent()) {
            String tokenKey = TOKEN_PREFIX + refreshToken;
            String userTokenKey = USER_TOKEN_PREFIX + userIdOpt.get();
            
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userTokenKey);
        }
    }

    public void deleteTokenByUserId(Long userId) {
        Optional<String> tokenOpt = getTokenByUserId(userId);
        if (tokenOpt.isPresent()) {
            deleteToken(tokenOpt.get());
        }
    }

    public boolean existsToken(String refreshToken) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }
}