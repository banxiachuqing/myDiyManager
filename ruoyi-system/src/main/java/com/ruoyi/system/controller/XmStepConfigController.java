package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.XmStepConfig;
import com.ruoyi.system.service.IXmStepConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 小米步数配置Controller
 *
 * @author zhangjie
 * @date 2024-09-25
 */
@RestController
@RequestMapping("/step/stepConfig")
public class XmStepConfigController extends BaseController
{
    @Autowired
    private IXmStepConfigService xmStepConfigService;

    /**
     * 查询小米步数配置列表
     */
    @PreAuthorize("@ss.hasPermi('step:stepConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(XmStepConfig xmStepConfig)
    {
        startPage();
        List<XmStepConfig> list = xmStepConfigService.selectXmStepConfigList(xmStepConfig);
        return getDataTable(list);
    }

    /**
     * 导出小米步数配置列表
     */
    @PreAuthorize("@ss.hasPermi('step:stepConfig:export')")
    @Log(title = "小米步数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, XmStepConfig xmStepConfig)
    {
        List<XmStepConfig> list = xmStepConfigService.selectXmStepConfigList(xmStepConfig);
        ExcelUtil<XmStepConfig> util = new ExcelUtil<XmStepConfig>(XmStepConfig.class);
        util.exportExcel(response, list, "小米步数配置数据");
    }

    /**
     * 获取小米步数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('step:stepConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(xmStepConfigService.selectXmStepConfigById(id));
    }

    /**
     * 新增小米步数配置
     */
    @PreAuthorize("@ss.hasPermi('step:stepConfig:add')")
    @Log(title = "小米步数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody XmStepConfig xmStepConfig)
    {
        return toAjax(xmStepConfigService.insertXmStepConfig(xmStepConfig));
    }

    /**
     * 修改小米步数配置
     */
    @PreAuthorize("@ss.hasPermi('step:stepConfig:edit')")
    @Log(title = "小米步数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody XmStepConfig xmStepConfig)
    {
        return toAjax(xmStepConfigService.updateXmStepConfig(xmStepConfig));
    }

    /**
     * 删除小米步数配置
     */
    @PreAuthorize("@ss.hasPermi('step:stepConfig:remove')")
    @Log(title = "小米步数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(xmStepConfigService.deleteXmStepConfigByIds(ids));
    }
}
