package com.synthrasim.framework.security.service;

import com.synthrasim.common.constant.Constants;
import com.synthrasim.common.core.redis.RedisCache;
import com.synthrasim.common.utils.ServletUtils;
import com.synthrasim.common.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token令牌服务
 * 
 * JWT + Redis 双重机制实现无状态认证：
 * 
 * 【登录流程】
 * 1. 用户名密码验证通过
 * 2. 生成一个随机UUID作为用户唯一标识
 * 3. 将LoginUser对象存入Redis（key = login_tokens:{uuid}）
 * 4. 将UUID写入JWT的payload，生成JWT字符串返回给前端
 * 
 * 【请求认证流程】
 * 1. 从请求头Authorization中取出JWT
 * 2. 解析JWT获取UUID
 * 3. 用UUID从Redis中取出LoginUser
 * 4. 将LoginUser放入SecurityContext
 * 
 * 这种设计的好处：
 * - JWT本身不存储用户详细信息，减小Token体积
 * - 用户信息存在Redis中，支持实时更新（如角色变更、强制下线）
 * - Token续期只需刷新Redis过期时间，无需重新签发JWT
 */
@Component
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    /** JWT签名密钥（从配置文件读取） */
    @Value("${token.secret:synthrasimSecretKey2026}")
    private String secret;

    /** Token有效期，单位分钟（默认720分钟 = 12小时） */
    @Value("${token.expireTime:720}")
    private int expireTime;

    /** Token自动续期阈值：当剩余有效期小于此值时自动续期（默认120分钟） */
    private static final long MILLIS_MINUTE_TEN = 120 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 创建Token
     * 
     * @param loginUser 已认证的用户信息
     * @return JWT字符串
     */
    public String createToken(LoginUser loginUser) {
        // 1. 生成UUID作为用户标识
        String userKey = UUID.randomUUID().toString().replaceAll("-", "");
        loginUser.setToken(userKey);

        // 2. 设置登录时间和过期时间
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * 60 * 1000L);

        // 3. 将LoginUser存入Redis
        String redisKey = Constants.LOGIN_TOKEN_KEY + userKey;
        redisCache.setCacheObject(redisKey, loginUser, expireTime, TimeUnit.MINUTES);

        // 4. 生成JWT（payload中只存UUID）
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, userKey);
        return createJwtToken(claims);
    }

    /**
     * 从HTTP请求中获取LoginUser
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String redisKey = Constants.LOGIN_TOKEN_KEY + uuid;
                return redisCache.getCacheObject(redisKey);
            } catch (Exception e) {
                log.error("Token解析失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取当前请求的LoginUser（便捷方法）
     */
    public LoginUser getLoginUser() {
        return getLoginUser(ServletUtils.getRequest());
    }

    /**
     * 验证Token有效期，如果即将过期则自动续期
     * 
     * 自动续期机制：当Token剩余有效期不足120分钟时，
     * 自动将Redis中的过期时间延长，实现"活跃用户不掉线"的效果。
     */
    public void verifyToken(LoginUser loginUser) {
        long currentTime = System.currentTimeMillis();
        long expTime = loginUser.getExpireTime();
        if (expTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新Token过期时间
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * 60 * 1000L);
        String redisKey = Constants.LOGIN_TOKEN_KEY + loginUser.getToken();
        redisCache.setCacheObject(redisKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 删除用户Token（退出登录时调用）
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String redisKey = Constants.LOGIN_TOKEN_KEY + token;
            redisCache.deleteObject(redisKey);
        }
    }

    /**
     * 从请求头中提取JWT字符串
     * 格式：Authorization: Bearer {jwt_token}
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return token;
    }

    /** 生成JWT字符串 */
    private String createJwtToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /** 解析JWT获取payload */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
