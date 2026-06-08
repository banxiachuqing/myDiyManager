package com.ruoyi.system.ai.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.ai.domain.AiMessage;
import com.ruoyi.system.ai.domain.AiSession;
import com.ruoyi.system.ai.domain.bo.ChatSendBo;
import com.ruoyi.system.ai.mapper.AiCommandMapper;
import com.ruoyi.system.ai.service.IChatService;
import com.ruoyi.system.ai.service.ISessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/ai/chat")
public class AiChatController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final IChatService chatService;
    private final ISessionService sessionService;
    private final AiCommandMapper commandMapper;

    @Autowired
    public AiChatController(IChatService chatService, ISessionService sessionService, AiCommandMapper commandMapper) {
        this.chatService = chatService;
        this.sessionService = sessionService;
        this.commandMapper = commandMapper;
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:send')")
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@RequestBody ChatSendBo bo) {
        SysUser u = getLoginUser().getUser();
        // 4 分钟超时，参考 RuoYi chat 模块
        SseEmitter emitter = new SseEmitter(4L * 60 * 1000);
        emitter.onCompletion(() -> {});
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError(e -> {});

        long t0 = System.currentTimeMillis();
        AtomicBoolean firstChunk = new AtomicBoolean(true);

        // 异步线程中执行 LLM 流式调用（参考 RuoYi chat 模块 ThreadUtil.execute 模式），
        // controller 立即 return emitter，Spring MVC 自动切到 async 模式，
        // emitter.send() 真正逐帧 flush 到客户端。
        log.info("===[SSE] send() entered, thread={}, sessionId={}, contentLen={}",
            Thread.currentThread().getName(), bo.getSessionId(),
            bo.getContent() == null ? 0 : bo.getContent().length());

        ThreadUtil.execute(() -> {
            long streamStart = System.currentTimeMillis();
            int[] chunkCount = {0};
            log.info("===[SSE] async block started, thread={}", Thread.currentThread().getName());
            try {
                String hostIds = bo.getHostIds() == null ? null
                    : bo.getHostIds().toString().replaceAll("[\\[\\] ]", "");

                // 1. 会话
                AiSession session = sessionService.getOrCreate(bo.getSessionId(), u.getUserId(), bo.getTabType(), hostIds);

                // 2. 落用户消息
                AiMessage userMsg = new AiMessage();
                userMsg.setMessageId(UUID.randomUUID().toString());
                userMsg.setSessionId(session.getSessionId());
                userMsg.setRole("0");
                userMsg.setContent(bo.getContent());
                userMsg.setStatus("1");
                userMsg.setCreateTime(new Date());
                sessionService.addMessage(userMsg);

                // 3. 创建 AI 消息占位
                String aiMessageId = UUID.randomUUID().toString();
                AiMessage aiMsg = new AiMessage();
                aiMsg.setMessageId(aiMessageId);
                aiMsg.setSessionId(session.getSessionId());
                aiMsg.setRole("1");
                aiMsg.setContent("");
                aiMsg.setVendorId(bo.getVendorId());
                aiMsg.setStatus("0");
                aiMsg.setCreateTime(new Date());
                sessionService.addMessage(aiMsg);

                // 4. 流式推送 chunk
                final String finalSessionId = session.getSessionId();
                chatService.streamAnswer(bo, aiMsg, delta -> {
                    try {
                        if (delta == null) return;
                        chunkCount[0]++;
                        if (firstChunk.compareAndSet(true, false)) {
                            log.info("===[SSE] first chunk @{}ms, content=[{}]", System.currentTimeMillis() - streamStart, delta);
                            emitter.send(SseEmitter.event().name("first")
                                .data(Collections.singletonMap("elapsedMs", System.currentTimeMillis() - t0)));
                        }
                        emitter.send(SseEmitter.event().name("chunk")
                            .data(Collections.singletonMap("delta", delta)));
                        if (chunkCount[0] % 10 == 0) {
                            log.info("===[SSE] chunk #{} @{}ms, len={}", chunkCount[0], System.currentTimeMillis() - streamStart, delta.length());
                        }
                    } catch (IOException ex) {
                        log.warn("===[SSE] send IOException at chunk#{}, thread={}", chunkCount[0], Thread.currentThread().getName(), ex);
                        emitter.completeWithError(ex);
                    }
                }, cmds -> {
                    try {
                        log.info("===[SSE] onDone called, total chunks={}, elapsed={}ms", chunkCount[0], System.currentTimeMillis() - streamStart);
                        java.util.Map<String, Object> done = new java.util.HashMap<>();
                        done.put("messageId", aiMessageId);
                        done.put("sessionId", finalSessionId);
                        done.put("commands", cmds);
                        emitter.send(SseEmitter.event().name("done").data(done));
                        emitter.complete();
                        log.info("===[SSE] emitter.complete() called, total elapsed={}ms", System.currentTimeMillis() - streamStart);
                    } catch (IOException ex) {
                        log.warn("===[SSE] done IOException", ex);
                        emitter.completeWithError(ex);
                    }
                });
            } catch (Exception e) {
                log.error("===[SSE] async block exception", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:list')")
    @GetMapping("/session/current")
    public AjaxResult current() { return AjaxResult.success(); }

    @PreAuthorize("@ss.hasPermi('ai:chat:list')")
    @PostMapping("/session/new")
    public AjaxResult newSession() { return AjaxResult.success(); }

    @PreAuthorize("@ss.hasPermi('ai:chat:list')")
    @GetMapping("/sessions")
    public AjaxResult listSessions(@org.springframework.web.bind.annotation.RequestParam String tabType) {
        SysUser u = getLoginUser().getUser();
        java.util.List<AiSession> list = sessionService.listByUserAndTab(u.getUserId(), tabType);
        log.info("===[SSE] listSessions userId={} tabType={} count={} first.preview=[{}]",
            u.getUserId(), tabType, list.size(),
            list.isEmpty() ? "(empty)" : list.get(0).getPreview());
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:cmd')")
    @GetMapping("/cmd/list")
    public AjaxResult listCommands(@org.springframework.web.bind.annotation.RequestParam String messageId) {
        return AjaxResult.success(commandMapper.selectByMessageId(messageId));
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:list')")
    @GetMapping("/session/{sessionId}/messages")
    public AjaxResult listMessages(@PathVariable String sessionId) {
        return AjaxResult.success(sessionService.listMessages(sessionId, 200));
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:list')")
    @DeleteMapping("/session/{sessionId}")
    public AjaxResult deleteSession(@PathVariable String sessionId) {
        sessionService.delete(sessionId);
        return AjaxResult.success();
    }
}
