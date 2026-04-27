package com.lingyi.service.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "lingyi.minio")
public class MinioProperties {

    private boolean enabled = false;
    private String endpoint = "http://127.0.0.1:9000";
    private String bucket = "lingyi-product";
    private String publicBaseUrl = "http://127.0.0.1:9000/lingyi-product";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
}
