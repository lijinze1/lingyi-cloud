package com.lingyi.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.ai.prompt.entity.LyPromptVersion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptVersionMapper extends BaseMapper<LyPromptVersion> {
}
