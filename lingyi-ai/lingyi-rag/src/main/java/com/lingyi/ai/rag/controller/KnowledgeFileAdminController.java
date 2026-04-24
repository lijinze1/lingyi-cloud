package com.lingyi.ai.rag.controller;

import com.lingyi.ai.rag.service.KnowledgeFileAdminService;
import com.lingyi.ai.rag.vo.KnowledgeChunkVO;
import com.lingyi.ai.rag.vo.KnowledgeFileVO;
import com.lingyi.common.web.domain.Result;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/admin/kbs/{kbId}/files")
public class KnowledgeFileAdminController {

    private final KnowledgeFileAdminService knowledgeFileAdminService;

    public KnowledgeFileAdminController(KnowledgeFileAdminService knowledgeFileAdminService) {
        this.knowledgeFileAdminService = knowledgeFileAdminService;
    }

    @GetMapping
    public Result<List<KnowledgeFileVO>> files(@PathVariable Long kbId) {
        return Result.success(knowledgeFileAdminService.files(kbId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<KnowledgeFileVO> upload(@PathVariable Long kbId, @RequestPart("file") MultipartFile file) {
        return Result.success(knowledgeFileAdminService.upload(kbId, file));
    }

    @GetMapping("/{fileId}")
    public Result<KnowledgeFileVO> detail(@PathVariable Long kbId, @PathVariable Long fileId) {
        return Result.success(knowledgeFileAdminService.detail(kbId, fileId));
    }

    @DeleteMapping("/{fileId}")
    public Result<Void> delete(@PathVariable Long kbId, @PathVariable Long fileId) {
        knowledgeFileAdminService.delete(kbId, fileId);
        return Result.success(null);
    }

    @PostMapping("/{fileId}/parse")
    public Result<KnowledgeFileVO> parse(@PathVariable Long kbId, @PathVariable Long fileId) {
        return Result.success(knowledgeFileAdminService.parse(kbId, fileId));
    }

    @PostMapping("/{fileId}/index")
    public Result<KnowledgeFileVO> index(@PathVariable Long kbId, @PathVariable Long fileId) {
        return Result.success(knowledgeFileAdminService.index(kbId, fileId));
    }

    @GetMapping("/{fileId}/chunks")
    public Result<List<KnowledgeChunkVO>> chunks(@PathVariable Long kbId, @PathVariable Long fileId) {
        return Result.success(knowledgeFileAdminService.chunks(kbId, fileId));
    }
}
