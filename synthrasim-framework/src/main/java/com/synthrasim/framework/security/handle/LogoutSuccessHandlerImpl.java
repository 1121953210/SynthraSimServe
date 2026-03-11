package com.synthrasim.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.utils.ServletUtils;
import com.synthrasim.common.utils.ip.IpUtils;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.TokenService;
import com.synthrasim.system.domain.SysLoginLog;
import com.synthrasim.system.service.ISysLoginLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 退出登录成功处理器
 *
 * 用户调用退出登录接口后，Spring Security会调用此处理器：
 * 1. 记录注销登录日志（operationType=2）
 * 2. 从Redis中删除用户的Token缓存
 * 3. 返回退出成功的JSON响应
 *
 * 注意：这里直接使用ISysLoginLogService而非SysLoginService来记录日志，
 * 避免与SecurityConfig之间产生循环依赖。
 */
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LogoutSuccessHandlerImpl.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysLoginLogService loginLogService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser != null) {
            // 1. 记录注销登录日志
            try {
                SysLoginLog loginLog = new SysLoginLog();
                loginLog.setUsername(loginUser.getUser().getUsername());
                loginLog.setUserId(loginUser.getUser().getId());
                loginLog.setOperationType(2);  // 注销登录
                loginLog.setLoginStatus(1);    // 操作成功
                loginLog.setIpAddress(IpUtils.getIpAddr(request));
                loginLog.setUserAgent(request.getHeader("User-Agent"));
                loginLog.setOperationTime(new Date());
                loginLogService.insertLoginLog(loginLog);
            } catch (Exception e) {
                log.error("记录注销日志异常: {}", e.getMessage());
            }

            // 2. 删除Redis中的Token缓存
            tokenService.delLoginUser(loginUser.getToken());

            log.info("用户 {} 退出登录", loginUser.getUsername());
        }

        // 3. 返回退出成功响应
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success("退出成功")));
    }
}
