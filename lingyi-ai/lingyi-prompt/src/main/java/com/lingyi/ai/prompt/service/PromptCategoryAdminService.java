package com.lingyi.ai.prompt.service;

import com.lingyi.ai.prompt.dto.PromptCategorySaveRequest;
import com.lingyi.ai.prompt.vo.PromptCategoryVO;
import java.util.List;

public interface PromptCategoryAdminService {

    List<PromptCategoryVO> tree();

    PromptCategoryVO detail(Long categoryId);

    PromptCategoryVO create(PromptCategorySaveRequest request);

    PromptCategoryVO update(Long categoryId, PromptCategorySaveRequest request);

    void delete(Long categoryId);
}
