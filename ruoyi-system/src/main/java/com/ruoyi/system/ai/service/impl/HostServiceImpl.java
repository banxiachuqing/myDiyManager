package com.ruoyi.system.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiHost;
import com.ruoyi.system.ai.domain.bo.AiHostBo;
import com.ruoyi.system.ai.domain.vo.AiHostVo;
import com.ruoyi.system.ai.domain.vo.TestResultVo;
import com.ruoyi.system.ai.mapper.AiHostMapper;
import com.ruoyi.system.ai.service.IHostService;
import com.ruoyi.system.ai.ssh.JSchClient;
import com.ruoyi.system.ai.ssh.SshClient;
import com.ruoyi.system.ai.ssh.SshCommand;
import com.ruoyi.system.ai.ssh.SshException;
import com.ruoyi.system.ai.ssh.SshResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HostServiceImpl implements IHostService {

    private final AiHostMapper mapper;
    private final LlmVendorCryptoService crypto;
    private final SshClient ssh;

    @Autowired
    public HostServiceImpl(AiHostMapper mapper, LlmVendorCryptoService crypto) {
        this.mapper = mapper;
        this.crypto = crypto;
        this.ssh = new JSchClient();
    }

    @Override
    public List<AiHostVo> list(AiHostBo query, Long userDeptId, boolean isAdmin) {
        AiHost q = new AiHost();
        q.setHostName(query.getHostName());
        q.setIp(query.getIp());
        if (!isAdmin) {
            q.setDeptId(userDeptId);
        } else {
            q.setDeptId(query.getDeptId());
        }
        return mapper.selectList(q).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public List<AiHostVo> listAll() {
        return mapper.selectList(new AiHost()).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public AiHostVo getById(Long id) { return toVo(mapper.selectById(id)); }

    @Override
    public Long insert(AiHostBo bo) {
        if (bo.getDeptId() == null) throw new IllegalArgumentException("所属部门不能为空");
        if (mapper.countByDeptAndName(bo.getDeptId(), bo.getHostName()) > 0) {
            throw new IllegalArgumentException("该部门下已存在同名主机");
        }
        AiHost e = toEntity(bo);
        e.setStatus("2");
        if ("0".equals(bo.getAuthType())) {
            e.setPasswordCipher(crypto.encrypt(bo.getPassword()));
        } else if ("1".equals(bo.getAuthType())) {
            e.setPrivateKeyCipher(crypto.encrypt(bo.getPrivateKey()));
        }
        e.setCreateTime(DateUtils.getNowDate());
        mapper.insert(e);
        return e.getHostId();
    }

    @Override
    public void update(AiHostBo bo) {
        AiHost existing = mapper.selectById(bo.getHostId());
        if (existing == null) throw new IllegalArgumentException("主机不存在");
        AiHost e = toEntity(bo);
        if ("0".equals(bo.getAuthType()) && StrUtil.isNotBlank(bo.getPassword())) {
            e.setPasswordCipher(crypto.encrypt(bo.getPassword()));
        } else if ("1".equals(bo.getAuthType()) && StrUtil.isNotBlank(bo.getPrivateKey())) {
            e.setPrivateKeyCipher(crypto.encrypt(bo.getPrivateKey()));
        }
        e.setUpdateTime(DateUtils.getNowDate());
        mapper.updateById(e);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        // Phase 2: ai_session 尚未建表，引用检查方法安全返回空（见 Mapper XML TODO）
        List<Long> referencing = mapper.selectReferencingHostIds(ids);
        if (referencing != null && !referencing.isEmpty()) {
            throw new IllegalStateException("主机 [" + referencing + "] 正被 AI 会话使用，不能删除");
        }
        mapper.deleteByIds(ids);
    }

    @Override
    public TestResultVo testConnect(AiHostBo bo) {
        AiHost existing = null;
        if (bo.getHostId() != null) existing = mapper.selectById(bo.getHostId());
        String password = "0".equals(bo.getAuthType()) ? bo.getPassword() :
            "1".equals(bo.getAuthType()) ? bo.getPrivateKey() : null;
        if (password == null && existing != null) {
            password = "0".equals(bo.getAuthType()) ? crypto.decrypt(existing.getPasswordCipher()) :
                "1".equals(bo.getAuthType()) ? crypto.decrypt(existing.getPrivateKeyCipher()) : null;
        }
        SshCommand cmd = new SshCommand(bo.getIp(),
            bo.getSshPort() == null ? 22 : bo.getSshPort(),
            bo.getUsername(), bo.getAuthType(), password,
            "echo alive", 30);
        long t0 = System.currentTimeMillis();
        try {
            ssh.exec(cmd);
            TestResultVo r = TestResultVo.success("连接且命令成功");
            r.setElapsedMs(System.currentTimeMillis() - t0);
            return r;
        } catch (SshException e) {
            return TestResultVo.failure(e.getCode() + ": " + e.getMessage());
        }
    }

    @Override
    public TestResultVo testConnectAndSave(Long id) {
        AiHost v = mapper.selectById(id);
        if (v == null) throw new IllegalArgumentException("主机不存在");
        AiHostBo bo = new AiHostBo();
        bo.setHostId(v.getHostId());
        bo.setIp(v.getIp());
        bo.setSshPort(v.getSshPort());
        bo.setUsername(v.getUsername());
        bo.setAuthType(v.getAuthType());
        TestResultVo r = testConnect(bo);
        v.setLastTestAt(new Date());
        v.setLastTestMsg(r.getMessage());
        v.setStatus(r.getSuccess() ? "0" : "1");
        mapper.updateById(v);
        return r;
    }

    private AiHostVo toVo(AiHost e) {
        if (e == null) return null;
        AiHostVo v = new AiHostVo();
        v.setHostId(e.getHostId());
        v.setHostName(e.getHostName());
        v.setIp(e.getIp());
        v.setSshPort(e.getSshPort());
        v.setSshProtocol(e.getSshProtocol());
        v.setUsername(e.getUsername());
        v.setAuthType(e.getAuthType());
        v.setDeptId(e.getDeptId());
        v.setStatus(e.getStatus());
        v.setLastTestAt(e.getLastTestAt());
        v.setLastTestMsg(e.getLastTestMsg());
        v.setRemark(e.getRemark());
        v.setCreateBy(e.getCreateBy());
        v.setCreateTime(e.getCreateTime());
        v.setUpdateBy(e.getUpdateBy());
        v.setUpdateTime(e.getUpdateTime());
        return v;
    }

    private AiHost toEntity(AiHostBo bo) {
        AiHost e = new AiHost();
        e.setHostId(bo.getHostId());
        e.setHostName(bo.getHostName());
        e.setIp(bo.getIp());
        e.setSshPort(bo.getSshPort());
        e.setSshProtocol(bo.getSshProtocol());
        e.setUsername(bo.getUsername());
        e.setAuthType(bo.getAuthType());
        e.setDeptId(bo.getDeptId());
        e.setRemark(bo.getRemark());
        e.setCreateBy(bo.getCreateBy());
        e.setUpdateBy(bo.getUpdateBy());
        return e;
    }
}
