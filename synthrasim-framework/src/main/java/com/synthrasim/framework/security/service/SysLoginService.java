package com.synthrasim.framework.security.service;

import com.synthrasim.common.constant.Constants;
import com.synthrasim.common.core.redis.RedisCache;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.common.utils.SecurityUtils;
import com.synthrasim.common.utils.StringUtils;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 登录业务服务
 * 
 * 处理用户登录的完整流程：
 * 1. 验证码校验
 * 2. 用户名密码认证（委托给Spring Security）
 * 3. 生成JWT Token
 * 4. 记录登录日志
 */
@Service
public class SysLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisCache redisCache;

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
            throw new ServiceException("用户名或密码错误");
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }

        // 3. 认证成功，生成Token
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
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
