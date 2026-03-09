package com.synthrasim.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.synthrasim.common.constant.HttpStatus;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.utils.ServletUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 认证失败处理器
 * 
 * 当用户访问需要认证的接口但未提供有效Token时，
 * Spring Security会调用此处理器返回401错误。
 * 
 * 替代默认的重定向到登录页行为，改为返回JSON格式的错误信息，
 * 适配前后端分离架构。
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) {
        String msg = "认证失败，请重新登录";
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpStatus.UNAUTHORIZED, msg)));
    }
}
