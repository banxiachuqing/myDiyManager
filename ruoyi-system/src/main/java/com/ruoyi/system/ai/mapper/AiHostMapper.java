package com.ruoyi.system.ai.mapper;

import com.ruoyi.system.ai.domain.AiHost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiHostMapper {
    AiHost selectById(Long hostId);
    List<AiHost> selectList(AiHost query);
    List<AiHost> selectAllInUse();
    int insert(AiHost entity);
    int updateById(AiHost entity);
    int deleteByIds(Long[] hostIds);
    int countByDeptAndName(@Param("deptId") Long deptId, @Param("hostName") String hostName);
    List<Long> selectReferencingHostIds(Long[] hostIds);
    List<AiHost> selectBatchIds(@Param("ids") List<Long> ids);
    AiHost selectByHostName(String hostName);
}
