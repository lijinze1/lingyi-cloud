package com.lingyi.service.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.lingyi.service.order.mapper")
public class LingyiOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiOrderApplication.class, args);
    }
}

