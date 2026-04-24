package com.lingyi.ai.rag.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lingyi.ai.rag.defaults")
public class RagDefaultsProperties {

    private String embeddingModel;
    private List<String> supportedFileTypes = List.of("txt", "md", "pdf", "docx");
}
