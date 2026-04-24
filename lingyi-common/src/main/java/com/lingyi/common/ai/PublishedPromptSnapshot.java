package com.lingyi.common.ai;

public class PublishedPromptSnapshot {

    private Long promptId;
    private Long promptVersionId;
    private String promptCode;
    private String promptName;
    private String content;
    private String variablesJson;
    private AiModelConfig modelConfig;

    public Long getPromptId() {
        return promptId;
    }

    public void setPromptId(Long promptId) {
        this.promptId = promptId;
    }

    public Long getPromptVersionId() {
        return promptVersionId;
    }

    public void setPromptVersionId(Long promptVersionId) {
        this.promptVersionId = promptVersionId;
    }

    public String getPromptCode() {
        return promptCode;
    }

    public void setPromptCode(String promptCode) {
        this.promptCode = promptCode;
    }

    public String getPromptName() {
        return promptName;
    }

    public void setPromptName(String promptName) {
        this.promptName = promptName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public void setVariablesJson(String variablesJson) {
        this.variablesJson = variablesJson;
    }

    public AiModelConfig getModelConfig() {
        return modelConfig;
    }

    public void setModelConfig(AiModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }
}
