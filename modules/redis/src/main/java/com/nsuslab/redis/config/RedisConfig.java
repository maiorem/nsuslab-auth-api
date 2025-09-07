package com.nsuslab.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    @ConfigurationProperties("datasource.redis")
    public RedisProperties redisMasterProperties() {
        return new RedisProperties();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisProperties masterProps = redisMasterProperties();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        
        // master 속성에서 호스트와 포트 가져오기
        if (masterProps.getMaster() != null) {
            config.setHostName(masterProps.getMaster().getHost());
            config.setPort(masterProps.getMaster().getPort());
        } else {
            // fallback to root level
            config.setHostName(masterProps.getHost());
            config.setPort(masterProps.getPort());
        }
        
        config.setDatabase(masterProps.getDatabase());
        
        // 비밀번호 설정
        if (masterProps.getPassword() != null && !masterProps.getPassword().isEmpty()) {
            config.setPassword(masterProps.getPassword());
        }
        
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // String serialization for keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}