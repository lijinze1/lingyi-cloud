package com.lingyi.ai.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.ai.diagnosis.entity.LyPrompt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptMapper extends BaseMapper<LyPrompt> {
}
