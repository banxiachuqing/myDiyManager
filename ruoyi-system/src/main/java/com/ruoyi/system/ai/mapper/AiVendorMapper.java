package com.ruoyi.system.ai.mapper;

import com.ruoyi.system.ai.domain.AiVendor;
import java.util.List;

public interface AiVendorMapper {
    AiVendor selectById(Long vendorId);
    List<AiVendor> selectList(AiVendor query);
    int insert(AiVendor entity);
    int updateById(AiVendor entity);
    int deleteByIds(Long[] vendorIds);
    int clearAllDefault();
    int setDefault(Long vendorId);
    AiVendor selectDefault();
    int countByName(String vendorName);
}
