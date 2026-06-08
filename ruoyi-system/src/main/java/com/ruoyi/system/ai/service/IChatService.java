package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.domain.AiMessage;
import com.ruoyi.system.ai.domain.bo.ChatSendBo;
import com.ruoyi.system.ai.domain.vo.CmdSummaryVo;

import java.util.List;
import java.util.function.Consumer;

public interface IChatService {
    /** SSE 编排：调 LLM 流式输出，并把 chunk 桥接到 emitter；同时把 AI 消息和解析的命令落库 */
    void streamAnswer(ChatSendBo bo, AiMessage aiMsg, Consumer<String> onChunk, Consumer<List<CmdSummaryVo>> onDone) throws Exception;
    /** 解析 AI 答复中的 <cmd host="...">...</cmd> 块并落 ai_command 表 */
    List<CmdSummaryVo> parseAndPersist(AiMessage aiMsg, String defaultHostIds);
}
