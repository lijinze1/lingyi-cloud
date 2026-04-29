package com.lingyi.ai.chatbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lingyi")
@MapperScan("com.lingyi.ai.chatbot.mapper")
public class LingyiChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiChatbotApplication.class, args);
    }
}
