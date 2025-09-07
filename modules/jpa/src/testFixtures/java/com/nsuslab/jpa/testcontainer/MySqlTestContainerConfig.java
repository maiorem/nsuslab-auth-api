package com.nsuslab.jpa.testcontainer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestContainerConfig {

    @Bean
    public MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("nsuslab_auth_test")
                .withUsername("test")
                .withPassword("test")
                .withEnv("MYSQL_ROOT_PASSWORD", "test")
                .withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_unicode_ci",
                    "--default-time-zone=+09:00"
                )
                .withReuse(true);
        container.start();
        return container;
    }

    @Bean
    @Primary
    public DataSource dataSource(MySQLContainer<?> mysqlContainer) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlContainer.getJdbcUrl());
        config.setUsername(mysqlContainer.getUsername());
        config.setPassword(mysqlContainer.getPassword());
        config.setDriverClassName(mysqlContainer.getDriverClassName());
        return new HikariDataSource(config);
    }
}