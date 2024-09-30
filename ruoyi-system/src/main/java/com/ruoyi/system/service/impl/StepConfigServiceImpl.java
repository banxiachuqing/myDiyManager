package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.exception.job.TaskException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;
import com.ruoyi.system.domain.StepConfig;
import com.ruoyi.system.mapper.StepConfigMapper;
import com.ruoyi.system.service.IStepConfigService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 小米步数配置Service业务层处理
 * @author zhangjie
 * @date 2024-09-26
 */
@Slf4j
@Service
public class StepConfigServiceImpl implements IStepConfigService {
    @Autowired
    private StepConfigMapper stepConfigMapper;

    @Resource
    private ISysJobService sysJobService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询小米步数配置
     * @param id 小米步数配置主键
     * @return 小米步数配置
     */
    @Override
    public StepConfig selectStepConfigById(String id) {
        return stepConfigMapper.selectStepConfigById(id);
    }

    /**
     * 查询小米步数配置列表
     * @param stepConfig 小米步数配置
     * @return 小米步数配置
     */
    @Override
    public List<StepConfig> selectStepConfigList(StepConfig stepConfig) {
        return stepConfigMapper.selectStepConfigList(stepConfig);
    }

    /**
     * 新增小米步数配置
     * @param stepConfig 小米步数配置
     * @return 结果
     */
    @Override
    public int insertStepConfig(StepConfig stepConfig) throws SchedulerException, TaskException {
        stepConfig.setCreateTime(DateUtils.getNowDate());


        if (!stringRedisTemplate.hasKey(Constants.STEP_CONFIG_REDIS_KEY)) {
            stringRedisTemplate.opsForValue().set(Constants.STEP_CONFIG_REDIS_KEY, "500");
        }

        Long id = this.stringRedisTemplate.opsForValue().increment(Constants.STEP_CONFIG_REDIS_KEY);


        stepConfig.setId(id.toString());

        SysJob sysJob = SysJob
                .builder()
                .concurrent("0")
                .jobId(id)
                .cronExpression(stepConfig.getCron())
                .jobName("小米步数任务:" + stepConfig.getUserName())
                .status("0")
                .jobGroup("XMSTEP")
                .misfirePolicy("1")
                .invokeTarget("miMotionRunner.runStep('" + stepConfig.getId() + "')")
                .build();
        this.sysJobService.insertJob(sysJob);


        this.stringRedisTemplate.opsForHash().put("xm-step-config", stepConfig.getId(), JSON.toJSONString(stepConfig));

        if (stepConfig.getStatus().equals(0L)) {
            sysJob.setStatus("0");
            this.sysJobService.changeStatus(sysJob);
        }

        return stepConfigMapper.insertStepConfig(stepConfig);
    }

    /**
     * 修改小米步数配置
     * @param stepConfig 小米步数配置
     * @return 结果
     */
    @Override
    public int updateStepConfig(StepConfig stepConfig) {
        stepConfig.setUpdateTime(DateUtils.getNowDate());

        try {
            SysJob sysJob = this.sysJobService.selectJobById(Long.valueOf(stepConfig.getId()));
            sysJob.setCronExpression(stepConfig.getCron());
            sysJob.setStatus(stepConfig.getStatus() + "");
            this.sysJobService.updateJob(sysJob);
        } catch (Exception e) {
            log.error("更新失败", e);
            throw new BaseException("通知定时任务失败");
        }
        this.stringRedisTemplate.opsForHash().put("xm-step-config", stepConfig.getId(), JSON.toJSONString(stepConfig));
        return stepConfigMapper.updateStepConfig(stepConfig);
    }

    /**
     * 批量删除小米步数配置
     * @param ids 需要删除的小米步数配置主键
     * @return 结果
     */
    @Override
    public int deleteStepConfigByIds(String[] ids) {
        try {
            List<Long> newids = new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                newids.add(Long.parseLong(ids[i]));
            }
            this.sysJobService.deleteJobByIds(newids.toArray(new Long[0]));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return stepConfigMapper.deleteStepConfigByIds(ids);
    }

    /**
     * 删除小米步数配置信息
     * @param id 小米步数配置主键
     * @return 结果
     */
    @Override
    public int deleteStepConfigById(String id) {
        try {
            this.sysJobService.deleteJobByIds(new Long[]{Long.parseLong(id)});
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return stepConfigMapper.deleteStepConfigById(id);
    }
}
