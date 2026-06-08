package com.ruoyi.system.ai.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("AiHost")
public class AiHost extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long hostId;
    @Excel(name = "主机名")
    private String hostName;
    @Excel(name = "IP")
    private String ip;
    private Integer sshPort;
    private String sshProtocol;
    @Excel(name = "用户名")
    private String username;
    /** 0口令 1私钥 */
    @Excel(name = "认证", readConverterExp = "0=口令,1=私钥")
    private String authType;

    @JsonIgnore private String passwordCipher;
    @JsonIgnore private String privateKeyCipher;

    private Long deptId;
    /** 0在线 1离线 2未知 */
    @Excel(name = "状态", readConverterExp = "0=在线,1=离线,2=未知")
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
    public String getPasswordCipher() { return passwordCipher; }
    public void setPasswordCipher(String passwordCipher) { this.passwordCipher = passwordCipher; }
    public String getPrivateKeyCipher() { return privateKeyCipher; }
    public void setPrivateKeyCipher(String privateKeyCipher) { this.privateKeyCipher = privateKeyCipher; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getLastTestAt() { return lastTestAt; }
    public void setLastTestAt(Date lastTestAt) { this.lastTestAt = lastTestAt; }
    public String getLastTestMsg() { return lastTestMsg; }
    public void setLastTestMsg(String lastTestMsg) { this.lastTestMsg = lastTestMsg; }
}
