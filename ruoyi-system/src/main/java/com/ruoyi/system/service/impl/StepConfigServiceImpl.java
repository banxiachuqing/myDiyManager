package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.StepConfig;
import com.ruoyi.system.mapper.StepConfigMapper;
import com.ruoyi.system.service.IStepConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小米步数配置Service业务层处理
 *
 * @author zhangjie
 * @date 2024-09-26
 */
@Service
public class StepConfigServiceImpl implements IStepConfigService
{
    @Autowired
    private StepConfigMapper stepConfigMapper;

    /**
     * 查询小米步数配置
     *
     * @param id 小米步数配置主键
     * @return 小米步数配置
     */
    @Override
    public StepConfig selectStepConfigById(String id)
    {
        return stepConfigMapper.selectStepConfigById(id);
    }

    /**
     * 查询小米步数配置列表
     *
     * @param stepConfig 小米步数配置
     * @return 小米步数配置
     */
    @Override
    public List<StepConfig> selectStepConfigList(StepConfig stepConfig)
    {
        return stepConfigMapper.selectStepConfigList(stepConfig);
    }

    /**
     * 新增小米步数配置
     *
     * @param stepConfig 小米步数配置
     * @return 结果
     */
    @Override
    public int insertStepConfig(StepConfig stepConfig)
    {
        stepConfig.setCreateTime(DateUtils.getNowDate());
        return stepConfigMapper.insertStepConfig(stepConfig);
    }

    /**
     * 修改小米步数配置
     *
     * @param stepConfig 小米步数配置
     * @return 结果
     */
    @Override
    public int updateStepConfig(StepConfig stepConfig)
    {
        stepConfig.setUpdateTime(DateUtils.getNowDate());
        return stepConfigMapper.updateStepConfig(stepConfig);
    }

    /**
     * 批量删除小米步数配置
     *
     * @param ids 需要删除的小米步数配置主键
     * @return 结果
     */
    @Override
    public int deleteStepConfigByIds(String[] ids)
    {
        return stepConfigMapper.deleteStepConfigByIds(ids);
    }

    /**
     * 删除小米步数配置信息
     *
     * @param id 小米步数配置主键
     * @return 结果
     */
    @Override
    public int deleteStepConfigById(String id)
    {
        return stepConfigMapper.deleteStepConfigById(id);
    }
}
