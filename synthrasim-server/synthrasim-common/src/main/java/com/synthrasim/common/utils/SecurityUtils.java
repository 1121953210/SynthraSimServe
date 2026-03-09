package com.synthrasim.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security工具类
 * 
 * 提供便捷方法获取当前登录用户信息、密码加密/匹配等操作。
 * 基于SecurityContextHolder获取线程级别的安全上下文。
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的Authentication对象
     * SecurityContextHolder使用ThreadLocal存储，每个请求线程独立
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        try {
            return getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * BCrypt密码加密
     * BCrypt是一种单向Hash加密算法，每次加密结果不同（自带随机盐值），安全性高
     *
     * @param password 明文密码
     * @return 加密后的密码字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 验证明文密码与加密密码是否匹配
     *
     * @param rawPassword     明文密码（用户输入）
     * @param encodedPassword 加密密码（数据库存储）
     * @return true=匹配，false=不匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
