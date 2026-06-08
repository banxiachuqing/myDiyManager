package com.ruoyi.system.ai.crypto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.ruoyi.system.ai.config.AiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * LLM 厂商 apiKey 加解密服务（基于 Hutool AES-128-CBC）。
 * 主密钥来自 AiProperties.cryptoKey（application.yml: ai.crypto.key）。
 */
@Service
public class LlmVendorCryptoService {

    private final AES aes;

    @Autowired
    public LlmVendorCryptoService(AiProperties props) {
        if (StrUtil.isBlank(props.getCryptoKey())) {
            throw new IllegalStateException("ai.crypto.key 未配置（application.yml）");
        }
        // AES 密钥长度 16 字节
        byte[] keyBytes = props.getCryptoKey().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16) {
            throw new IllegalStateException("ai.crypto.key 必须是 16 字节（AES-128）");
        }
        this.aes = SecureUtil.aes(keyBytes);
    }

    public String encrypt(String plain) {
        if (StrUtil.isBlank(plain)) {
            throw new IllegalArgumentException("明文不能为空");
        }
        return aes.encryptHex(plain);
    }

    public String decrypt(String cipher) {
        if (StrUtil.isBlank(cipher)) {
            throw new IllegalArgumentException("密文不能为空");
        }
        return aes.decryptStr(cipher);
    }

    public String mask(String plain) {
        if (StrUtil.isBlank(plain) || plain.length() <= 4) {
            return "****";
        }
        return "****" + plain.substring(plain.length() - 4);
    }
}
