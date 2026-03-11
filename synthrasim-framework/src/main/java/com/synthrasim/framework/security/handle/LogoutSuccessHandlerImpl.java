package com.synthrasim.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.utils.ServletUtils;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.SysLoginService;
import com.synthrasim.framework.security.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 退出登录成功处理器
 *
 * 用户调用退出登录接口后，Spring Security会调用此处理器：
 * 1. 记录注销登录日志（operationType=2）
 * 2. 从Redis中删除用户的Token缓存
 * 3. 返回退出成功的JSON响应
 */
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LogoutSuccessHandlerImpl.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLoginService loginService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser != null) {
            // 1. 记录注销登录日志（operationType=2表示注销）
            loginService.recordLoginLog(
                    loginUser.getUser().getUsername(),
                    loginUser.getUser().getId(),
                    2,  // 注销登录
                    1,  // 操作成功
                    null
            );

            // 2. 删除Redis中的Token缓存
            tokenService.delLoginUser(loginUser.getToken());

            log.info("用户 {} 退出登录", loginUser.getUsername());
        }

        // 3. 返回退出成功响应
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success("退出成功")));
    }
}
