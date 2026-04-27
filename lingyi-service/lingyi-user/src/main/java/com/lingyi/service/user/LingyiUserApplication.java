package com.lingyi.service.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.lingyi.service.user.mapper")
@ConfigurationPropertiesScan("com.lingyi.service.user.config")
public class LingyiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiUserApplication.class, args);
    }
}

