package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.domain.bo.AiHostBo;
import com.ruoyi.system.ai.domain.vo.AiHostVo;
import com.ruoyi.system.ai.domain.vo.TestResultVo;
import java.util.List;

public interface IHostService {
    List<AiHostVo> list(AiHostBo query, Long userDeptId, boolean isAdmin);
    List<AiHostVo> listAll();
    AiHostVo getById(Long hostId);
    Long insert(AiHostBo bo);
    void update(AiHostBo bo);
    void deleteByIds(Long[] hostIds);
    TestResultVo testConnect(AiHostBo bo);
    TestResultVo testConnectAndSave(Long hostId);
}
