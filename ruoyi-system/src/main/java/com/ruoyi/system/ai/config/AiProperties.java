package com.ruoyi.system.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模块配置（application.yml 前缀 ai.*）
 *   ai:
 *     crypto-key: 0123456789abcdef   # 16 字节 AES-128
 *     chat:
 *       default-timeout-sec: 60
 *       max-tokens: 2048
 *       temperature: 0.7
 *     test:
 *       consume-quota: false         # 测试连通是否消耗厂商额度
 *     high-risk-keywords: rm -rf /,chmod 777,dd of=/dev/,mkfs,shutdown,reboot,iptables -F,userdel,kill -9 1
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String cryptoKey;
    private Chat chat = new Chat();
    private Test test = new Test();
    private String highRiskKeywords = "rm -rf /,chmod 777,dd of=/dev/,mkfs,shutdown,reboot,iptables -F,userdel,kill -9 1";

    public String getCryptoKey() { return cryptoKey; }
    public void setCryptoKey(String cryptoKey) { this.cryptoKey = cryptoKey; }
    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }
    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }
    public String getHighRiskKeywords() { return highRiskKeywords; }
    public void setHighRiskKeywords(String highRiskKeywords) { this.highRiskKeywords = highRiskKeywords; }

    public static class Chat {
        private int defaultTimeoutSec = 60;
        private int maxTokens = 2048;
        private double temperature = 0.7;
        public int getDefaultTimeoutSec() { return defaultTimeoutSec; }
        public void setDefaultTimeoutSec(int defaultTimeoutSec) { this.defaultTimeoutSec = defaultTimeoutSec; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
    }

    public static class Test {
        private boolean consumeQuota = false;
        public boolean isConsumeQuota() { return consumeQuota; }
        public void setConsumeQuota(boolean consumeQuota) { this.consumeQuota = consumeQuota; }
    }
}
