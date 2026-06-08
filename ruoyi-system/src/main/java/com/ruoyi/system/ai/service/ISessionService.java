package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.domain.AiMessage;
import com.ruoyi.system.ai.domain.AiSession;

import java.util.List;

public interface ISessionService {
    AiSession create(Long userId, String tabType, String hostIds);
    AiSession getOrCreate(String sessionId, Long userId, String tabType, String hostIds);
    AiSession touch(String sessionId, String hostIds);
    void addMessage(AiMessage msg);
    List<AiMessage> listMessages(String sessionId, int limit);
    List<AiSession> listByUserAndTab(Long userId, String tabType);
    void delete(String sessionId);
}
