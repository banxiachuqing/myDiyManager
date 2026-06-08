package com.ruoyi.system.ai.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.ai.domain.AiCommand;
import com.ruoyi.system.ai.domain.AiMessage;
import com.ruoyi.system.ai.domain.bo.CmdDecisionBo;
import com.ruoyi.system.ai.domain.vo.CmdDecisionVo;
import com.ruoyi.system.ai.mapper.AiCommandMapper;
import com.ruoyi.system.ai.mapper.AiMessageMapper;
import com.ruoyi.system.ai.service.ICmdExecService;
import com.ruoyi.system.ai.service.IWhitelistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/chat/cmd")
public class AiOpsController extends BaseController {

    private final ICmdExecService execService;
    private final IWhitelistService whitelistService;
    private final AiCommandMapper commandMapper;
    private final AiMessageMapper messageMapper;

    @Autowired
    public AiOpsController(ICmdExecService execService, IWhitelistService whitelistService, AiCommandMapper commandMapper, AiMessageMapper messageMapper) {
        this.execService = execService;
        this.whitelistService = whitelistService;
        this.commandMapper = commandMapper;
        this.messageMapper = messageMapper;
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:cmd')")
    @PostMapping("/decision")
    public AjaxResult decide(@RequestBody CmdDecisionBo bo) {
        AiCommand cmd = commandMapper.selectById(bo.getCmdId());
        if (cmd == null) return AjaxResult.error("命令不存在");
        if (!"0".equals(cmd.getDecision())) return AjaxResult.error("该命令已裁决");
        commandMapper.updateDecision(bo.getCmdId(), bo.getDecision(), getUsername());

        // "本次会话内允许" 加入白名单（key 与 parseAndPersist.contains 一致：sessionId + fingerprint）
        if ("2".equals(bo.getDecision())) {
            AiMessage aiMsg = messageMapper.selectById(cmd.getMessageId());
            if (aiMsg != null) {
                whitelistService.add(aiMsg.getSessionId(), cmd.getCmdFingerprint(), 1800);
            }
        }
        if ("3".equals(bo.getDecision())) {
            return AjaxResult.success("已拒绝");
        }
        // 异步执行：立即返回执行中状态，SSH 在后台线程跑，前端轮询 cmd/list 拿到最终结果
        execService.executeAsync(cmd);
        CmdDecisionVo vo = new CmdDecisionVo();
        vo.setCmdId(cmd.getCmdId());
        vo.setExecStatus("1");
        vo.setExecResult("执行中…");
        return AjaxResult.success(vo);
    }
}
