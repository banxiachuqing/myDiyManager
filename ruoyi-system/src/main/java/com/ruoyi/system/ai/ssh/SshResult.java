package com.ruoyi.system.ai.ssh;

public class SshResult {
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final long elapsedMs;

    public SshResult(int exitCode, String stdout, String stderr, long elapsedMs) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.elapsedMs = elapsedMs;
    }

    public int getExitCode() { return exitCode; }
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public long getElapsedMs() { return elapsedMs; }

    public boolean isSuccess() { return exitCode == 0; }
}
