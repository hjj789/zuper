package com.example.zupder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot 主應用類
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.zupder")
@EnableJpaRepositories(basePackages = "com.example.zupder.repository")
@EntityScan(basePackages = "com.example.zupder.entity")
public class ZupderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZupderApplication.class, args);
    }

}
