package com.lingyi.ai.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.ai.prompt.dto.PageQueryDTO;
import com.lingyi.ai.prompt.dto.PromptSaveRequest;
import com.lingyi.ai.prompt.dto.PromptVersionCreateRequest;
import com.lingyi.ai.prompt.entity.LyPrompt;
import com.lingyi.ai.prompt.entity.LyPromptCategory;
import com.lingyi.ai.prompt.entity.LyPromptVersion;
import com.lingyi.ai.prompt.mapper.PromptCategoryMapper;
import com.lingyi.ai.prompt.mapper.PromptMapper;
import com.lingyi.ai.prompt.mapper.PromptVersionMapper;
import com.lingyi.ai.prompt.service.PromptAdminService;
import com.lingyi.ai.prompt.vo.PromptDetailVO;
import com.lingyi.ai.prompt.vo.PromptVO;
import com.lingyi.ai.prompt.vo.PromptVersionVO;
import com.lingyi.common.ai.AiModelConfig;
import com.lingyi.common.util.SnowflakeIdGenerator;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromptAdminServiceImpl implements PromptAdminService {

    private final PromptMapper promptMapper;
    private final PromptVersionMapper versionMapper;
    private final PromptCategoryMapper categoryMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromptAdminServiceImpl(PromptMapper promptMapper,
                                  PromptVersionMapper versionMapper,
                                  PromptCategoryMapper categoryMapper,
                                  SnowflakeIdGenerator idGenerator) {
        this.promptMapper = promptMapper;
        this.versionMapper = versionMapper;
        this.categoryMapper = categoryMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public IPage<PromptVO> page(PageQueryDTO queryDTO, Long categoryId) {
        Page<LyPrompt> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<LyPrompt> wrapper = new LambdaQueryWrapper<LyPrompt>()
                .eq(LyPrompt::getIsDeleted, 0)
                .orderByDesc(LyPrompt::getCreatedAt);
        if (categoryId != null) {
            wrapper.eq(LyPrompt::getCategoryId, categoryId);
        }
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(LyPrompt::getPromptCode, keyword)
                    .or().like(LyPrompt::getName, keyword)
                    .or().like(LyPrompt::getBizScene, keyword));
        }
        return promptMapper.selectPage(page, wrapper).convert(this::toPromptVO);
    }

    @Override
    public PromptDetailVO detail(Long promptId) {
        LyPrompt prompt = requirePrompt(promptId);
        PromptDetailVO detail = new PromptDetailVO();
        PromptVO promptVO = toPromptVO(prompt);
        detail.setId(promptVO.getId());
        detail.setCategoryId(promptVO.getCategoryId());
        detail.setPromptCode(promptVO.getPromptCode());
        detail.setName(promptVO.getName());
        detail.setBizScene(promptVO.getBizScene());
        detail.setDescription(promptVO.getDescription());
        detail.setStatus(promptVO.getStatus());
        detail.setPublishedVersionId(promptVO.getPublishedVersionId());
        detail.setCreatedAt(promptVO.getCreatedAt());
        detail.setVersions(versions(promptId));
        return detail;
    }

    @Override
    public PromptVO create(PromptSaveRequest request) {
        ensurePromptCodeAvailable(request.getPromptCode().trim(), null);
        requireCategory(request.getCategoryId());
        LyPrompt prompt = new LyPrompt();
        prompt.setId(idGenerator.nextId());
        prompt.setCategoryId(request.getCategoryId());
        prompt.setPromptCode(request.getPromptCode().trim());
        prompt.setName(request.getName().trim());
        prompt.setBizScene(request.getBizScene().trim());
        prompt.setDescription(request.getDescription());
        prompt.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        prompt.setIsDeleted(0);
        promptMapper.insert(prompt);
        return toPromptVO(prompt);
    }

    @Override
    public PromptVO update(Long promptId, PromptSaveRequest request) {
        LyPrompt prompt = requirePrompt(promptId);
        requireCategory(request.getCategoryId());
        ensurePromptCodeAvailable(request.getPromptCode().trim(), promptId);
        prompt.setCategoryId(request.getCategoryId());
        prompt.setPromptCode(request.getPromptCode().trim());
        prompt.setName(request.getName().trim());
        prompt.setBizScene(request.getBizScene().trim());
        prompt.setDescription(request.getDescription());
        prompt.setStatus(request.getStatus() == null ? prompt.getStatus() : request.getStatus());
        promptMapper.updateById(prompt);
        return toPromptVO(prompt);
    }

    @Override
    public void delete(Long promptId) {
        requirePrompt(promptId);
        promptMapper.deleteById(promptId);
    }

    @Override
    public List<PromptVersionVO> versions(Long promptId) {
        requirePrompt(promptId);
        return versionMapper.selectList(new LambdaQueryWrapper<LyPromptVersion>()
                        .eq(LyPromptVersion::getPromptId, promptId)
                        .eq(LyPromptVersion::getIsDeleted, 0)
                        .orderByDesc(LyPromptVersion::getVersionNo))
                .stream()
                .map(this::toPromptVersionVO)
                .toList();
    }

    @Override
    @Transactional
    public PromptVersionVO createVersion(Long promptId, PromptVersionCreateRequest request) {
        requirePrompt(promptId);
        Integer maxVersionNo = versionMapper.selectList(new LambdaQueryWrapper<LyPromptVersion>()
                        .eq(LyPromptVersion::getPromptId, promptId)
                        .eq(LyPromptVersion::getIsDeleted, 0))
                .stream()
                .map(LyPromptVersion::getVersionNo)
                .max(Integer::compareTo)
                .orElse(0);
        LyPromptVersion version = new LyPromptVersion();
        version.setId(idGenerator.nextId());
        version.setPromptId(promptId);
        version.setVersionNo(maxVersionNo + 1);
        version.setContent(request.getContent().trim());
        version.setVariablesJson(request.getVariablesJson());
        validateModelConfig(request.getModelConfig());
        version.setModelConfigJson(writeModelConfig(request.getModelConfig()));
        version.setStatus(0);
        version.setIsDeleted(0);
        versionMapper.insert(version);
        return toPromptVersionVO(version);
    }

    @Override
    @Transactional
    public void deleteVersion(Long promptId, Long versionId) {
        LyPrompt prompt = requirePrompt(promptId);
        LyPromptVersion version = requirePromptVersion(promptId, versionId);
        if (Integer.valueOf(1).equals(version.getStatus()) || versionId.equals(prompt.getPublishedVersionId())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "current published version cannot be deleted");
        }
        versionMapper.deleteById(versionId);
    }

    @Override
    @Transactional
    public PromptVersionVO publish(Long promptId, Long versionId) {
        LyPrompt prompt = requirePrompt(promptId);
        LyPromptVersion targetVersion = requirePromptVersion(promptId, versionId);
        clearPublishedVersion(promptId);
        targetVersion.setStatus(1);
        targetVersion.setPublishedAt(LocalDateTime.now());
        versionMapper.updateById(targetVersion);
        prompt.setPublishedVersionId(versionId);
        promptMapper.updateById(prompt);
        return toPromptVersionVO(targetVersion);
    }

    @Override
    @Transactional
    public PromptVersionVO rollback(Long promptId, Long versionId) {
        return publish(promptId, versionId);
    }

    private void clearPublishedVersion(Long promptId) {
        List<LyPromptVersion> versions = versionMapper.selectList(new LambdaQueryWrapper<LyPromptVersion>()
                .eq(LyPromptVersion::getPromptId, promptId)
                .eq(LyPromptVersion::getIsDeleted, 0)
                .eq(LyPromptVersion::getStatus, 1));
        for (LyPromptVersion version : versions) {
            version.setStatus(2);
            versionMapper.updateById(version);
        }
    }

    private LyPrompt requirePrompt(Long promptId) {
        LyPrompt prompt = promptMapper.selectById(promptId);
        if (prompt == null || Integer.valueOf(1).equals(prompt.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return prompt;
    }

    private LyPromptVersion requirePromptVersion(Long promptId, Long versionId) {
        LyPromptVersion version = versionMapper.selectById(versionId);
        if (version == null
                || Integer.valueOf(1).equals(version.getIsDeleted())
                || !promptId.equals(version.getPromptId())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return version;
    }

    private void ensurePromptCodeAvailable(String promptCode, Long currentPromptId) {
        LyPrompt existing = promptMapper.selectOne(new LambdaQueryWrapper<LyPrompt>()
                .eq(LyPrompt::getPromptCode, promptCode)
                .eq(LyPrompt::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !existing.getId().equals(currentPromptId)) {
            throw new BizException(ErrorCode.PROMPT_CODE_ALREADY_EXISTS);
        }
    }

    private LyPromptCategory requireCategory(Long categoryId) {
        LyPromptCategory category = categoryMapper.selectById(categoryId);
        if (category == null || Integer.valueOf(1).equals(category.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return category;
    }

    private PromptVO toPromptVO(LyPrompt prompt) {
        PromptVO vo = new PromptVO();
        vo.setId(prompt.getId());
        vo.setCategoryId(prompt.getCategoryId());
        vo.setPromptCode(prompt.getPromptCode());
        vo.setName(prompt.getName());
        vo.setBizScene(prompt.getBizScene());
        vo.setDescription(prompt.getDescription());
        vo.setStatus(prompt.getStatus());
        vo.setPublishedVersionId(prompt.getPublishedVersionId());
        vo.setCreatedAt(prompt.getCreatedAt());
        return vo;
    }

    private PromptVersionVO toPromptVersionVO(LyPromptVersion version) {
        PromptVersionVO vo = new PromptVersionVO();
        vo.setId(version.getId());
        vo.setPromptId(version.getPromptId());
        vo.setVersionNo(version.getVersionNo());
        vo.setContent(version.getContent());
        vo.setVariablesJson(version.getVariablesJson());
        vo.setModelConfig(readModelConfig(version.getModelConfigJson()));
        vo.setStatus(version.getStatus());
        vo.setPublishedAt(version.getPublishedAt());
        vo.setCreatedAt(version.getCreatedAt());
        return vo;
    }

    private void validateModelConfig(AiModelConfig modelConfig) {
        if (modelConfig == null) {
            return;
        }
        validateRange("temperature", modelConfig.getTemperature(), BigDecimal.ZERO, new BigDecimal("2"));
        validateRange("topP", modelConfig.getTopP(), BigDecimal.ZERO, BigDecimal.ONE);
        validateRange("presencePenalty", modelConfig.getPresencePenalty(), new BigDecimal("-2"), new BigDecimal("2"));
        validateRange("frequencyPenalty", modelConfig.getFrequencyPenalty(), new BigDecimal("-2"), new BigDecimal("2"));
        if (modelConfig.getTopK() != null && modelConfig.getTopK() < 1) {
            throw new BizException(ErrorCode.BAD_REQUEST, "topK must be greater than 0");
        }
        if (modelConfig.getMaxTokens() != null && modelConfig.getMaxTokens() < 1) {
            throw new BizException(ErrorCode.BAD_REQUEST, "maxTokens must be greater than 0");
        }
    }

    private void validateRange(String field, BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return;
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    field + " must be between " + min.stripTrailingZeros().toPlainString()
                            + " and " + max.stripTrailingZeros().toPlainString());
        }
    }

    private String writeModelConfig(AiModelConfig modelConfig) {
        if (modelConfig == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(modelConfig);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, "modelConfig is invalid");
        }
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
