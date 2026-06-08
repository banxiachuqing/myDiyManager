package com.ruoyi.system.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiCommand;
import com.ruoyi.system.ai.domain.AiMessage;
import com.ruoyi.system.ai.domain.AiVendor;
import com.ruoyi.system.ai.domain.AiHost;
import com.ruoyi.system.ai.domain.bo.ChatSendBo;
import com.ruoyi.system.ai.domain.vo.CmdSummaryVo;
import com.ruoyi.system.ai.llm.ChatChunk;
import com.ruoyi.system.ai.llm.ChatRequest;
import com.ruoyi.system.ai.llm.OpenAiCompatClient;
import com.ruoyi.system.ai.mapper.AiCommandMapper;
import com.ruoyi.system.ai.mapper.AiHostMapper;
import com.ruoyi.system.ai.mapper.AiMessageMapper;
import com.ruoyi.system.ai.mapper.AiVendorMapper;
import com.ruoyi.system.ai.service.IChatService;
import com.ruoyi.system.ai.service.IWhitelistService;
import com.ruoyi.system.ai.util.CmdFingerprint;
import com.ruoyi.system.ai.util.HighRiskScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatServiceImpl implements IChatService {

    private static final Pattern CMD_RE = Pattern.compile("<cmd(?:\\s+host=\"([^\"]+)\")?>([\\s\\S]*?)</cmd>");

    private final AiVendorMapper vendorMapper;
    private final AiMessageMapper messageMapper;
    private final AiCommandMapper commandMapper;
    private final AiHostMapper hostMapper;
    private final LlmVendorCryptoService crypto;
    private final HighRiskScanner scanner;
    private final IWhitelistService whitelistService;

    @Autowired
    public ChatServiceImpl(AiVendorMapper vendorMapper,
                           AiMessageMapper messageMapper,
                           AiCommandMapper commandMapper,
                           AiHostMapper hostMapper,
                           LlmVendorCryptoService crypto,
                           HighRiskScanner scanner,
                           IWhitelistService whitelistService) {
        this.vendorMapper = vendorMapper;
        this.messageMapper = messageMapper;
        this.commandMapper = commandMapper;
        this.hostMapper = hostMapper;
        this.crypto = crypto;
        this.scanner = scanner;
        this.whitelistService = whitelistService;
    }

    @Override
    public void streamAnswer(ChatSendBo bo, AiMessage aiMsg, Consumer<String> onChunk, Consumer<List<CmdSummaryVo>> onDone) throws Exception {
        AiVendor v = vendorMapper.selectById(bo.getVendorId());
        if (v == null) throw new IllegalArgumentException("厂商不存在");
        OpenAiCompatClient client = new OpenAiCompatClient(
            v.getBaseUrl(), crypto.decrypt(v.getApiKeyCipher()), v.getModelName(),
            v.getTimeoutSec() == null ? 30 : v.getTimeoutSec());

        ChatRequest req = new ChatRequest();
        req.setModel(v.getModelName());
        req.setStream(true);
        req.setMaxTokens(v.getMaxTokens());
        req.setTemperature(v.getTemperature() == null ? 0.7 : v.getTemperature().doubleValue());

        // 构建 messages：system + 历史对话 + 当前 user
        // 关键：必须维护多轮上下文，否则 LLM 看不到上文，每次 send 是孤立的"问一句答一句"
        List<Map<String, String>> messages = new ArrayList<>();
        String systemPrompt = buildSystemPrompt(bo);
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(msgOf("system", systemPrompt));
        }
        // 加载历史对话（最多 200 条 ≈ 全量），role 映射
        if (bo.getSessionId() != null && !bo.getSessionId().isEmpty()) {
            // 命令总结模式（bo.cmdId 非空）不加载历史：上下文就是执行结果 + 总结指令
            if (bo.getCmdId() == null || bo.getCmdId().isEmpty()) {
                List<AiMessage> history = messageMapper.selectBySession(bo.getSessionId(), 200);
                for (AiMessage m : history) {
                    if (m.getContent() == null || m.getContent().isEmpty()) continue;
                    String role = "0".equals(m.getRole()) ? "user" : "assistant";
                    messages.add(msgOf(role, m.getContent()));
                }
            }
        }
        // 当前 user 消息（命令总结模式下包含执行结果回灌）
        String userContent = buildUserContent(bo);
        messages.add(msgOf("user", userContent));
        req.setMessages(messages);

        StringBuilder content = new StringBuilder();
        client.chatStream(req, chunk -> {
            if (chunk.getDelta() != null) {
                content.append(chunk.getDelta());
                onChunk.accept(chunk.getDelta());
            }
        });

        // 流式结束：落 AI 消息 + 解析命令
        aiMsg.setContent(content.toString());
        aiMsg.setStatus("1");
        // 关键修复：必须把 content 也写回 DB，否则刷新后 listMessages 查到的 AI 消息 content 永远是空
        messageMapper.updateContentAndStatus(aiMsg.getMessageId(), content.toString(), "1");

        String defaultHostIds = bo.getHostIds() == null ? "" : joinHostIds(bo.getHostIds());
        List<CmdSummaryVo> cmds = parseAndPersist(aiMsg, defaultHostIds);
        onDone.accept(cmds);
    }

    @Override
    public List<CmdSummaryVo> parseAndPersist(AiMessage aiMsg, String defaultHostIds) {
        List<CmdSummaryVo> out = new ArrayList<>();
        if (aiMsg.getContent() == null) return out;
        Matcher m = CMD_RE.matcher(aiMsg.getContent());
        while (m.find()) {
            String hostTag = m.group(1);
            String text = m.group(2).trim();
            // 关键修复：把 LLM 输出的 host="主机名" 反查为 hostId 列表，
            // 因为 doExec 内部用 Long.valueOf() 解析 targetHostIds
            // 1) 优先用 LLM 指定的 hostTag（按主机名查表）
            // 2) hostTag 查不到或没指定时回退到 defaultHostIds（前端已选主机的 hostId 列表）
            String resolvedHostIds = resolveTargetHostIds(hostTag, defaultHostIds);
            String fp = CmdFingerprint.compute(text);
            String highRisk = scanner.isHighRisk(text) ? "1" : "0";
            boolean inWhitelist = whitelistService.contains(aiMsg.getSessionId(), fp);

            AiCommand ac = new AiCommand();
            ac.setCmdId(IdUtil.fastSimpleUUID());
            ac.setMessageId(aiMsg.getMessageId());
            ac.setCmdText(text);
            ac.setTargetHostIds(resolvedHostIds);
            ac.setCmdFingerprint(fp);
            ac.setIsHighRisk(highRisk);
            ac.setDecision("0");
            ac.setExecStatus("0");
            ac.setCreateTime(new Date());
            commandMapper.insert(ac);

            CmdSummaryVo v = new CmdSummaryVo();
            v.setCmdId(ac.getCmdId());
            v.setCmdText(text);
            v.setTargetHostIds(resolvedHostIds);
            v.setCmdFingerprint(fp);
            v.setIsHighRisk(highRisk);
            v.setExecStatus("0");
            v.setDecision("0");
            v.setInWhitelist(inWhitelist);
            out.add(v);
        }
        return out;
    }

    /**
     * 把 LLM 输出的 <cmd host="主机名"> 解析为 hostId 列表：
     *  - hostTag 非空 → 查 AiHost by host_name，转 hostId（支持"主机名"或"主机名,主机名"多目标）
     *  - hostTag 查不到或为空 → 落到 defaultHostIds（前端 bo.hostIds 拼接出的 hostId 串）
     */
    private String resolveTargetHostIds(String hostTag, String defaultHostIds) {
        if (hostTag == null || hostTag.trim().isEmpty()) return defaultHostIds == null ? "" : defaultHostIds;
        StringBuilder sb = new StringBuilder();
        for (String name : hostTag.split(",")) {
            String n = name.trim();
            if (n.isEmpty()) continue;
            AiHost h = hostMapper.selectByHostName(n);
            if (h == null) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(h.getHostId());
        }
        // 全是空或全查不到 → 回退默认
        if (sb.length() == 0) return defaultHostIds == null ? "" : defaultHostIds;
        return sb.toString();
    }

    private static Map<String, String> msgOf(String role, String content) {
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    /**
     * 构造 user 上下文：
     *  - 普通模式：直接用 bo.content
     *  - 命令总结模式（bo.cmdId 非空）：从 ai_command 查执行结果，拼成"原问题 + 命令 + 输出"
     *    让 LLM 拿到真实数据，给出结论式回答
     */
    private String buildUserContent(ChatSendBo bo) {
        if (bo.getCmdId() == null || bo.getCmdId().isEmpty()) {
            return bo.getContent() == null ? "" : bo.getContent();
        }
        AiCommand cmd = commandMapper.selectById(bo.getCmdId());
        String userAsk = bo.getContent() == null ? "请根据命令执行结果给出结论" : bo.getContent();
        if (cmd == null) {
            return userAsk + "\n\n（未找到命令 " + bo.getCmdId() + "）";
        }
        // host 名展示
        String hosts = cmd.getTargetHostIds() == null ? "" : cmd.getTargetHostIds();
        String execResult = cmd.getExecResult() == null ? "（无输出）" : cmd.getExecResult();
        String statusLabel;
        switch (cmd.getExecStatus() == null ? "" : cmd.getExecStatus()) {
            case "2": statusLabel = "成功"; break;
            case "3": statusLabel = "部分成功"; break;
            case "4": statusLabel = "失败"; break;
            case "5": statusLabel = "超时"; break;
            default: statusLabel = "已执行（状态 " + cmd.getExecStatus() + "）"; break;
        }
        return userAsk + "\n\n" +
               "【命令执行结果】\n" +
               "目标主机：" + hosts + "\n" +
               "命令：`" + cmd.getCmdText() + "`\n" +
               "状态：" + statusLabel + "（" + cmd.getExecMs() + "ms）\n" +
               "输出：\n```\n" + execResult + "\n```";
    }

    /**
     * 构造 system prompt：
     *  - tabType='0'（智能问答）：直接问答，无系统指令
     *  - tabType='1'（主机运维）：注入运维指令 + 可用主机上下文，强制 LLM 用 <cmd> 块输出
     * 关键：必须把"tabType"和"hostIds"告诉 LLM，否则它会当普通对话回答
     */
    private String buildSystemPrompt(ChatSendBo bo) {
        if (!"1".equals(bo.getTabType())) return null;
        List<Long> ids = bo.getHostIds();
        StringBuilder hosts = new StringBuilder();
        if (ids != null && !ids.isEmpty()) {
            for (AiHost h : hostMapper.selectBatchIds(ids)) {
                if (h == null) continue;
                if (hosts.length() > 0) hosts.append("、");
                hosts.append(h.getHostName()).append('(').append(h.getHostId()).append('@').append(h.getIp()).append(')');
            }
        }
        String hostCtx = hosts.length() == 0 ? "（用户未选主机，请提示用户先在工具栏选择目标主机）" : hosts.toString();
        return "你是服务器运维助手。当前 tab=主机运维，可用主机列表：" + hostCtx + "。\n" +
               "你必须针对用户提出的运维诉求，输出可在 SSH 上执行的具体 shell 命令。\n" +
               "命令必须用 <cmd host=\"主机名\"> 命令内容 </cmd> 包裹。host 属性必须是上面列表里的主机名，不指定时省略 host 属性。\n" +
               "多条命令用多个 <cmd>...</cmd> 块；输出格式：先简要说明（1-2 句），再列命令；不要输出与运维无关的内容。";
    }

    private static String joinHostIds(List<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(ids.get(i));
        }
        return sb.toString();
    }
}
