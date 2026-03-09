package com.synthrasim.framework.security.service;

import com.synthrasim.common.enums.UserStatus;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.service.ISysRoleService;
import com.synthrasim.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户认证服务 - Spring Security核心
 * 
 * Spring Security在执行认证时会调用loadUserByUsername方法：
 * 1. 根据用户名查询数据库获取用户信息
 * 2. 校验用户状态（是否禁用/删除）
 * 3. 查询用户的角色和权限
 * 4. 构建LoginUser对象返回给Security框架
 * 5. Security框架自动比对密码（BCrypt）
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户（支持用户名/手机号/邮箱登录）
        SysUser user = userService.selectUserByUsername(username);
        if (user == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("用户名或密码错误");
        }

        // 2. 校验用户状态
        if (UserStatus.DISABLE.getCode() == user.getStatus()) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号已被停用，请联系管理员");
        }

        // 3. 获取用户权限信息
        Set<String> permissions = getPermissions(user);

        // 4. 构建LoginUser返回
        LoginUser loginUser = new LoginUser(user.getId(), user, permissions);
        loginUser.setRoles(roleService.selectRoleCodesByUserId(user.getId()));

        return loginUser;
    }

    /**
     * 获取用户权限集合
     * admin角色拥有所有权限（用*:*:*表示）
     */
    private Set<String> getPermissions(SysUser user) {
        Set<String> permissions = new HashSet<>();
        Set<String> roleCodes = roleService.selectRoleCodesByUserId(user.getId());
        if (roleCodes.contains("admin")) {
            permissions.add("*:*:*");
        } else {
            // 非管理员用户根据角色获取具体权限（后续可扩展菜单权限表）
            for (String roleCode : roleCodes) {
                permissions.add("role:" + roleCode);
            }
        }
        return permissions;
    }
}
