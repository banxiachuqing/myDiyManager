package com.ruoyi.system.ai.ssh;

public class SshCommand {
    private final String ip;
    private final int port;
    private final String username;
    private final String authType;   // "0"=口令, "1"=私钥
    private final String password;  // 口令或私钥明文
    private final String command;
    private final int timeoutSec;

    public SshCommand(String ip, int port, String username, String authType, String password, String command, int timeoutSec) {
        this.ip = ip;
        this.port = port <= 0 ? 22 : port;
        this.username = username;
        this.authType = authType;
        this.password = password;
        this.command = command;
        this.timeoutSec = timeoutSec <= 0 ? 30 : timeoutSec;
    }

    public String getIp() { return ip; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
    public String getAuthType() { return authType; }
    public String getPassword() { return password; }
    public String getCommand() { return command; }
    public int getTimeoutSec() { return timeoutSec; }
}
