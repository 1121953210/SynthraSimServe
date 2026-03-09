package com.synthrasim.web.controller.system;

import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.core.domain.model.LoginBody;
import com.synthrasim.common.core.domain.model.RegisterBody;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.SysLoginService;
import com.synthrasim.framework.security.service.SysRegisterService;
import com.synthrasim.framework.security.service.TokenService;
import com.synthrasim.system.domain.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录/注册控制器
 * 
 * 提供用户认证相关的接口：
 * - POST /login      用户登录，返回JWT Token
 * - POST /register   用户注册
 * - GET  /getInfo    获取当前登录用户信息
 * - POST /logout     退出登录（由Spring Security的LogoutFilter处理）
 */
@Api(tags = "用户认证")
@RestController
public class SysLoginController {

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private TokenService tokenService;

    /**
     * 用户登录接口
     * 
     * 前端提交用户名、密码、验证码，验证通过后返回JWT Token。
     * 前端需要将Token存储到localStorage，后续每次请求在Header中携带：
     * Authorization: Bearer {token}
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        String token = loginService.login(
                loginBody.getUsername(),
                loginBody.getPassword(),
                loginBody.getCode(),
                loginBody.getUuid()
        );
        return AjaxResult.success("登录成功").put("token", token);
    }

    /**
     * 用户注册接口
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody registerBody) {
        SysUser user = new SysUser();
        user.setUsername(registerBody.getUsername());
        user.setPassword(registerBody.getPassword());
        user.setRealName(registerBody.getRealName());
        user.setEmail(registerBody.getEmail());
        user.setPhone(registerBody.getPhone());
        registerService.register(user, registerBody.getCode(), registerBody.getUuid());
        return AjaxResult.success("注册成功");
    }

    /**
     * 获取当前登录用户信息
     * 
     * 前端登录成功后调用此接口获取用户详细信息（姓名、角色、权限等），
     * 用于页面展示和前端路由权限控制。
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = tokenService.getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error(401, "认证失败，请重新登录");
        }
        SysUser user = loginUser.getUser();
        // 脱敏：不返回密码
        user.setPassword(null);
        return AjaxResult.success()
                .put("user", user)
                .put("roles", loginUser.getRoles())
                .put("permissions", loginUser.getPermissions());
    }
}
