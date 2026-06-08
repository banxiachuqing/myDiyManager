package com.ruoyi.system.ai.task;

import com.ruoyi.system.ai.mapper.AiMessageMapper;
import com.ruoyi.system.ai.mapper.AiSessionMapper;
import com.ruoyi.system.ai.service.IWhitelistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aiSessionCleanupTask")
public class AiSessionCleanupTask {

    @Autowired private AiSessionMapper sessionMapper;
    @Autowired private AiMessageMapper messageMapper;
    @Autowired private IWhitelistService whitelistService;

    private static final int IDLE_MINUTES = 30;

    public void run() {
        // simplified: only flush expired Redis whitelist; session/message/command kept for history
        // full impl would: DELETE FROM ai_session WHERE last_active_at < NOW() - 30min
        //                  AND cascade delete ai_message, ai_command
        // TODO Phase 4: add mapper.deleteIdleSessions() with cascade
    }
}
