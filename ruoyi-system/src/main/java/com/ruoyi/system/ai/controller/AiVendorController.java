package com.ruoyi.system.ai.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.service.IVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/vendor")
public class AiVendorController extends BaseController {

    private final IVendorService service;

    @Autowired
    public AiVendorController(IVendorService service) {
        this.service = service;
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiVendorBo query) {
        startPage();
        return getDataTable(service.list(query));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:query')")
    @GetMapping("/{vendorId}")
    public AjaxResult getInfo(@PathVariable Long vendorId) {
        return AjaxResult.success(service.getById(vendorId));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AiVendorBo bo) {
        bo.setCreateBy(getUsername());
        return toAjax(service.insert(bo).intValue());
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody AiVendorBo bo) {
        bo.setUpdateBy(getUsername());
        service.update(bo);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:remove')")
    @DeleteMapping("/{vendorIds}")
    public AjaxResult remove(@PathVariable Long[] vendorIds) {
        service.deleteByIds(vendorIds);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:default')")
    @PutMapping("/{vendorId}/default")
    public AjaxResult setDefault(@PathVariable Long vendorId) {
        service.setDefault(vendorId);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:test')")
    @PostMapping("/test")
    public AjaxResult test(@RequestBody AiVendorBo bo) {
        return AjaxResult.success(service.testConnect(bo));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:test')")
    @PostMapping("/{vendorId}/test")
    public AjaxResult testAndSave(@PathVariable Long vendorId) {
        return AjaxResult.success(service.testConnectAndSave(vendorId));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:query')")
    @GetMapping("/default")
    public AjaxResult getDefault() {
        return AjaxResult.success(service.getDefault());
    }
}
