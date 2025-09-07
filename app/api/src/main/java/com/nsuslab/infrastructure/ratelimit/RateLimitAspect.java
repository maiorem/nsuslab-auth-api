package com.nsuslab.infrastructure.ratelimit;

import com.nsuslab.auth.jwt.JwtTokenProvider;
import com.nsuslab.supports.ratelimit.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final RateLimitService rateLimitService;
    private final JwtTokenProvider jwtTokenProvider;

    @Before("@annotation(com.nsuslab.supports.ratelimit.RateLimit)")
    public void checkRateLimit(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        String key = buildRateLimitKey(rateLimit, method);
        
        rateLimitService.checkRateLimit(
            key,
            rateLimit.time(),
            rateLimit.count(),
            rateLimit.message()
        );
    }
    
    private String buildRateLimitKey(RateLimit rateLimit, Method method) {
        String baseKey = rateLimit.key().isEmpty() ? method.getName() : rateLimit.key();
        
        switch (rateLimit.limitType()) {
            case IP:
                return baseKey + ":" + getClientIP();
            case USER:
                return baseKey + ":" + getCurrentUserId();
            case CUSTOM:
                return baseKey;
            default:
                return baseKey + ":" + getClientIP();
        }
    }
    
    private String getClientIP() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
    
    private String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // JWT 토큰 검증 및 사용자 ID 추출
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    return userId.toString();
                }
            }
            
            // JWT 토큰이 없거나 유효하지 않으면 IP 사용 (fallback)
            log.debug("No valid JWT token found, using IP for rate limiting");
            return "anonymous:" + getClientIP();
            
        } catch (Exception e) {
            log.warn("Failed to extract user ID from JWT token: {}", e.getMessage());
            // 오류 발생 시 IP 사용
            return "error:" + getClientIP();
        }
    }
}