package com.nsuslab.auth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret = "mySecretKey";
    private long accessTokenExpirationMs = 900000; // 15분
    private long refreshTokenExpirationMs = 604800000; // 7일
}