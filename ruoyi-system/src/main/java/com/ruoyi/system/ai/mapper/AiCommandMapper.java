package com.ruoyi.system.ai.mapper;

import com.ruoyi.system.ai.domain.AiCommand;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiCommandMapper {
    int insert(AiCommand cmd);
    AiCommand selectById(String cmdId);
    List<AiCommand> selectByMessageId(String messageId);
    int updateDecision(@Param("cmdId") String cmdId, @Param("decision") String decision, @Param("user") String user);
    int updateExecResult(@Param("cmdId") String cmdId, @Param("execStatus") String execStatus, @Param("execResult") String execResult, @Param("execMs") Integer execMs);
    int deleteBySession(String sessionId);
}
