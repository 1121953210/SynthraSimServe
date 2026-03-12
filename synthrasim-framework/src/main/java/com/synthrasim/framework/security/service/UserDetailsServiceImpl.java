package com.synthrasim.framework.security.service;

import com.synthrasim.common.enums.UserStatus;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.mapper.SysMenuMapper;
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
import java.util.List;
import java.util.Set;

/**
 * 用户认证服务 - Spring Security核心
 *
 * ============================================================
 * 【权限写入Redis的完整流程】
 *
 * 1. 用户调用 POST /login 提交用户名密码
 * 2. SysLoginService 调用 authenticationManager.authenticate()
 * 3. Spring Security 内部调用本类的 loadUserByUsername() 方法
 * 4. 本方法中：
 *    a) 查数据库获取用户信息（sys_user表）
 *    b) 校验用户状态（是否禁用）
 *    c) 查数据库获取用户角色（sys_user_role → sys_role）
 *    d) 查数据库获取用户权限（sys_user_role → sys_role_menu → sys_menu.perms）
 *       ★ 这一步就是 permissions 的来源！
 *       ★ admin角色 → 写入 "*:*:*"（通配所有权限）
 *       ★ 普通角色 → 写入该角色关联的所有权限标识，如：
 *         ["system:user:list", "system:user:query", "system:role:list"]
 *    e) 将 permissions 和 roles 设置到 LoginUser 对象中
 * 5. 认证成功后，SysLoginService 调用 tokenService.createToken(loginUser)
 * 6. TokenService 将整个 LoginUser（包含permissions）序列化存入 Redis
 *    Redis Key: login_tokens:{uuid}
 *    Redis Value: LoginUser对象（含user信息、roles、permissions）
 * 7. 后续每次请求：
 *    JwtAuthenticationFilter → 解析JWT → 从Redis取出LoginUser
 *    → PermissionAspect 检查 loginUser.getPermissions() 是否包含所需权限
 *
 * 【权限标识怎么对应到Controller方法？】
 *
 * 数据库 sys_menu 表中存储：
 *   perms = "system:user:list"
 *
 * Controller方法上标注：
 *   @RequiresPermissions("system:user:list")
 *
 * 切面 PermissionAspect 做比对：
 *   loginUser.getPermissions().contains("system:user:list") → 通过
 *   loginUser.getPermissions().contains("system:user:add")  → 不通过 → 403
 * ============================================================
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ===== 第一步：查询用户基本信息 =====
        SysUser user = userService.selectUserByUsername(username);
        if (user == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("用户名或密码错误");
        }

        // ===== 第二步：校验用户状态 =====
        if (UserStatus.DISABLE.getCode() == user.getStatus()) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号已被停用，请联系管理员");
        }

        // ===== 第三步：查询角色编码集合 =====
        // 从 sys_user_role + sys_role 联查，得到如 ["admin"] 或 ["user", "pm"]
        Set<String> roles = roleService.selectRoleCodesByUserId(user.getId());

        // ===== 第四步：查询权限标识集合（★ 这是permissions写入的关键 ★） =====
        Set<String> permissions = getPermissions(user.getId(), roles);

        log.info("用户 {} 登录成功，角色：{}，权限数量：{}", username, roles, permissions.size());

        // ===== 第五步：构建LoginUser返回 =====
        // 这个LoginUser后续会被TokenService存入Redis
        LoginUser loginUser = new LoginUser(user.getId(), user, permissions);
        loginUser.setRoles(roles);

        return loginUser;
    }

    /**
     * 获取用户权限集合
     *
     * @param userId 用户ID
     * @param roles  用户的角色编码集合
     * @return 权限标识集合，如 {"system:user:list", "system:user:query", ...}
     *
     * 权限来源：sys_menu 表的 perms 字段
     * 查询链路：用户ID → sys_user_role → sys_role_menu → sys_menu.perms
     */
    private Set<String> getPermissions(Long userId, Set<String> roles) {
        Set<String> permissions = new HashSet<>();

        if (roles.contains("admin")) {
            // ===== admin角色：拥有所有权限 =====
            // 写入 "*:*:*" 通配符，PermissionAspect 检测到此标识直接放行
            permissions.add("*:*:*");
        } else {
            // ===== 非admin角色：从数据库查询具体权限 =====
            // SQL: SELECT DISTINCT m.perms FROM sys_menu m
            //      JOIN sys_role_menu rm ON m.id = rm.menu_id
            //      JOIN sys_user_role ur ON rm.role_id = ur.role_id
            //      WHERE ur.user_id = ?
            //      AND m.menu_type = 'F' AND m.status = 1
            List<String> perms = menuMapper.selectPermsByUserId(userId);
            permissions.addAll(perms);
        }


        return permissions;
    }
}
