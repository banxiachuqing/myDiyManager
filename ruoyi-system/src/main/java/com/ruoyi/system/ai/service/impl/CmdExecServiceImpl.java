package com.ruoyi.system.ai.service.impl;

import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiCommand;
import com.ruoyi.system.ai.domain.AiHost;
import com.ruoyi.system.ai.domain.vo.CmdDecisionVo;
import com.ruoyi.system.ai.mapper.AiCommandMapper;
import com.ruoyi.system.ai.mapper.AiHostMapper;
import com.ruoyi.system.ai.service.ICmdExecService;
import com.ruoyi.system.ai.ssh.JSchClient;
import com.ruoyi.system.ai.ssh.SshClient;
import com.ruoyi.system.ai.ssh.SshCommand;
import com.ruoyi.system.ai.ssh.SshResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class CmdExecServiceImpl implements ICmdExecService {

    private final AiHostMapper hostMapper;
    private final AiCommandMapper commandMapper;
    private final LlmVendorCryptoService crypto;
    private final SshClient ssh;
    private final Executor executor;

    @Autowired
    public CmdExecServiceImpl(AiHostMapper hostMapper,
                              AiCommandMapper commandMapper,
                              LlmVendorCryptoService crypto,
                              @Qualifier("aiCmdExecExecutor") Executor executor) {
        this.hostMapper = hostMapper;
        this.commandMapper = commandMapper;
        this.crypto = crypto;
        this.ssh = new JSchClient();
        this.executor = executor;
    }

    @Override
    public CmdDecisionVo exec(AiCommand cmd) {
        // 同步执行：保留给内部单元测试或特殊场景；生产推荐 executeAsync
        return doExec(cmd);
    }

    @Override
    public void executeAsync(AiCommand cmd) {
        // 用项目内的 SSH 执行线程池异步跑，主调用线程立即返回，
        // SSH 完成后通过 AiCommandMapper.updateExecResult 回写 DB，前端轮询拿到最终状态
        executor.execute(() -> {
            try {
                doExec(cmd);
            } catch (Exception e) {
                commandMapper.updateExecResult(cmd.getCmdId(), "4", "执行异常：" + e.getMessage(), null);
            }
        });
    }

    private CmdDecisionVo doExec(AiCommand cmd) {
        if (cmd == null || cmd.getCmdId() == null) throw new IllegalArgumentException("命令不存在");
        commandMapper.updateExecResult(cmd.getCmdId(), "1", "执行中…", null);
        String[] hostIdArr = cmd.getTargetHostIds().split(",");
        long t0 = System.currentTimeMillis();
        List<CompletableFuture<SshResult>> futures = new ArrayList<>();
        List<String> validHosts = new ArrayList<>();
        for (String idStr : hostIdArr) {
            Long hostId;
            try { hostId = Long.valueOf(idStr.trim()); } catch (Exception e) { continue; }
            AiHost h = hostMapper.selectById(hostId);
            if (h == null) continue;
            validHosts.add(idStr.trim());
            SshCommand sc = new SshCommand(h.getIp(),
                h.getSshPort() == null ? 22 : h.getSshPort(),
                h.getUsername(), h.getAuthType(),
                "0".equals(h.getAuthType()) ? crypto.decrypt(h.getPasswordCipher()) : crypto.decrypt(h.getPrivateKeyCipher()),
                cmd.getCmdText(), 30);
            futures.add(CompletableFuture.supplyAsync(() -> ssh.exec(sc), executor));
        }
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        int success = 0, failed = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < futures.size(); i++) {
            String hostLabel = validHosts.get(i);
            try {
                SshResult r = futures.get(i).get();
                sb.append(hostLabel).append(" (exit=").append(r.getExitCode()).append(", ").append(r.getElapsedMs()).append("ms)\n");
                sb.append("STDOUT: ").append(r.getStdout()).append('\n');
                if (r.getStderr() != null && !r.getStderr().isEmpty()) sb.append("STDERR: ").append(r.getStderr()).append('\n');
                if (r.isSuccess()) success++; else failed++;
            } catch (Exception e) {
                failed++;
                sb.append(hostLabel).append(" (异常: ").append(e.getMessage()).append(")\n");
            }
        }
        long elapsed = System.currentTimeMillis() - t0;
        String status;
        if (futures.isEmpty()) {
            status = "4";
            sb.append("无可用目标主机（targetHostIds=").append(cmd.getTargetHostIds()).append("）");
        } else if (failed == 0) status = "2";
        else if (success == 0) status = "4";
        else status = "3";
        commandMapper.updateExecResult(cmd.getCmdId(), status, sb.toString(), (int) elapsed);
        CmdDecisionVo vo = new CmdDecisionVo();
        vo.setCmdId(cmd.getCmdId());
        vo.setExecStatus(status);
        vo.setExecResult(sb.toString());
        vo.setElapsedMs(elapsed);
        return vo;
    }
}
