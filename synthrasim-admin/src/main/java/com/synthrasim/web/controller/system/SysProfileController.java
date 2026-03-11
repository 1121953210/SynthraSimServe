package com.synthrasim.web.controller.system;

import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.utils.SecurityUtils;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.TokenService;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 *
 * 对应原型设计中的"用户个人信息"页面，提供：
 * - 查看个人基本信息
 * - 修改个人资料（邮箱、手机号、办公电话、工作地）
 * - 修改密码
 * - 修改头像
 *
 * 路径说明：类级别 @RequestMapping("/system/user/profile")
 * 每个方法必须声明明确的子路径，否则会与 SysUserController 的
 * @GetMapping("/{userId}") 路径变量产生冲突，导致Spring将 "profile"
 * 当作 userId 解析并抛出 NumberFormatException。
 */
@Api(tags = "个人中心")
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 获取个人信息
     */
    @ApiOperation("获取个人信息")
    @GetMapping("/info")
    public AjaxResult profile() {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser user = loginUser.getUser();
        user.setPassword(null);
        return AjaxResult.success(user);
    }

    /**
     * 修改个人基本信息
     *
     * 对应原型中基础信息的"保存"按钮。
     * 可修改字段：电子邮箱、手机号码、办公电话、工作地
     * 只读字段（不可修改）：姓名、用户名、组织机构
     */
    @ApiOperation("修改个人信息")
    @PutMapping("/update")
    public AjaxResult updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser currentUser = loginUser.getUser();

        // 构建更新对象，只允许修改可编辑字段
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUser.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setOfficePhone(user.getOfficePhone());
        updateUser.setWorkLocation(user.getWorkLocation());

        // 校验邮箱和手机号唯一性
        updateUser.setUsername(currentUser.getUsername());
        if (!userService.checkEmailUnique(updateUser)) {
            return AjaxResult.error("修改失败，邮箱已被使用");
        }
        if (!userService.checkPhoneUnique(updateUser)) {
            return AjaxResult.error("修改失败，手机号已被使用");
        }

        if (userService.updateUserProfile(updateUser) > 0) {
            // 更新缓存中的用户信息
            currentUser.setEmail(user.getEmail());
            currentUser.setPhone(user.getPhone());
            currentUser.setOfficePhone(user.getOfficePhone());
            currentUser.setWorkLocation(user.getWorkLocation());
            tokenService.refreshToken(loginUser);
            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("修改失败，请联系管理员");
    }

    /**
     * 修改密码
     *
     * 对应用户头像下拉菜单中的"修改密码"功能
     */
    @ApiOperation("修改密码")
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestParam String oldPassword, @RequestParam String newPassword) {
        LoginUser loginUser = tokenService.getLoginUser();
        String encryptedPassword = loginUser.getUser().getPassword();

        // 校验旧密码是否正确
        if (!SecurityUtils.matchesPassword(oldPassword, encryptedPassword)) {
            return AjaxResult.error("修改密码失败，旧密码错误");
        }

        // 校验新旧密码不能相同
        if (SecurityUtils.matchesPassword(newPassword, encryptedPassword)) {
            return AjaxResult.error("新密码不能与旧密码相同");
        }

        // 更新密码
        String newEncryptedPwd = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(loginUser.getUsername(), newEncryptedPwd) > 0) {
            // 更新缓存中的密码
            loginUser.getUser().setPassword(newEncryptedPwd);
            tokenService.refreshToken(loginUser);
            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("修改密码失败，请联系管理员");
    }

    /**
     * 修改头像
     */
    @ApiOperation("修改头像")
    @PutMapping("/avatar")
    public AjaxResult avatar(@RequestParam String avatar) {
        LoginUser loginUser = tokenService.getLoginUser();
        if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
            loginUser.getUser().setAvatar(avatar);
            tokenService.refreshToken(loginUser);
            return AjaxResult.success("修改成功").put("imgUrl", avatar);
        }
        return AjaxResult.error("修改头像失败，请联系管理员");
    }
}
