package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.StepConfig;
import com.ruoyi.system.service.IStepConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 小米步数配置Controller
 *
 * @author zhangjie
 * @date 2024-09-26
 */
@RestController
@RequestMapping("/step/config")
public class StepConfigController extends BaseController
{
    @Autowired
    private IStepConfigService stepConfigService;

    /**
     * 查询小米步数配置列表
     */
    @PreAuthorize("@ss.hasPermi('step:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(StepConfig stepConfig)
    {
        startPage();
        List<StepConfig> list = stepConfigService.selectStepConfigList(stepConfig);
        return getDataTable(list);
    }

    /**
     * 导出小米步数配置列表
     */
    @PreAuthorize("@ss.hasPermi('step:config:export')")
    @Log(title = "小米步数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StepConfig stepConfig)
    {
        List<StepConfig> list = stepConfigService.selectStepConfigList(stepConfig);
        ExcelUtil<StepConfig> util = new ExcelUtil<StepConfig>(StepConfig.class);
        util.exportExcel(response, list, "小米步数配置数据");
    }

    /**
     * 获取小米步数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('step:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(stepConfigService.selectStepConfigById(id));
    }

    /**
     * 新增小米步数配置
     */
    @PreAuthorize("@ss.hasPermi('step:config:add')")
    @Log(title = "小米步数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StepConfig stepConfig)
    {
        return toAjax(stepConfigService.insertStepConfig(stepConfig));
    }

    /**
     * 修改小米步数配置
     */
    @PreAuthorize("@ss.hasPermi('step:config:edit')")
    @Log(title = "小米步数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StepConfig stepConfig)
    {
        return toAjax(stepConfigService.updateStepConfig(stepConfig));
    }

    /**
     * 删除小米步数配置
     */
    @PreAuthorize("@ss.hasPermi('step:config:remove')")
    @Log(title = "小米步数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(stepConfigService.deleteStepConfigByIds(ids));
    }
}
