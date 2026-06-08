package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.domain.vo.AiVendorVo;
import com.ruoyi.system.ai.domain.vo.TestResultVo;
import java.util.List;

public interface IVendorService {
    List<AiVendorVo> list(AiVendorBo query);
    AiVendorVo getById(Long vendorId);
    Long insert(AiVendorBo bo);
    void update(AiVendorBo bo);
    void deleteByIds(Long[] vendorIds);
    void setDefault(Long vendorId);
    TestResultVo testConnect(AiVendorBo bo);
    TestResultVo testConnectAndSave(Long vendorId);
    AiVendorVo getDefault();
}
