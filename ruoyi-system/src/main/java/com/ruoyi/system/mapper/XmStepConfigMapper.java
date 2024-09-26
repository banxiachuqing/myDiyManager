package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.XmStepConfig;

import java.util.List;

/**
 * 小米步数配置Mapper接口
 *
 * @author zhangjie
 * @date 2024-09-25
 */
public interface XmStepConfigMapper
{
    /**
     * 查询小米步数配置
     *
     * @param id 小米步数配置主键
     * @return 小米步数配置
     */
    public XmStepConfig selectXmStepConfigById(String id);

    /**
     * 查询小米步数配置列表
     *
     * @param xmStepConfig 小米步数配置
     * @return 小米步数配置集合
     */
    public List<XmStepConfig> selectXmStepConfigList(XmStepConfig xmStepConfig);

    /**
     * 新增小米步数配置
     *
     * @param xmStepConfig 小米步数配置
     * @return 结果
     */
    public int insertXmStepConfig(XmStepConfig xmStepConfig);

    /**
     * 修改小米步数配置
     *
     * @param xmStepConfig 小米步数配置
     * @return 结果
     */
    public int updateXmStepConfig(XmStepConfig xmStepConfig);

    /**
     * 删除小米步数配置
     *
     * @param id 小米步数配置主键
     * @return 结果
     */
    public int deleteXmStepConfigById(String id);

    /**
     * 批量删除小米步数配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteXmStepConfigByIds(String[] ids);
}
