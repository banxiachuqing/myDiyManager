package com.ruoyi.system.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

public class AiHostVo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long hostId;
    private String hostName;
    private String ip;
    private Integer sshPort;
    private String sshProtocol;
    private String username;
    private String authType;
    @JsonIgnore private String password;
    @JsonIgnore private String privateKey;
    private Long deptId;
    private String status;
    private Date lastTestAt;
    private String lastTestMsg;

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Integer getSshPort() { return sshPort; }
    public void setSshPort(Integer sshPort) { this.sshPort = sshPort; }
    public String getSshProtocol() { return sshProtocol; }
    public void setSshProtocol(String sshProtocol) { this.sshProtocol = sshProtocol; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getLastTestAt() { return lastTestAt; }
    public void setLastTestAt(Date lastTestAt) { this.lastTestAt = lastTestAt; }
    public String getLastTestMsg() { return lastTestMsg; }
    public void setLastTestMsg(String lastTestMsg) { this.lastTestMsg = lastTestMsg; }
}
