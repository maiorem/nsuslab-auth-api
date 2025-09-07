package com.nsuslab.supports.error;

public class RateLimitExceededException extends RuntimeException {
    private final int retryAfter;
    
    public RateLimitExceededException(String message, int retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }
    
    public int getRetryAfter() {
        return retryAfter;
    }
}