package com.synthrasim.framework.security.service;

import com.synthrasim.common.constant.Constants;
import com.synthrasim.common.core.redis.RedisCache;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.common.utils.ServletUtils;
import com.synthrasim.common.utils.StringUtils;
import com.synthrasim.common.utils.ip.IpUtils;
import com.synthrasim.system.domain.SysLoginLog;
import com.synthrasim.system.service.ISysLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 登录业务服务
 *
 * 处理用户登录的完整流程：
 * 1. 验证码校验
 * 2. 用户名密码认证（委托给Spring Security）
 * 3. 记录登录日志（成功/失败都记录）
 * 4. 生成JWT Token
 */
@Service
public class SysLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysLoginLogService loginLogService;

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     验证码唯一标识
     * @return JWT Token字符串
     */
    public String login(String username, String password, String code, String uuid) {
        // 1. 验证码校验
        validateCaptcha(code, uuid);

        // 2. 调用Spring Security认证
        Authentication authentication;
        try {
            /*
             * AuthenticationManager.authenticate() 会触发以下流程：
             * → UserDetailsServiceImpl.loadUserByUsername() 查询用户
             * → BCryptPasswordEncoder 比对密码
             * → 认证成功返回Authentication对象，失败抛出异常
             */
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException e) {
            // 登录失败，记录失败日志
            recordLoginLog(username, null, 1, 0, "用户名或密码错误");
            throw new ServiceException("用户名或密码错误");
        } catch (Exception e) {
            // 登录失败，记录失败日志（其他异常，如账号被停用等）
            recordLoginLog(username, null, 1, 0, e.getMessage());
            throw new ServiceException(e.getMessage());
        }

        // 3. 认证成功，获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 4. 记录登录成功日志
        recordLoginLog(loginUser.getUser().getUsername(),
                loginUser.getUser().getId(), 1, 1, null);

        // 5. 生成Token返回
        return tokenService.createToken(loginUser);
    }

    /**
     * 记录登录日志
     *
     * @param username      用户名
     * @param userId        用户ID（登录失败时可能为null，因为用户可能不存在）
     * @param operationType 操作类型：1=登录，2=注销登录
     * @param loginStatus   操作结果：0=失败，1=成功
     * @param failReason    失败原因（成功时为null）
     */
    public void recordLoginLog(String username, Long userId, Integer operationType,
                                Integer loginStatus, String failReason) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();

            SysLoginLog log = new SysLoginLog();
            log.setUsername(username);
            log.setUserId(userId);
            log.setOperationType(operationType);
            log.setLoginStatus(loginStatus);
            log.setFailReason(failReason);
            log.setIpAddress(IpUtils.getIpAddr(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setOperationTime(new Date());

            loginLogService.insertLoginLog(log);
        } catch (Exception e) {
            // 日志记录失败不应影响主业务流程，仅打印异常
            org.slf4j.LoggerFactory.getLogger(SysLoginService.class)
                    .error("记录登录日志异常: {}", e.getMessage());
        }
    }

    /**
     * 验证码校验
     * 从Redis中取出验证码与用户输入的进行比对
     */
    private void validateCaptcha(String code, String uuid) {
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid)) {
            return;
        }
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new ServiceException("验证码已失效");
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new ServiceException("验证码错误");
        }
    }
}
