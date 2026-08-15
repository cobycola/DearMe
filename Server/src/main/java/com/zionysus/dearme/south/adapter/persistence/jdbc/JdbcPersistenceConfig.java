package com.zionysus.dearme.south.adapter.persistence.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * 仅 jdbc 模式装配：用 spring.datasource.* 建 Hikari 池化 DataSource + JdbcTemplate，
 * 启动时显式跑 schema.sql 建表（不依赖 Boot 的 sql.init autoconfigure，避开 Boot 4
 * starter-jdbc 不再传递 autoconfigure 的设计变化）。PGUSER/PGPASSWORD 走环境变量，守密钥红线。
 */
@Configuration
@ConditionalOnProperty(name = "dearme.persistence", havingValue = "jdbc")
public class JdbcPersistenceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driver) {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driver)
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate t = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        return t;
    }
}