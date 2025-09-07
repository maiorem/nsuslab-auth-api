package com.nsuslab.supports.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"));
    }

    private Info apiInfo() {
        return new Info()
                .title("NSUS Lab Authentication API")
                .description("""
                        JWT 기반 사용자 인증 시스템 API
                        
                        ## 주요 기능
                        - 이메일 기반 회원가입 및 로그인
                        - JWT Access Token 및 Refresh Token 관리
                        - BCrypt 암호화를 통한 보안 강화
                        - Redis 캐싱을 활용한 성능 최적화
                        - Rate Limiting을 통한 API 보호
                        - 사용자 프로필 관리
                        
                        ## 인증 방법
                        API 호출 시 Authorization 헤더에 'Bearer {access_token}' 형태로 토큰을 포함해주세요.
                        """)
                .version("1.0.0");
    }
}