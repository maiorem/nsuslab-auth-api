package com.nsuslab.supports.ratelimit;

public enum RateLimitType {
    /**
     * Rate limit by IP address
     */
    IP,
    
    /**
     * Rate limit by user ID
     */
    USER,
    
    /**
     * Rate limit by custom key
     */
    CUSTOM
}