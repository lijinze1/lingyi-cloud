package com.lingyi.ai.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.vectorstore.qdrant")
public class SpringAiQdrantProperties {

    private String host;
    private Integer port;
    private String apiKey;
    private Boolean useTls = Boolean.TRUE;
    private Boolean initializeSchema = Boolean.TRUE;
}
