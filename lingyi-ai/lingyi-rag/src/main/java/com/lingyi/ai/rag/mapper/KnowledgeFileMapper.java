package com.lingyi.ai.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.ai.rag.entity.LyKbFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeFileMapper extends BaseMapper<LyKbFile> {
}
