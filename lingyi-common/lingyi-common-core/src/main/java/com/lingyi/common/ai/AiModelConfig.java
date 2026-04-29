package com.lingyi.common.ai;

import java.math.BigDecimal;

public class AiModelConfig {

    private String model;
    private BigDecimal temperature;
    private BigDecimal topP;
    private Integer topK;
    private Integer maxTokens;
    private BigDecimal presencePenalty;
    private BigDecimal frequencyPenalty;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getTopP() {
        return topP;
    }

    public void setTopP(BigDecimal topP) {
        this.topP = topP;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public BigDecimal getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(BigDecimal presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public BigDecimal getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(BigDecimal frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
}
