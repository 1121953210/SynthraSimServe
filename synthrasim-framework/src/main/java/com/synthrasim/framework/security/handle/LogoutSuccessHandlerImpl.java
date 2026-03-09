package com.synthrasim.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.synthrasim.common.constant.HttpStatus;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.utils.ServletUtils;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.TokenService;
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
 * 1. 从Redis中删除用户的Token缓存
 * 2. 返回退出成功的JSON响应
 */
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Autowired
    private TokenService tokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser != null) {
            // 删除Redis中的Token缓存
            tokenService.delLoginUser(loginUser.getToken());
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success("退出成功")));
    }
}
