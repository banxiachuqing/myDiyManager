package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.StepConfig;

import java.util.List;

/**
 * 小米步数配置Mapper接口
 * @author zhangjie
 * @date 2024-09-26
 */
public interface StepConfigMapper {
    /**
     * 查询小米步数配置
     * @param id 小米步数配置主键
     * @return 小米步数配置
     */
    public StepConfig selectStepConfigById(String id);

    /**
     * 查询小米步数配置列表
     * @param stepConfig 小米步数配置
     * @return 小米步数配置集合
     */
    public List<StepConfig> selectStepConfigList(StepConfig stepConfig);

    /**
     * 新增小米步数配置
     * @param stepConfig 小米步数配置
     * @return 结果
     */
    public int insertStepConfig(StepConfig stepConfig);

    /**
     * 修改小米步数配置
     * @param stepConfig 小米步数配置
     * @return 结果
     */
    public int updateStepConfig(StepConfig stepConfig);

    /**
     * 删除小米步数配置
     * @param id 小米步数配置主键
     * @return 结果
     */
    public int deleteStepConfigById(String id);

    /**
     * 批量删除小米步数配置
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStepConfigByIds(String[] ids);
}
