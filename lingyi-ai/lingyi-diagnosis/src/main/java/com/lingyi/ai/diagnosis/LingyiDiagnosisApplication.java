package com.lingyi.ai.diagnosis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lingyi")
@MapperScan("com.lingyi.ai.diagnosis.mapper")
public class LingyiDiagnosisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingyiDiagnosisApplication.class, args);
    }
}
