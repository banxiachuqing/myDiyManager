package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.XmStepRunLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小米刷步数运行日志Mapper接口
 *
 * @author zhangjie
 * @date 2024-09-26
 */
public interface XmStepRunLogMapper
{
    /**
     * 查询小米刷步数运行日志
     *
     * @param id 小米刷步数运行日志主键
     * @return 小米刷步数运行日志
     */
    public XmStepRunLog selectXmStepRunLogById(Long id);

    /**
     * 查询小米刷步数运行日志列表
     *
     * @param xmStepRunLog 小米刷步数运行日志
     * @return 小米刷步数运行日志集合
     */
    public List<XmStepRunLog> selectXmStepRunLogList(XmStepRunLog xmStepRunLog);

    /**
     * 新增小米刷步数运行日志
     *
     * @param xmStepRunLog 小米刷步数运行日志
     * @return 结果
     */
    public int insertXmStepRunLog(XmStepRunLog xmStepRunLog);

    /**
     * 修改小米刷步数运行日志
     *
     * @param xmStepRunLog 小米刷步数运行日志
     * @return 结果
     */
    public int updateXmStepRunLog(XmStepRunLog xmStepRunLog);

    /**
     * 删除小米刷步数运行日志
     *
     * @param id 小米刷步数运行日志主键
     * @return 结果
     */
    public int deleteXmStepRunLogById(Long id);

    /**
     * 批量删除小米刷步数运行日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteXmStepRunLogByIds(Long[] ids);

    List<XmStepRunLog> selectByConfigId(@Param("configId") String configId, @Param("limit") int limit);
}
