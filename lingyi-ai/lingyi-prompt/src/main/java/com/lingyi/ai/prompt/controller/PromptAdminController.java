package com.lingyi.ai.prompt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.ai.prompt.dto.PageQueryDTO;
import com.lingyi.ai.prompt.dto.PromptSaveRequest;
import com.lingyi.ai.prompt.dto.PromptVersionCreateRequest;
import com.lingyi.ai.prompt.service.PromptAdminService;
import com.lingyi.ai.prompt.vo.PromptDetailVO;
import com.lingyi.ai.prompt.vo.PromptVO;
import com.lingyi.ai.prompt.vo.PromptVersionVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/admin/prompts")
public class PromptAdminController {

    private final PromptAdminService promptAdminService;

    public PromptAdminController(PromptAdminService promptAdminService) {
        this.promptAdminService = promptAdminService;
    }

    @GetMapping
    public Result<IPage<PromptVO>> page(PageQueryDTO queryDTO,
                                        @RequestParam(required = false) Long categoryId) {
        return Result.success(promptAdminService.page(queryDTO, categoryId));
    }

    @GetMapping("/{promptId}")
    public Result<PromptDetailVO> detail(@PathVariable Long promptId) {
        return Result.success(promptAdminService.detail(promptId));
    }

    @PostMapping
    public Result<PromptVO> create(@Valid @RequestBody PromptSaveRequest request) {
        return Result.success(promptAdminService.create(request));
    }

    @PutMapping("/{promptId}")
    public Result<PromptVO> update(@PathVariable Long promptId, @Valid @RequestBody PromptSaveRequest request) {
        return Result.success(promptAdminService.update(promptId, request));
    }

    @DeleteMapping("/{promptId}")
    public Result<Void> delete(@PathVariable Long promptId) {
        promptAdminService.delete(promptId);
        return Result.success(null);
    }

    @GetMapping("/{promptId}/versions")
    public Result<List<PromptVersionVO>> versions(@PathVariable Long promptId) {
        return Result.success(promptAdminService.versions(promptId));
    }

    @PostMapping("/{promptId}/versions")
    public Result<PromptVersionVO> createVersion(@PathVariable Long promptId,
                                                 @Valid @RequestBody PromptVersionCreateRequest request) {
        return Result.success(promptAdminService.createVersion(promptId, request));
    }

    @PostMapping("/{promptId}/versions/{versionId}/publish")
    public Result<PromptVersionVO> publish(@PathVariable Long promptId, @PathVariable Long versionId) {
        return Result.success(promptAdminService.publish(promptId, versionId));
    }

    @PostMapping("/{promptId}/versions/{versionId}/rollback")
    public Result<PromptVersionVO> rollback(@PathVariable Long promptId, @PathVariable Long versionId) {
        return Result.success(promptAdminService.rollback(promptId, versionId));
    }
}
