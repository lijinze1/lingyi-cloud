package com.lingyi.ai.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lingyi")
@MapperScan("com.lingyi.ai.rag.mapper")
public class LingyiRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiRagApplication.class, args);
    }
}
