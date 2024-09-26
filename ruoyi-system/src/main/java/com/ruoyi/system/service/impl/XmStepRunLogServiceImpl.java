package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.XmStepRunLog;
import com.ruoyi.system.mapper.XmStepRunLogMapper;
import com.ruoyi.system.service.IXmStepRunLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小米刷步数运行日志Service业务层处理
 *
 * @author zhangjie
 * @date 2024-09-26
 */
@Service
public class XmStepRunLogServiceImpl implements IXmStepRunLogService
{
    @Autowired
    private XmStepRunLogMapper xmStepRunLogMapper;

    /**
     * 查询小米刷步数运行日志
     *
     * @param id 小米刷步数运行日志主键
     * @return 小米刷步数运行日志
     */
    @Override
    public XmStepRunLog selectXmStepRunLogById(Long id)
    {
        return xmStepRunLogMapper.selectXmStepRunLogById(id);
    }

    /**
     * 查询小米刷步数运行日志列表
     *
     * @param xmStepRunLog 小米刷步数运行日志
     * @return 小米刷步数运行日志
     */
    @Override
    public List<XmStepRunLog> selectXmStepRunLogList(XmStepRunLog xmStepRunLog)
    {
        return xmStepRunLogMapper.selectXmStepRunLogList(xmStepRunLog);
    }

    /**
     * 新增小米刷步数运行日志
     *
     * @param xmStepRunLog 小米刷步数运行日志
     * @return 结果
     */
    @Override
    public int insertXmStepRunLog(XmStepRunLog xmStepRunLog)
    {
        xmStepRunLog.setCreateTime(DateUtils.getNowDate());
        return xmStepRunLogMapper.insertXmStepRunLog(xmStepRunLog);
    }

    /**
     * 修改小米刷步数运行日志
     *
     * @param xmStepRunLog 小米刷步数运行日志
     * @return 结果
     */
    @Override
    public int updateXmStepRunLog(XmStepRunLog xmStepRunLog)
    {
        return xmStepRunLogMapper.updateXmStepRunLog(xmStepRunLog);
    }

    /**
     * 批量删除小米刷步数运行日志
     *
     * @param ids 需要删除的小米刷步数运行日志主键
     * @return 结果
     */
    @Override
    public int deleteXmStepRunLogByIds(Long[] ids)
    {
        return xmStepRunLogMapper.deleteXmStepRunLogByIds(ids);
    }

    /**
     * 删除小米刷步数运行日志信息
     *
     * @param id 小米刷步数运行日志主键
     * @return 结果
     */
    @Override
    public int deleteXmStepRunLogById(Long id)
    {
        return xmStepRunLogMapper.deleteXmStepRunLogById(id);
    }
}
