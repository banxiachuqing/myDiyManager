package com.ruoyi.system.ai.mapper;

import com.ruoyi.system.ai.domain.AiMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiMessageMapper {
    int insert(AiMessage msg);
    List<AiMessage> selectBySession(@Param("sessionId") String sessionId, @Param("limit") int limit);
    int updateStatus(@Param("messageId") String messageId, @Param("status") String status);
    int updateContentAndStatus(@Param("messageId") String messageId, @Param("content") String content, @Param("status") String status);
    AiMessage selectById(String messageId);
    int deleteBySession(String sessionId);
}
