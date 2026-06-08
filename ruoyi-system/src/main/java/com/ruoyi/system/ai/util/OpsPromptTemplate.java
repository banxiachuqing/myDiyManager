package com.ruoyi.system.ai.util;

import com.ruoyi.system.ai.domain.AiHost;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpsPromptTemplate {

    public static final String TEMPLATE = ""
        + "你是 myDiyManager 系统的运维助手。当前用户：%s，所属部门：%s。\n"
        + "目标主机清单：\n%s\n\n"
        + "约束规则：\n"
        + "1. 回答必须分两部分：自然语言解释 + <cmd host=\"hostId\">命令</cmd> 包裹的可执行命令块\n"
        + "2. 命令必须只读或低风险（df / free / top / ps / cat / tail / systemctl status 等）\n"
        + "3. 禁止破坏性命令（rm -rf /、mkfs、dd of=/dev/、iptables -F、shutdown 等）\n"
        + "4. 一台主机一条 <cmd> 块，跨主机用多个 <cmd> 包裹\n"
        + "5. 不要输出命令执行结果预测；只输出命令\n"
        + "6. 不要解释命令含义\n"
        + "7. 高危操作必须先在自然语言部分提示\"以下操作存在风险\"\n";

    public String build(String userName, String deptName, List<AiHost> hosts) {
        String hostList = hosts.stream()
            .map(h -> String.format("- hostId=%d 主机名=%s IP=%s 认证=%s",
                h.getHostId(), h.getHostName(), h.getIp(),
                "0".equals(h.getAuthType()) ? "口令" : "私钥"))
            .collect(Collectors.joining("\n"));
        return String.format(TEMPLATE, userName, deptName, hostList);
    }
}
