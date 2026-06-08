package com.ruoyi.system.ai.crypto;

import com.ruoyi.system.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LlmVendorCryptoServiceTest {

    private LlmVendorCryptoService newService(String key) {
        AiProperties p = new AiProperties();
        p.setCryptoKey(key);
        return new LlmVendorCryptoService(p);
    }

    @Test
    void encrypt_decrypt_roundtrip() {
        LlmVendorCryptoService crypto = newService("0123456789abcdef");
        String plain = "sk-1234567890abcdef";
        String cipher = crypto.encrypt(plain);
        assertNotEquals(plain, cipher);
        assertEquals(plain, crypto.decrypt(cipher));
    }

    @Test
    void mask_returns_last_four() {
        LlmVendorCryptoService crypto = newService("0123456789abcdef");
        assertEquals("****cdef", crypto.mask("sk-1234567890abcdef"));
    }

    @Test
    void mask_handles_short_input() {
        LlmVendorCryptoService crypto = newService("0123456789abcdef");
        assertEquals("****", crypto.mask("abcd"));
    }

    @Test
    void encrypt_rejectsBlank() {
        LlmVendorCryptoService crypto = newService("0123456789abcdef");
        assertThrows(IllegalArgumentException.class, () -> crypto.encrypt(""));
    }

    @Test
    void wrongKeyLength_failsAtConstruction() {
        AiProperties p = new AiProperties();
        p.setCryptoKey("short");
        assertThrows(IllegalStateException.class, () -> new LlmVendorCryptoService(p));
    }
}
