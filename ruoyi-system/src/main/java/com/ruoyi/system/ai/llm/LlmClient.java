package com.ruoyi.system.ai.llm;

import java.util.List;
import java.util.function.Consumer;

/**
 * LLM 客户端抽象。实现类负责对接具体协议（OpenAI 兼容、Azure、文心等）。
 */
public interface LlmClient {

    /** 流式 chat；handler 接收 ChatChunk 增量回调 */
    void chatStream(ChatRequest req, Consumer<ChatChunk> handler) throws LlmException;

    /** 非流式 chat */
    ChatResponse chat(ChatRequest req) throws LlmException;

    /** 探测厂商可用模型清单；用于"测试连通"流程 */
    List<String> listModels() throws LlmException;
}
