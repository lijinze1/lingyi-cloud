package com.lingyi.service.product;

import com.lingyi.service.product.config.MinioProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.lingyi.service.product.mapper")
@EnableConfigurationProperties(MinioProperties.class)
public class LingyiProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiProductApplication.class, args);
    }
}

