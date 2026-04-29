package com.lingyi.ai.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.ai.prompt.dto.PromptCategorySaveRequest;
import com.lingyi.ai.prompt.entity.LyPrompt;
import com.lingyi.ai.prompt.entity.LyPromptCategory;
import com.lingyi.ai.prompt.mapper.PromptCategoryMapper;
import com.lingyi.ai.prompt.mapper.PromptMapper;
import com.lingyi.ai.prompt.service.PromptCategoryAdminService;
import com.lingyi.ai.prompt.vo.PromptCategoryVO;
import com.lingyi.common.util.SnowflakeIdGenerator;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PromptCategoryAdminServiceImpl implements PromptCategoryAdminService {

    private final PromptCategoryMapper categoryMapper;
    private final PromptMapper promptMapper;
    private final SnowflakeIdGenerator idGenerator;

    public PromptCategoryAdminServiceImpl(PromptCategoryMapper categoryMapper,
                                          PromptMapper promptMapper,
                                          SnowflakeIdGenerator idGenerator) {
        this.categoryMapper = categoryMapper;
        this.promptMapper = promptMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public List<PromptCategoryVO> tree() {
        List<LyPromptCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<LyPromptCategory>()
                .eq(LyPromptCategory::getIsDeleted, 0)
                .orderByAsc(LyPromptCategory::getSortNo)
                .orderByAsc(LyPromptCategory::getId));
        Map<Long, PromptCategoryVO> nodeMap = new LinkedHashMap<>();
        List<PromptCategoryVO> roots = new ArrayList<>();
        for (LyPromptCategory category : categories) {
            PromptCategoryVO vo = toCategoryVO(category);
            nodeMap.put(vo.getId(), vo);
        }
        for (PromptCategoryVO vo : nodeMap.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0L) {
                roots.add(vo);
                continue;
            }
            PromptCategoryVO parent = nodeMap.get(vo.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        roots.sort(Comparator.comparing(PromptCategoryVO::getSortNo).thenComparing(PromptCategoryVO::getId));
        return roots;
    }

    @Override
    public PromptCategoryVO detail(Long categoryId) {
        return toCategoryVO(requireCategory(categoryId));
    }

    @Override
    public PromptCategoryVO create(PromptCategorySaveRequest request) {
        ensureCategoryCodeAvailable(request.getCategoryCode().trim(), null);
        requireParentCategory(request.getParentId());
        LyPromptCategory category = new LyPromptCategory();
        category.setId(idGenerator.nextId());
        category.setParentId(defaultParentId(request.getParentId()));
        category.setCategoryCode(request.getCategoryCode().trim());
        category.setCategoryName(request.getCategoryName().trim());
        category.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        category.setIsDeleted(0);
        categoryMapper.insert(category);
        return detail(category.getId());
    }

    @Override
    public PromptCategoryVO update(Long categoryId, PromptCategorySaveRequest request) {
        LyPromptCategory category = requireCategory(categoryId);
        ensureCategoryCodeAvailable(request.getCategoryCode().trim(), categoryId);
        requireParentCategory(request.getParentId());
        category.setParentId(defaultParentId(request.getParentId()));
        category.setCategoryCode(request.getCategoryCode().trim());
        category.setCategoryName(request.getCategoryName().trim());
        category.setSortNo(request.getSortNo() == null ? category.getSortNo() : request.getSortNo());
        category.setStatus(request.getStatus() == null ? category.getStatus() : request.getStatus());
        categoryMapper.updateById(category);
        return detail(categoryId);
    }

    @Override
    public void delete(Long categoryId) {
        requireCategory(categoryId);
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<LyPromptCategory>()
                .eq(LyPromptCategory::getParentId, categoryId)
                .eq(LyPromptCategory::getIsDeleted, 0));
        if (childCount != null && childCount > 0) {
            throw new BizException(ErrorCode.PROMPT_CATEGORY_HAS_CHILDREN);
        }
        Long promptCount = promptMapper.selectCount(new LambdaQueryWrapper<LyPrompt>()
                .eq(LyPrompt::getCategoryId, categoryId)
                .eq(LyPrompt::getIsDeleted, 0));
        if (promptCount != null && promptCount > 0) {
            throw new BizException(ErrorCode.PROMPT_CATEGORY_HAS_PROMPTS);
        }
        categoryMapper.deleteById(categoryId);
    }

    private LyPromptCategory requireCategory(Long categoryId) {
        LyPromptCategory category = categoryMapper.selectById(categoryId);
        if (category == null || Integer.valueOf(1).equals(category.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return category;
    }

    private Long defaultParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private void ensureCategoryCodeAvailable(String categoryCode, Long currentCategoryId) {
        LyPromptCategory existing = categoryMapper.selectOne(new LambdaQueryWrapper<LyPromptCategory>()
                .eq(LyPromptCategory::getCategoryCode, categoryCode)
                .eq(LyPromptCategory::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !existing.getId().equals(currentCategoryId)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "prompt category code already exists");
        }
    }

    private void requireParentCategory(Long parentId) {
        Long normalizedParentId = defaultParentId(parentId);
        if (normalizedParentId == 0L) {
            return;
        }
        requireCategory(normalizedParentId);
    }

    private PromptCategoryVO toCategoryVO(LyPromptCategory entity) {
        PromptCategoryVO vo = new PromptCategoryVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setCategoryName(entity.getCategoryName());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
