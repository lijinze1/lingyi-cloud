package com.lingyi.ai.prompt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lingyi.ai.prompt.mapper")
public class LingyiPromptApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiPromptApplication.class, args);
    }
}
