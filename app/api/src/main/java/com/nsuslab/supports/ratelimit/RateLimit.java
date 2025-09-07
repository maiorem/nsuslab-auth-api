package com.nsuslab.supports.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Rate limit key (default: method name)
     */
    String key() default "";
    
    /**
     * Time window in seconds
     */
    int time() default 60;
    
    /**
     * Number of requests allowed in time window
     */
    int count() default 10;
    
    /**
     * Rate limit type
     */
    RateLimitType limitType() default RateLimitType.IP;
    
    /**
     * Custom message when rate limit exceeded
     */
    String message() default "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";
}