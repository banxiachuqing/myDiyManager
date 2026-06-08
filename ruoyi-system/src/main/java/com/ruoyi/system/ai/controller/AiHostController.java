package com.ruoyi.system.ai.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.ai.domain.bo.AiHostBo;
import com.ruoyi.system.ai.service.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/host")
public class AiHostController extends BaseController {

    private final IHostService service;

    @Autowired
    public AiHostController(IHostService service) {
        this.service = service;
    }

    @PreAuthorize("@ss.hasPermi('ai:host:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiHostBo query) {
        startPage();
        SysUser u = getLoginUser().getUser();
        boolean isAdmin = u.isAdmin();
        Long deptId = u.getDeptId();
        return getDataTable(service.list(query, deptId, isAdmin));
    }

    @PreAuthorize("@ss.hasPermi('ai:host:list')")
    @GetMapping("/all")
    public AjaxResult all() {
        return AjaxResult.success(service.listAll());
    }

    @PreAuthorize("@ss.hasPermi('ai:host:query')")
    @GetMapping("/{hostId}")
    public AjaxResult getInfo(@PathVariable Long hostId) {
        return AjaxResult.success(service.getById(hostId));
    }

    @PreAuthorize("@ss.hasPermi('ai:host:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AiHostBo bo) {
        bo.setCreateBy(getUsername());
        return toAjax(service.insert(bo).intValue());
    }

    @PreAuthorize("@ss.hasPermi('ai:host:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody AiHostBo bo) {
        bo.setUpdateBy(getUsername());
        service.update(bo);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:host:remove')")
    @DeleteMapping("/{hostIds}")
    public AjaxResult remove(@PathVariable Long[] hostIds) {
        service.deleteByIds(hostIds);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:host:test')")
    @PostMapping("/test")
    public AjaxResult test(@RequestBody AiHostBo bo) {
        return AjaxResult.success(service.testConnect(bo));
    }

    @PreAuthorize("@ss.hasPermi('ai:host:test')")
    @PostMapping("/{hostId}/test")
    public AjaxResult testAndSave(@PathVariable Long hostId) {
        return AjaxResult.success(service.testConnectAndSave(hostId));
    }
}
