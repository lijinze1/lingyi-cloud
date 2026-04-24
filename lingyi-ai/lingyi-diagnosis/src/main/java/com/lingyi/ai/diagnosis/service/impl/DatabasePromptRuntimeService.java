package com.lingyi.ai.diagnosis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.ai.diagnosis.entity.LyPrompt;
import com.lingyi.ai.diagnosis.entity.LyPromptVersion;
import com.lingyi.ai.diagnosis.mapper.PromptMapper;
import com.lingyi.ai.diagnosis.mapper.PromptVersionMapper;
import com.lingyi.ai.diagnosis.service.PromptRuntimeService;
import com.lingyi.common.ai.AiModelConfig;
import com.lingyi.common.ai.PublishedPromptSnapshot;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DatabasePromptRuntimeService implements PromptRuntimeService {

    private final PromptMapper promptMapper;
    private final PromptVersionMapper promptVersionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatabasePromptRuntimeService(PromptMapper promptMapper, PromptVersionMapper promptVersionMapper) {
        this.promptMapper = promptMapper;
        this.promptVersionMapper = promptVersionMapper;
    }

    @Override
    public PublishedPromptSnapshot loadPublishedPrompt(String promptCode) {
        LyPrompt prompt = promptMapper.selectOne(new LambdaQueryWrapper<LyPrompt>()
                .eq(LyPrompt::getPromptCode, promptCode)
                .eq(LyPrompt::getStatus, 1)
                .eq(LyPrompt::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (prompt == null) {
            throw new BizException(ErrorCode.PROMPT_PUBLISHED_VERSION_NOT_FOUND, "prompt not found: " + promptCode);
        }
        LyPromptVersion version = promptVersionMapper.selectOne(new LambdaQueryWrapper<LyPromptVersion>()
                .eq(LyPromptVersion::getId, prompt.getPublishedVersionId())
                .eq(LyPromptVersion::getPromptId, prompt.getId())
                .eq(LyPromptVersion::getStatus, 1)
                .eq(LyPromptVersion::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (version == null) {
            throw new BizException(ErrorCode.PROMPT_PUBLISHED_VERSION_NOT_FOUND,
                    "published prompt version not found: " + promptCode);
        }
        PublishedPromptSnapshot snapshot = new PublishedPromptSnapshot();
        snapshot.setPromptId(prompt.getId());
        snapshot.setPromptVersionId(version.getId());
        snapshot.setPromptCode(prompt.getPromptCode());
        snapshot.setPromptName(prompt.getName());
        snapshot.setContent(version.getContent());
        snapshot.setVariablesJson(version.getVariablesJson());
        snapshot.setModelConfig(readModelConfig(version.getModelConfigJson()));
        return snapshot;
    }

    @Override
    public String renderContent(PublishedPromptSnapshot snapshot, Map<String, Object> variables) {
        String rendered = snapshot.getContent();
        if (rendered == null || variables == null || variables.isEmpty()) {
            return rendered;
        }
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return rendered;
    }

    private AiModelConfig readModelConfig(String modelConfigJson) {
        if (modelConfigJson == null || modelConfigJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(modelConfigJson, AiModelConfig.class);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "stored modelConfig is invalid");
        }
    }
}
