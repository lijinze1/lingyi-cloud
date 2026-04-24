package com.lingyi.ai.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.ai.rag.entity.LyKbChunk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<LyKbChunk> {
}
