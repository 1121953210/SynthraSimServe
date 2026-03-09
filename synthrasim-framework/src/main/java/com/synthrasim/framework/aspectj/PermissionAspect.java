package com.synthrasim.framework.aspectj;

import com.synthrasim.common.annotation.RequiresPermissions;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.common.constant.HttpStatus;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.TokenService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验切面
 * 
 * 拦截所有标注了 @RequiresPermissions 注解的Controller方法，
 * 在方法执行前校验当前用户是否具备所需权限。
 * 
 * 权限校验逻辑：
 * 1. 获取当前登录用户的权限集合
 * 2. 判断是否包含注解中声明的权限标识
 * 3. admin角色拥有通配权限 "*:*:*"，跳过校验
 * 4. 不具备权限时抛出403异常
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private TokenService tokenService;

    @Around("@annotation(requiresPermissions)")
    public Object around(ProceedingJoinPoint point, RequiresPermissions requiresPermissions) throws Throwable {
        // 获取当前登录用户
        LoginUser loginUser = tokenService.getLoginUser();
        if (loginUser == null) {
            throw new ServiceException("认证失败，请重新登录", HttpStatus.UNAUTHORIZED);
        }

        // 获取用户权限集合
        Set<String> permissions = loginUser.getPermissions();

        // admin拥有所有权限
        if (permissions.contains("*:*:*")) {
            return point.proceed();
        }

        // 获取注解中声明的所需权限
        String[] requiredPerms = requiresPermissions.value();
        RequiresPermissions.Logical logical = requiresPermissions.logical();

        boolean hasPermission;
        if (logical == RequiresPermissions.Logical.AND) {
            // AND模式：所有权限都必须具备
            hasPermission = true;
            for (String perm : requiredPerms) {
                if (!permissions.contains(perm)) {
                    hasPermission = false;
                    break;
                }
            }
        } else {
            // OR模式：任一权限即可
            hasPermission = false;
            for (String perm : requiredPerms) {
                if (permissions.contains(perm)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            throw new ServiceException("没有权限访问此资源", HttpStatus.FORBIDDEN);
        }

        return point.proceed();
    }
}
