package com.synthrasim.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 
 * 标注在Controller方法上，表示调用该接口需要具备指定的权限标识。
 * 框架模块中的PermissionAspect切面会拦截此注解，从当前登录用户的权限列表中
 * 检查是否包含所需权限，若不具备则抛出权限不足异常。
 * 
 * 使用示例：
 *   @RequiresPermissions("system:user:list")    // 需要用户列表查看权限
 *   @RequiresPermissions("system:user:add")     // 需要用户新增权限
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {

    /** 需要校验的权限标识（如 "system:user:list"） */
    String[] value();

    /**
     * 验证模式：AND=所有权限都需具备，OR=任一权限即可
     * 默认AND
     */
    Logical logical() default Logical.AND;

    /** 逻辑运算枚举 */
    enum Logical {
        AND, OR
    }
}
