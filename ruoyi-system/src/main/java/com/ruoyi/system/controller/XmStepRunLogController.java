package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.XmStepRunLog;
import com.ruoyi.system.service.IXmStepRunLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 小米刷步数运行日志Controller
 *
 * @author zhangjie
 * @date 2024-09-26
 */
@RestController
@RequestMapping("/step/log")
public class XmStepRunLogController extends BaseController
{
    @Autowired
    private IXmStepRunLogService xmStepRunLogService;

    /**
     * 查询小米刷步数运行日志列表
     */
    @PreAuthorize("@ss.hasPermi('step:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(XmStepRunLog xmStepRunLog)
    {
        startPage();
        List<XmStepRunLog> list = xmStepRunLogService.selectXmStepRunLogList(xmStepRunLog);
        return getDataTable(list);
    }

    /**
     * 导出小米刷步数运行日志列表
     */
    @PreAuthorize("@ss.hasPermi('step:log:export')")
    @Log(title = "小米刷步数运行日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, XmStepRunLog xmStepRunLog)
    {
        List<XmStepRunLog> list = xmStepRunLogService.selectXmStepRunLogList(xmStepRunLog);
        ExcelUtil<XmStepRunLog> util = new ExcelUtil<XmStepRunLog>(XmStepRunLog.class);
        util.exportExcel(response, list, "小米刷步数运行日志数据");
    }

    /**
     * 获取小米刷步数运行日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('step:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(xmStepRunLogService.selectXmStepRunLogById(id));
    }

    /**
     * 新增小米刷步数运行日志
     */
    @PreAuthorize("@ss.hasPermi('step:log:add')")
    @Log(title = "小米刷步数运行日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody XmStepRunLog xmStepRunLog)
    {
        return toAjax(xmStepRunLogService.insertXmStepRunLog(xmStepRunLog));
    }

    /**
     * 修改小米刷步数运行日志
     */
    @PreAuthorize("@ss.hasPermi('step:log:edit')")
    @Log(title = "小米刷步数运行日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody XmStepRunLog xmStepRunLog)
    {
        return toAjax(xmStepRunLogService.updateXmStepRunLog(xmStepRunLog));
    }

    /**
     * 删除小米刷步数运行日志
     */
    @PreAuthorize("@ss.hasPermi('step:log:remove')")
    @Log(title = "小米刷步数运行日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(xmStepRunLogService.deleteXmStepRunLogByIds(ids));
    }
}
