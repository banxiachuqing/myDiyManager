package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.XmStepConfig;
import com.ruoyi.system.mapper.XmStepConfigMapper;
import com.ruoyi.system.service.IXmStepConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小米步数配置Service业务层处理
 *
 * @author zhangjie
 * @date 2024-09-25
 */
@Service
public class XmStepConfigServiceImpl implements IXmStepConfigService
{
    @Autowired
    private XmStepConfigMapper xmStepConfigMapper;

    /**
     * 查询小米步数配置
     *
     * @param id 小米步数配置主键
     * @return 小米步数配置
     */
    @Override
    public XmStepConfig selectXmStepConfigById(String id)
    {
        return xmStepConfigMapper.selectXmStepConfigById(id);
    }

    /**
     * 查询小米步数配置列表
     *
     * @param xmStepConfig 小米步数配置
     * @return 小米步数配置
     */
    @Override
    public List<XmStepConfig> selectXmStepConfigList(XmStepConfig xmStepConfig)
    {
        return xmStepConfigMapper.selectXmStepConfigList(xmStepConfig);
    }

    /**
     * 新增小米步数配置
     *
     * @param xmStepConfig 小米步数配置
     * @return 结果
     */
    @Override
    public int insertXmStepConfig(XmStepConfig xmStepConfig)
    {
        xmStepConfig.setCreateTime(DateUtils.getNowDate());
        return xmStepConfigMapper.insertXmStepConfig(xmStepConfig);
    }

    /**
     * 修改小米步数配置
     *
     * @param xmStepConfig 小米步数配置
     * @return 结果
     */
    @Override
    public int updateXmStepConfig(XmStepConfig xmStepConfig)
    {
        xmStepConfig.setUpdateTime(DateUtils.getNowDate());
        return xmStepConfigMapper.updateXmStepConfig(xmStepConfig);
    }

    /**
     * 批量删除小米步数配置
     *
     * @param ids 需要删除的小米步数配置主键
     * @return 结果
     */
    @Override
    public int deleteXmStepConfigByIds(String[] ids)
    {
        return xmStepConfigMapper.deleteXmStepConfigByIds(ids);
    }

    /**
     * 删除小米步数配置信息
     *
     * @param id 小米步数配置主键
     * @return 结果
     */
    @Override
    public int deleteXmStepConfigById(String id)
    {
        return xmStepConfigMapper.deleteXmStepConfigById(id);
    }
}
