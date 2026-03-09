package com.synthrasim.web.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.synthrasim.common.annotation.Log;
import com.synthrasim.common.annotation.RequiresPermissions;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.core.page.TableDataInfo;
import com.synthrasim.common.utils.SecurityUtils;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.service.ISysRoleService;
import com.synthrasim.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 
 * 提供用户CRUD操作的RESTful接口，需要相应权限才能访问。
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    /**
     * 查询用户列表（分页）
     */
    @ApiOperation("查询用户列表")
    @RequiresPermissions("system:user:list")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        // 使用MyBatisPlus分页
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> result = userService.page(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .like(user.getUsername() != null, SysUser::getUsername, user.getUsername())
                        .like(user.getRealName() != null, SysUser::getRealName, user.getRealName())
                        .eq(user.getStatus() != null, SysUser::getStatus, user.getStatus())
                        .orderByDesc(SysUser::getCreateTime));
        return new TableDataInfo(result.getRecords(), result.getTotal());
    }

    /**
     * 根据ID查询用户详情
     */
    @ApiOperation("查询用户详情")
    @RequiresPermissions("system:user:query")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId) {
        SysUser user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            user.setRoles(roleService.selectRolesByUserId(userId));
        }
        return AjaxResult.success(user);
    }

    /**
     * 新增用户
     */
    @ApiOperation("新增用户")
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = "INSERT")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (!userService.checkUsernameUnique(user.getUsername())) {
            return AjaxResult.error("新增用户'" + user.getUsername() + "'失败，用户名已存在");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return userService.save(user) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 修改用户
     */
    @ApiOperation("修改用户")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = "UPDATE")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        if (!userService.checkEmailUnique(user)) {
            return AjaxResult.error("修改用户'" + user.getUsername() + "'失败，邮箱已被使用");
        }
        if (!userService.checkPhoneUnique(user)) {
            return AjaxResult.error("修改用户'" + user.getUsername() + "'失败，手机号已被使用");
        }
        user.setPassword(null);
        return userService.updateById(user) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 删除用户（逻辑删除）
     */
    @ApiOperation("删除用户")
    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = "DELETE")
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        for (Long userId : userIds) {
            userService.removeById(userId);
        }
        return AjaxResult.success();
    }

    /**
     * 重置用户密码
     */
    @ApiOperation("重置密码")
    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "用户管理", businessType = "UPDATE")
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return userService.updateById(user) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 给用户分配角色
     */
    @ApiOperation("分配角色")
    @RequiresPermissions("system:user:edit")
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(@RequestParam Long userId, @RequestParam Long[] roleIds) {
        userService.insertUserAuth(userId, roleIds);
        return AjaxResult.success();
    }
}
