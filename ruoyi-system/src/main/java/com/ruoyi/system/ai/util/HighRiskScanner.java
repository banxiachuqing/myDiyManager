package com.ruoyi.system.ai.util;

import com.ruoyi.system.ai.config.AiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HighRiskScanner {

    private final List<String> keywords;

    @Autowired
    public HighRiskScanner(AiProperties props) {
        this.keywords = parseKeywords(props.getHighRiskKeywords());
    }

    private static List<String> parseKeywords(String csv) {
        if (csv == null || csv.isEmpty()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
            .map(String::trim).map(String::toLowerCase)
            .filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    public boolean isHighRisk(String cmd) {
        if (cmd == null) return false;
        String lower = cmd.toLowerCase();
        for (String k : keywords) {
            if (lower.contains(k)) return true;
        }
        return false;
    }

    HighRiskScanner(List<String> keywords) { this.keywords = keywords; }
}
