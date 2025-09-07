package com.nsuslab.redis.testcontainer;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class RedisTestContainerConfig {

    @Bean
    public RedisContainer redisContainer() {
        RedisContainer container = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(true);
        container.start();
        return container;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisContainer redisContainer) {
        return new LettuceConnectionFactory(
            redisContainer.getHost(), 
            redisContainer.getMappedPort(6379)
        );
    }
}