package com.ruoyi.system.ai.llm;

public class LlmException extends RuntimeException {
    public enum Code { NETWORK, AUTH, RATE_LIMIT, BIZ, INVALID_RESPONSE }
    private final Code code;
    public LlmException(Code code, String msg) { super(msg); this.code = code; }
    public LlmException(Code code, String msg, Throwable t) { super(msg, t); this.code = code; }
    public Code getCode() { return code; }
}
