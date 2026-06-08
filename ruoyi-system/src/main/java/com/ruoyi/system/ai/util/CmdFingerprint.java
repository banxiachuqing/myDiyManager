package com.ruoyi.system.ai.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

@Component
public class CmdFingerprint {

    private static final Set<String> HIGH_RISK_VERBS = new HashSet<>(Arrays.asList(
        "rm", "mkfs", "dd", "shutdown", "reboot", "iptables", "userdel", "kill"
    ));

    private static final Map<String, String> TARGET_MAP = new HashMap<>();
    static {
        TARGET_MAP.put("df", "fs_usage");
        TARGET_MAP.put("du", "fs_usage");
        TARGET_MAP.put("free", "memory");
        TARGET_MAP.put("top", "process");
        TARGET_MAP.put("ps", "process");
        TARGET_MAP.put("cat", "read_file");
        TARGET_MAP.put("tail", "read_file");
        TARGET_MAP.put("head", "read_file");
        TARGET_MAP.put("rm", "delete_file");
        TARGET_MAP.put("mv", "move_file");
        TARGET_MAP.put("cp", "copy_file");
        TARGET_MAP.put("chmod", "change_perm");
        TARGET_MAP.put("chown", "change_owner");
        TARGET_MAP.put("systemctl", "service");
        TARGET_MAP.put("service", "service");
        TARGET_MAP.put("docker", "container");
        TARGET_MAP.put("kubectl", "k8s");
    }

    public static String compute(String cmd) {
        if (StrUtil.isBlank(cmd)) return "";
        String[] tokens = cmd.trim().split("\\s+");
        if (tokens.length == 0) return "";
        String verb = tokens[0];
        String target = TARGET_MAP.getOrDefault(verb, "other");
        return SecureUtil.md5(verb + ":" + target);
    }

    public static boolean isHighRiskVerb(String verb) {
        return HIGH_RISK_VERBS.contains(verb);
    }
}
