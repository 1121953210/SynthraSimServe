package com.synthrasim.web.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.synthrasim.common.annotation.Log;
import com.synthrasim.common.annotation.RequiresPermissions;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.core.page.TableDataInfo;
import com.synthrasim.system.domain.SysRole;
import com.synthrasim.system.service.ISysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private ISysRoleService roleService;

    @ApiOperation("查询角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping("/list")
    public TableDataInfo list(SysRole role,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        IPage<SysRole> result = roleService.page(page);
        return new TableDataInfo(result.getRecords(), result.getTotal());
    }

    @ApiOperation("查询所有角色")
    @GetMapping("/all")
    public AjaxResult all() {
        return AjaxResult.success(roleService.selectRoleAll());
    }

    @ApiOperation("查询角色详情")
    @RequiresPermissions("system:role:query")
    @GetMapping("/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId) {
        return AjaxResult.success(roleService.getById(roleId));
    }

    @ApiOperation("新增角色")
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = "INSERT")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role) {
        return roleService.save(role) ? AjaxResult.success() : AjaxResult.error();
    }

    @ApiOperation("修改角色")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = "UPDATE")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role) {
        return roleService.updateById(role) ? AjaxResult.success() : AjaxResult.error();
    }

    @ApiOperation("删除角色")
    @RequiresPermissions("system:role:remove")
    @Log(title = "角色管理", businessType = "DELETE")
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds) {
        for (Long roleId : roleIds) {
            roleService.removeById(roleId);
        }
        return AjaxResult.success();
    }
}
