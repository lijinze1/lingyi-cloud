package com.lingyi.ai.rag.service;

import com.lingyi.ai.rag.vo.KnowledgeChunkVO;
import com.lingyi.ai.rag.vo.KnowledgeFileVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeFileAdminService {

    List<KnowledgeFileVO> files(Long kbId);

    KnowledgeFileVO upload(Long kbId, MultipartFile file);

    KnowledgeFileVO detail(Long kbId, Long fileId);

    void delete(Long kbId, Long fileId);

    KnowledgeFileVO parse(Long kbId, Long fileId);

    KnowledgeFileVO index(Long kbId, Long fileId);

    List<KnowledgeChunkVO> chunks(Long kbId, Long fileId);
}
