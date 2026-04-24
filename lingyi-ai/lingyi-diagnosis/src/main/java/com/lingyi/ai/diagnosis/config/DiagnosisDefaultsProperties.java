package com.lingyi.ai.diagnosis.config;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lingyi.ai.diagnosis.defaults")
public class DiagnosisDefaultsProperties {

    private String promptCode;
    private String model;
    private BigDecimal temperature;
    private BigDecimal topP;
    private Integer topK;
    private Integer maxTokens;
    private BigDecimal presencePenalty;
    private BigDecimal frequencyPenalty;
}
