package com.nsuslab.infrastructure.ratelimit;

import com.nsuslab.supports.error.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public void checkRateLimit(String key, int timeWindow, int maxRequests, String message) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - timeWindow * 1000L;
        
        // 1. 만료된 항목 제거
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        
        // 2. 현재 요청 수 확인
        Long currentCount = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
        
        if (currentCount != null && currentCount >= maxRequests) {
            log.warn("Rate limit exceeded for key: {}, count: {}/{}", key, currentCount, maxRequests);
            throw new RateLimitExceededException(message, timeWindow);
        }
        
        // 3. 새 요청 추가
        redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);
        redisTemplate.expire(redisKey, Duration.ofSeconds(timeWindow));
        
        long remaining = maxRequests - (currentCount != null ? currentCount : 0) - 1;
        log.debug("Rate limit check passed for key: {}, remaining: {}", key, remaining);
    }
    
    public long getRemainingRequests(String key, int timeWindow, int maxRequests) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        
        // Clean expired entries
        redisTemplate.opsForZSet().removeRangeByScore(
            redisKey, 
            Double.NEGATIVE_INFINITY, 
            currentTime - timeWindow * 1000L
        );
        
        Long currentCount = redisTemplate.opsForZSet().count(redisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return Math.max(0, maxRequests - (currentCount != null ? currentCount : 0));
    }
    
    public void resetRateLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(redisKey);
        log.debug("Rate limit reset for key: {}", key);
    }
}