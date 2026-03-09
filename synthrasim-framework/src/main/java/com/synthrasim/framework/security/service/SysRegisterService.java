package com.synthrasim.framework.security.service;

import com.synthrasim.common.constant.Constants;
import com.synthrasim.common.core.redis.RedisCache;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.common.utils.SecurityUtils;
import com.synthrasim.common.utils.StringUtils;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户注册服务
 * 
 * 处理新用户注册的完整流程：
 * 1. 验证码校验
 * 2. 用户名唯一性校验
 * 3. 密码加密
 * 4. 写入数据库
 */
@Service
public class SysRegisterService {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 用户注册
     *
     * @param user     注册信息
     * @param code     验证码
     * @param uuid     验证码唯一标识
     */
    public void register(SysUser user, String code, String uuid) {
        // 1. 验证码校验
        validateCaptcha(code, uuid);

        // 2. 校验用户名唯一
        if (!userService.checkUsernameUnique(user.getUsername())) {
            throw new ServiceException("注册用户'" + user.getUsername() + "'失败，该用户名已被注册");
        }

        // 3. 密码加密（BCrypt）
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));

        // 4. 设置默认状态为启用
        user.setStatus(1);

        // 5. 写入数据库
        boolean result = userService.registerUser(user);
        if (!result) {
            throw new ServiceException("注册失败，请联系管理员");
        }
    }

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
