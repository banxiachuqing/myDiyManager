package com.ruoyi.system.service;

import com.ruoyi.system.domain.XmStepRunLog;

import java.util.List;

/**
 * 小米刷步数运行日志Service接口
 *
 * @author zhangjie
 * @date 2024-09-26
 */
public interface IXmStepRunLogService
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
     * 批量删除小米刷步数运行日志
     *
     * @param ids 需要删除的小米刷步数运行日志主键集合
     * @return 结果
     */
    public int deleteXmStepRunLogByIds(Long[] ids);

    /**
     * 删除小米刷步数运行日志信息
     *
     * @param id 小米刷步数运行日志主键
     * @return 结果
     */
    public int deleteXmStepRunLogById(Long id);
}
