package com.zionysus.dearme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring Boot 4 的 DB autoconfigure 不在 classpath（starter-jdbc 不再传递），
// 内存（默认）模式下没有 spring.datasource.* 也不会启动爆，无需 exclude。
// jdbc 模式由 JdbcPersistenceConfig 显式建 DataSource + JdbcTemplate。
@SpringBootApplication
public class DearMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DearMeApplication.class, args);
    }

}
