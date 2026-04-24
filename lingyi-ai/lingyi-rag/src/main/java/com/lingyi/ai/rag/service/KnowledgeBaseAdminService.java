package com.lingyi.ai.rag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.ai.rag.dto.KnowledgeBaseSaveRequest;
import com.lingyi.ai.rag.dto.PageQueryDTO;
import com.lingyi.ai.rag.vo.KnowledgeBaseVO;

public interface KnowledgeBaseAdminService {

    IPage<KnowledgeBaseVO> page(PageQueryDTO queryDTO);

    KnowledgeBaseVO detail(Long kbId);

    KnowledgeBaseVO create(KnowledgeBaseSaveRequest request);

    KnowledgeBaseVO update(Long kbId, KnowledgeBaseSaveRequest request);

    void delete(Long kbId);
}
