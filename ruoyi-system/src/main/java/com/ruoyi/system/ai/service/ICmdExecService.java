package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.domain.AiCommand;
import com.ruoyi.system.ai.domain.vo.CmdDecisionVo;

public interface ICmdExecService {
    /** 执行单条命令（多主机并发），回写 exec_status / exec_result / exec_ms */
    CmdDecisionVo exec(AiCommand cmd);
    /** 异步执行：立即返回，SSH 在后台线程并发执行，完成后回写 DB */
    void executeAsync(AiCommand cmd);
}
