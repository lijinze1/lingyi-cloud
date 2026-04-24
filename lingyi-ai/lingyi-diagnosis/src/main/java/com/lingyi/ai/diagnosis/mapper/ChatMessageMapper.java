package com.lingyi.ai.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.ai.diagnosis.entity.LyChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<LyChatMessage> {
}
