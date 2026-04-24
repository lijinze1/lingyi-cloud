package com.lingyi.ai.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingyi.ai.rag.dto.KnowledgeBaseSaveRequest;
import com.lingyi.ai.rag.dto.PageQueryDTO;
import com.lingyi.ai.rag.entity.LyKb;
import com.lingyi.ai.rag.mapper.KnowledgeBaseMapper;
import com.lingyi.ai.rag.service.KnowledgeBaseAdminService;
import com.lingyi.ai.rag.vo.KnowledgeBaseVO;
import com.lingyi.common.util.SnowflakeIdGenerator;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseAdminServiceImpl implements KnowledgeBaseAdminService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final SnowflakeIdGenerator idGenerator;

    public KnowledgeBaseAdminServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper,
                                         SnowflakeIdGenerator idGenerator) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public IPage<KnowledgeBaseVO> page(PageQueryDTO queryDTO) {
        Page<LyKb> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<LyKb> wrapper = new LambdaQueryWrapper<LyKb>()
                .eq(LyKb::getIsDeleted, 0)
                .orderByDesc(LyKb::getCreatedAt);
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(LyKb::getKbCode, keyword).or().like(LyKb::getName, keyword));
        }
        return knowledgeBaseMapper.selectPage(page, wrapper).convert(this::toVO);
    }

    @Override
    public KnowledgeBaseVO detail(Long kbId) {
        return toVO(requireKb(kbId));
    }

    @Override
    public KnowledgeBaseVO create(KnowledgeBaseSaveRequest request) {
        ensureNameAvailable(request.getName().trim(), null);
        LyKb kb = new LyKb();
        kb.setId(idGenerator.nextId());
        kb.setKbCode(request.getKbCode().trim());
        kb.setName(request.getName().trim());
        kb.setDescription(request.getDescription());
        kb.setQdrantCollection(request.getQdrantCollection().trim());
        kb.setEmbeddingModel(request.getEmbeddingModel().trim());
        kb.setEmbeddingDimension(request.getEmbeddingDimension());
        kb.setChunkSize(request.getChunkSize());
        kb.setChunkOverlap(request.getChunkOverlap());
        kb.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        kb.setIsDeleted(0);
        knowledgeBaseMapper.insert(kb);
        return toVO(kb);
    }

    @Override
    public KnowledgeBaseVO update(Long kbId, KnowledgeBaseSaveRequest request) {
        LyKb kb = requireKb(kbId);
        ensureNameAvailable(request.getName().trim(), kbId);
        kb.setKbCode(request.getKbCode().trim());
        kb.setName(request.getName().trim());
        kb.setDescription(request.getDescription());
        kb.setQdrantCollection(request.getQdrantCollection().trim());
        kb.setEmbeddingModel(request.getEmbeddingModel().trim());
        kb.setEmbeddingDimension(request.getEmbeddingDimension());
        kb.setChunkSize(request.getChunkSize());
        kb.setChunkOverlap(request.getChunkOverlap());
        kb.setStatus(request.getStatus() == null ? kb.getStatus() : request.getStatus());
        knowledgeBaseMapper.updateById(kb);
        return toVO(kb);
    }

    @Override
    public void delete(Long kbId) {
        requireKb(kbId);
        knowledgeBaseMapper.deleteById(kbId);
    }

    private LyKb requireKb(Long kbId) {
        LyKb kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || Integer.valueOf(1).equals(kb.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return kb;
    }

    private void ensureNameAvailable(String name, Long currentId) {
        LyKb existing = knowledgeBaseMapper.selectOne(new LambdaQueryWrapper<LyKb>()
                .eq(LyKb::getName, name)
                .eq(LyKb::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BizException(ErrorCode.KB_NAME_ALREADY_EXISTS);
        }
    }

    private KnowledgeBaseVO toVO(LyKb kb) {
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        vo.setId(kb.getId());
        vo.setKbCode(kb.getKbCode());
        vo.setName(kb.getName());
        vo.setDescription(kb.getDescription());
        vo.setQdrantCollection(kb.getQdrantCollection());
        vo.setEmbeddingModel(kb.getEmbeddingModel());
        vo.setEmbeddingDimension(kb.getEmbeddingDimension());
        vo.setChunkSize(kb.getChunkSize());
        vo.setChunkOverlap(kb.getChunkOverlap());
        vo.setStatus(kb.getStatus());
        vo.setCreatedAt(kb.getCreatedAt());
        return vo;
    }
}
