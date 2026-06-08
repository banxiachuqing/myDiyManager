package com.ruoyi.system.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ruoyi.system.ai.domain.AiMessage;
import com.ruoyi.system.ai.domain.AiSession;
import com.ruoyi.system.ai.mapper.AiCommandMapper;
import com.ruoyi.system.ai.mapper.AiMessageMapper;
import com.ruoyi.system.ai.mapper.AiSessionMapper;
import com.ruoyi.system.ai.service.ISessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SessionServiceImpl implements ISessionService {

    private final AiSessionMapper sessionMapper;
    private final AiMessageMapper messageMapper;
    private final AiCommandMapper commandMapper;

    @Autowired
    public SessionServiceImpl(AiSessionMapper sessionMapper, AiMessageMapper messageMapper, AiCommandMapper commandMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.commandMapper = commandMapper;
    }

    @Override
    public AiSession create(Long userId, String tabType, String hostIds) {
        AiSession s = new AiSession();
        s.setSessionId(IdUtil.fastSimpleUUID());
        s.setUserId(userId);
        s.setTabType(tabType);
        s.setActiveHostIds(hostIds);
        s.setLastActiveAt(new Date());
        s.setCreateTime(new Date());
        sessionMapper.insert(s);
        return s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSession getOrCreate(String sessionId, Long userId, String tabType, String hostIds) {
        if (sessionId != null && !sessionId.isEmpty()) {
            AiSession existing = sessionMapper.selectById(sessionId);
            if (existing != null) {
                if (hostIds != null) sessionMapper.updateActiveAt(sessionId, new Date(), hostIds);
                existing.setActiveHostIds(hostIds);
                existing.setLastActiveAt(new Date());
                return existing;
            }
        }
        return create(userId, tabType, hostIds);
    }

    @Override
    public AiSession touch(String sessionId, String hostIds) {
        if (sessionId == null) return null;
        sessionMapper.updateActiveAt(sessionId, new Date(), hostIds);
        return sessionMapper.selectById(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMessage(AiMessage msg) {
        if (msg.getCreateTime() == null) msg.setCreateTime(new Date());
        messageMapper.insert(msg);
    }

    @Override
    public List<AiMessage> listMessages(String sessionId, int limit) {
        return messageMapper.selectBySession(sessionId, limit);
    }

    @Override
    public List<AiSession> listByUserAndTab(Long userId, String tabType) {
        return sessionMapper.selectByUserAndTab(userId, tabType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return;
        // 级联删除：commands → messages → session
        commandMapper.deleteBySession(sessionId);
        messageMapper.deleteBySession(sessionId);
        sessionMapper.deleteById(sessionId);
    }
}
