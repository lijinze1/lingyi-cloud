package com.lingyi.ai.prompt.controller;

import com.lingyi.ai.prompt.dto.PromptCategorySaveRequest;
import com.lingyi.ai.prompt.service.PromptCategoryAdminService;
import com.lingyi.ai.prompt.vo.PromptCategoryVO;
import com.lingyi.common.web.domain.Result;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/admin/prompt-categories")
public class PromptCategoryAdminController {

    private final PromptCategoryAdminService promptCategoryAdminService;

    public PromptCategoryAdminController(PromptCategoryAdminService promptCategoryAdminService) {
        this.promptCategoryAdminService = promptCategoryAdminService;
    }

    @GetMapping
    public Result<List<PromptCategoryVO>> tree() {
        return Result.success(promptCategoryAdminService.tree());
    }

    @GetMapping("/{categoryId}")
    public Result<PromptCategoryVO> detail(@PathVariable Long categoryId) {
        return Result.success(promptCategoryAdminService.detail(categoryId));
    }

    @PostMapping
    public Result<PromptCategoryVO> create(@Valid @RequestBody PromptCategorySaveRequest request) {
        return Result.success(promptCategoryAdminService.create(request));
    }

    @PutMapping("/{categoryId}")
    public Result<PromptCategoryVO> update(@PathVariable Long categoryId,
                                           @Valid @RequestBody PromptCategorySaveRequest request) {
        return Result.success(promptCategoryAdminService.update(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public Result<Void> delete(@PathVariable Long categoryId) {
        promptCategoryAdminService.delete(categoryId);
        return Result.success(null);
    }
}
