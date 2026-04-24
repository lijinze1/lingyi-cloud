package com.lingyi.ai.chatbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.ai.chatbot.entity.LyChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<LyChatMessage> {
}
