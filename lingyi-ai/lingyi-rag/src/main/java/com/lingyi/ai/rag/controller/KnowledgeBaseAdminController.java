package com.lingyi.ai.rag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.ai.rag.dto.KnowledgeBaseSaveRequest;
import com.lingyi.ai.rag.dto.PageQueryDTO;
import com.lingyi.ai.rag.service.KnowledgeBaseAdminService;
import com.lingyi.ai.rag.vo.KnowledgeBaseVO;
import com.lingyi.common.web.domain.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/admin/kbs")
public class KnowledgeBaseAdminController {

    private final KnowledgeBaseAdminService knowledgeBaseAdminService;

    public KnowledgeBaseAdminController(KnowledgeBaseAdminService knowledgeBaseAdminService) {
        this.knowledgeBaseAdminService = knowledgeBaseAdminService;
    }

    @GetMapping
    public Result<IPage<KnowledgeBaseVO>> page(PageQueryDTO queryDTO) {
        return Result.success(knowledgeBaseAdminService.page(queryDTO));
    }

    @GetMapping("/{kbId}")
    public Result<KnowledgeBaseVO> detail(@PathVariable Long kbId) {
        return Result.success(knowledgeBaseAdminService.detail(kbId));
    }

    @PostMapping
    public Result<KnowledgeBaseVO> create(@Valid @RequestBody KnowledgeBaseSaveRequest request) {
        return Result.success(knowledgeBaseAdminService.create(request));
    }

    @PutMapping("/{kbId}")
    public Result<KnowledgeBaseVO> update(@PathVariable Long kbId,
                                          @Valid @RequestBody KnowledgeBaseSaveRequest request) {
        return Result.success(knowledgeBaseAdminService.update(kbId, request));
    }

    @DeleteMapping("/{kbId}")
    public Result<Void> delete(@PathVariable Long kbId) {
        knowledgeBaseAdminService.delete(kbId);
        return Result.success(null);
    }
}
