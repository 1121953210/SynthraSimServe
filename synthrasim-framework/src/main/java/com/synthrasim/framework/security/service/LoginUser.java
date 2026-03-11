package com.synthrasim.framework.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synthrasim.system.domain.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * 登录用户身份信息
 *
 * 实现Spring Security的UserDetails接口，是安全框架识别用户身份的核心对象。
 * 在JWT认证流程中：
 * 1. 登录成功后，将LoginUser存入Redis（以token为key）
 * 2. 每次请求时，JWT过滤器从Redis取出LoginUser放入SecurityContext
 * 3. 业务代码通过SecurityContextHolder获取当前用户信息
 *
 * 序列化注意事项：
 * - Redis使用Jackson序列化，所以必须用 @JsonIgnore（Jackson注解）
 * - 之前误用了 @JSONField（fastjson注解），Jackson不认识导致password等字段泄漏到Redis
 * - @JsonIgnoreProperties(ignoreUnknown=true) 兜底：反序列化时忽略JSON中多余的字段
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 用户唯一标识（UUID），作为Redis缓存的key */
    private String token;

    /** 登录时间（毫秒时间戳） */
    private Long loginTime;

    /** Token过期时间（毫秒时间戳） */
    private Long expireTime;

    /** 登录IP地址 */
    private String ipaddr;

    /** 用户信息（包含用户基本资料） */
    private SysUser user;

    /** 角色编码集合（如 ["admin", "user"]） */
    private Set<String> roles;

    /** 权限标识集合（如 ["system:user:list", "system:user:add"]） */
    private Set<String> permissions;

    public LoginUser() {
    }

    public LoginUser(SysUser user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    public LoginUser(Long userId, SysUser user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    /**
     * Spring Security通过此方法获取用户名
     * @JsonIgnore 防止Jackson将此方法序列化为JSON字段（避免与user.username重复）
     */
    @JsonIgnore
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Spring Security通过此方法获取密码（用于认证比对）
     * @JsonIgnore 防止密码被序列化到Redis中
     */
    @JsonIgnore
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户的权限/角色集合
     * 权限校验通过自定义注解+切面实现，此处返回null
     */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /** 账户是否未过期 */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 账户是否未锁定 */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 凭证是否未过期 */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 账户是否可用 */
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
