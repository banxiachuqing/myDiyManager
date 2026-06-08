package com.ruoyi.system.ai.mapper;

import com.ruoyi.system.ai.domain.AiSession;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface AiSessionMapper {
    AiSession selectById(String sessionId);
    int insert(AiSession session);
    int updateActiveAt(@Param("sessionId") String sessionId, @Param("lastActiveAt") Date lastActiveAt, @Param("hostIds") String hostIds);
    int deleteById(String sessionId);
    List<AiSession> selectByUserAndTab(@Param("userId") Long userId, @Param("tabType") String tabType);
}
