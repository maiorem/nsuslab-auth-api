package com.nsuslab.redis.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisProperties {
    private String host = "localhost";
    private int port = 6379;
    private String password;
    private int database = 0;
    private int timeout = 2000;
    private Master master;
    
    @Getter
    @Setter
    public static class Master {
        private String host = "localhost";
        private int port = 6379;
    }
}