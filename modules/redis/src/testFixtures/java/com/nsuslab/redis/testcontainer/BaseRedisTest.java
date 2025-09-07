package com.nsuslab.redis.testcontainer;

import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataRedisTest
@Testcontainers
@Import(RedisTestContainerConfig.class)
@ActiveProfiles("test")
public abstract class BaseRedisTest {
}