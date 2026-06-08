package com.ruoyi.system.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiVendor;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.domain.vo.AiVendorVo;
import com.ruoyi.system.ai.domain.vo.TestResultVo;
import com.ruoyi.system.ai.llm.ChatRequest;
import com.ruoyi.system.ai.llm.LlmException;
import com.ruoyi.system.ai.llm.OpenAiCompatClient;
import com.ruoyi.system.ai.mapper.AiVendorMapper;
import com.ruoyi.system.ai.service.IVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements IVendorService {

    private final AiVendorMapper mapper;
    private final LlmVendorCryptoService crypto;

    @Autowired
    public VendorServiceImpl(AiVendorMapper mapper, LlmVendorCryptoService crypto) {
        this.mapper = mapper;
        this.crypto = crypto;
    }

    @Override
    public List<AiVendorVo> list(AiVendorBo q) {
        AiVendor query = new AiVendor();
        query.setVendorName(q.getVendorName());
        query.setStatus(q.getStatus());
        return mapper.selectList(query).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public AiVendorVo getById(Long id) { return toVo(mapper.selectById(id)); }

    @Override
    public Long insert(AiVendorBo bo) {
        if (mapper.countByName(bo.getVendorName()) > 0) {
            throw new IllegalArgumentException("已存在同名厂商");
        }
        AiVendor e = toEntity(bo);
        e.setApiKeyCipher(crypto.encrypt(bo.getApiKey()));
        e.setApiKeyMask(crypto.mask(bo.getApiKey()));
        e.setCreateTime(DateUtils.getNowDate());
        mapper.insert(e);
        return e.getVendorId();
    }

    @Override
    public void update(AiVendorBo bo) {
        AiVendor existing = mapper.selectById(bo.getVendorId());
        if (existing == null) throw new IllegalArgumentException("厂商不存在");
        if ("1".equals(existing.getIsDefault()) && "1".equals(bo.getStatus())) {
            throw new IllegalStateException("默认厂商不可停用，请先指定其它默认厂商");
        }
        AiVendor e = toEntity(bo);
        if (StrUtil.isNotBlank(bo.getApiKey())) {
            e.setApiKeyCipher(crypto.encrypt(bo.getApiKey()));
            e.setApiKeyMask(crypto.mask(bo.getApiKey()));
        }
        e.setUpdateTime(DateUtils.getNowDate());
        mapper.updateById(e);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        for (Long id : ids) {
            AiVendor v = mapper.selectById(id);
            if (v == null) continue;
            if ("1".equals(v.getIsDefault())) {
                throw new IllegalStateException("默认厂商不可直接删除，请先改默认");
            }
        }
        mapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        AiVendor v = mapper.selectById(id);
        if (v == null) throw new IllegalArgumentException("厂商不存在");
        if (!"0".equals(v.getStatus())) {
            throw new IllegalStateException("停用厂商不可设为默认");
        }
        mapper.clearAllDefault();
        mapper.setDefault(id);
    }

    @Override
    public TestResultVo testConnect(AiVendorBo bo) {
        OpenAiCompatClient client = new OpenAiCompatClient(
            bo.getBaseUrl(), bo.getApiKey(), bo.getModelName(),
            bo.getTimeoutSec() == null ? 30 : bo.getTimeoutSec());
        long t0 = System.currentTimeMillis();
        try {
            client.listModels();
            TestResultVo r = TestResultVo.success("连接且鉴权正常");
            r.setElapsedMs(System.currentTimeMillis() - t0);
            return r;
        } catch (LlmException e) {
            if (e.getCode() == LlmException.Code.AUTH) {
                try {
                    ChatRequest req = new ChatRequest();
                    req.setModel(bo.getModelName());
                    req.setMaxTokens(1);
                    req.setMessages(Collections.singletonList(msgOf("user", "hi")));
                    client.chat(req);
                    TestResultVo r = TestResultVo.success("鉴权通过（仅 listModels 失败）");
                    r.setElapsedMs(System.currentTimeMillis() - t0);
                    return r;
                } catch (LlmException e2) {
                    return TestResultVo.failure("鉴权失败: " + e2.getMessage());
                }
            }
            return TestResultVo.failure(e.getCode() + ": " + e.getMessage());
        }
    }

    @Override
    public TestResultVo testConnectAndSave(Long id) {
        AiVendor v = mapper.selectById(id);
        if (v == null) throw new IllegalArgumentException("厂商不存在");
        AiVendorBo bo = new AiVendorBo();
        bo.setBaseUrl(v.getBaseUrl());
        bo.setApiKey(crypto.decrypt(v.getApiKeyCipher()));
        bo.setModelName(v.getModelName());
        bo.setTimeoutSec(v.getTimeoutSec());
        TestResultVo r = testConnect(bo);
        v.setLastTestAt(DateUtils.getNowDate());
        v.setLastTestMsg(r.getMessage());
        mapper.updateById(v);
        return r;
    }

    @Override
    public AiVendorVo getDefault() { return toVo(mapper.selectDefault()); }

    private static Map<String, String> msgOf(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private AiVendorVo toVo(AiVendor e) {
        if (e == null) return null;
        AiVendorVo v = new AiVendorVo();
        v.setVendorId(e.getVendorId());
        v.setVendorName(e.getVendorName());
        v.setBaseUrl(e.getBaseUrl());
        v.setApiKeyMask(e.getApiKeyMask());
        v.setModelName(e.getModelName());
        v.setStatus(e.getStatus());
        v.setIsDefault(e.getIsDefault());
        v.setTimeoutSec(e.getTimeoutSec());
        v.setMaxTokens(e.getMaxTokens());
        v.setTemperature(e.getTemperature());
        v.setLastTestAt(e.getLastTestAt());
        v.setLastTestMsg(e.getLastTestMsg());
        v.setRemark(e.getRemark());
        v.setCreateBy(e.getCreateBy());
        v.setCreateTime(e.getCreateTime());
        v.setUpdateBy(e.getUpdateBy());
        v.setUpdateTime(e.getUpdateTime());
        return v;
    }

    private AiVendor toEntity(AiVendorBo bo) {
        AiVendor e = new AiVendor();
        e.setVendorId(bo.getVendorId());
        e.setVendorName(bo.getVendorName());
        e.setBaseUrl(bo.getBaseUrl());
        e.setModelName(bo.getModelName());
        e.setStatus(bo.getStatus());
        e.setIsDefault(bo.getIsDefault());
        e.setTimeoutSec(bo.getTimeoutSec());
        e.setMaxTokens(bo.getMaxTokens());
        e.setTemperature(bo.getTemperature());
        e.setRemark(bo.getRemark());
        e.setCreateBy(bo.getCreateBy());
        e.setUpdateBy(bo.getUpdateBy());
        return e;
    }
}
