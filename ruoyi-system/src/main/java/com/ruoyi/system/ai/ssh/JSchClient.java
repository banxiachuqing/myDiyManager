package com.ruoyi.system.ai.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 基于 JSch 的 SSH 客户端实现。
 * 每次调用新开 Session，结束立即 disconnect；不维护长连接池（避免状态泄漏与认证复用风险）。
 */
public class JSchClient implements SshClient {

    private static final Logger log = LoggerFactory.getLogger(JSchClient.class);

    @Override
    public SshResult exec(SshCommand cmd) {
        Session session = null;
        ChannelExec channel = null;
        long t0 = System.currentTimeMillis();
        try {
            session = openSession(cmd);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(cmd.getCommand());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channel.setOutputStream(out);
            channel.setErrStream(err);
            channel.connect(cmd.getTimeoutSec() * 1000);

            // 等待命令完成（带超时）
            long deadline = System.currentTimeMillis() + cmd.getTimeoutSec() * 1000L;
            while (!channel.isClosed()) {
                if (System.currentTimeMillis() > deadline) {
                    channel.disconnect();
                    session.disconnect();
                    throw new SshException(SshException.Code.TIMEOUT, "命令执行超时 " + cmd.getTimeoutSec() + "s");
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
            int exit = channel.getExitStatus();
            long elapsed = System.currentTimeMillis() - t0;
            return new SshResult(
                exit,
                out.toString(StandardCharsets.UTF_8.name()),
                err.toString(StandardCharsets.UTF_8.name()),
                elapsed);
        } catch (SshException e) {
            throw e;
        } catch (com.jcraft.jsch.JSchException e) {
            throw mapJSchException(e, cmd);
        } catch (Exception e) {
            throw new SshException(SshException.Code.EXECUTION, "执行异常: " + e.getMessage(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    @Override
    public void ping(SshCommand cmd) {
        Session session = null;
        try {
            session = openSession(cmd);
            // 仅建立连接 + 完成认证，不打开 channel
        } finally {
            if (session != null) session.disconnect();
        }
    }

    private Session openSession(SshCommand cmd) {
        try {
            JSch jsch = new JSch();
            if ("1".equals(cmd.getAuthType())) {
                // 私钥模式：password 字段实际传私钥明文
                jsch.addIdentity(cmd.getUsername(),
                    cmd.getPassword().getBytes(StandardCharsets.UTF_8), null, null);
            }
            Session session = jsch.getSession(cmd.getUsername(), cmd.getIp(), cmd.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,publickey");
            session.setTimeout(cmd.getTimeoutSec() * 1000);
            if ("0".equals(cmd.getAuthType())) {
                session.setPassword(cmd.getPassword());
            }
            session.connect(cmd.getTimeoutSec() * 1000);
            return session;
        } catch (com.jcraft.jsch.JSchException e) {
            throw mapJSchException(e, cmd);
        }
    }

    private SshException mapJSchException(com.jcraft.jsch.JSchException e, SshCommand cmd) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("Auth fail") || msg.contains("invalid privatekey") || msg.contains("USERAUTH fail")) {
            return new SshException(SshException.Code.AUTH, "认证失败: " + msg);
        }
        if (msg.contains("Connection refused") || msg.contains("connect timed out") || msg.contains("UnknownHostException")) {
            return new SshException(SshException.Code.CONNECTION, "主机不可达: " + cmd.getIp() + ":" + cmd.getPort());
        }
        if (msg.contains("timeout")) {
            return new SshException(SshException.Code.TIMEOUT, "连接超时");
        }
        return new SshException(SshException.Code.EXECUTION, "SSH 异常: " + msg, e);
    }
}
