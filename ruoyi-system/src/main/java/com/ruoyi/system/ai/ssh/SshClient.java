package com.ruoyi.system.ai.ssh;

public interface SshClient {
    /** 执行命令并返回结果。连接/认证/超时分别抛 SshException.Code */
    SshResult exec(SshCommand cmd) throws SshException;

    /** 仅做 TCP+SSH 握手+认证，不执行命令；用于"测试连接"流程 */
    void ping(SshCommand cmd) throws SshException;
}
