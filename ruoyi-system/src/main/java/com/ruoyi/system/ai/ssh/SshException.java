package com.ruoyi.system.ai.ssh;

public class SshException extends RuntimeException {
    public enum Code { CONNECTION, AUTH, TIMEOUT, EXECUTION }
    private final Code code;
    public SshException(Code code, String msg) { super(msg); this.code = code; }
    public SshException(Code code, String msg, Throwable t) { super(msg, t); this.code = code; }
    public Code getCode() { return code; }
}
