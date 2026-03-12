package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.synthrasim.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单/权限实体类 - 对应数据库表 sys_menu
 *
 * ============================================================
 * 【权限模型说明】
 *
 * 本系统采用 RBAC（Role-Based Access Control）权限模型：
 *   用户 → 角色 → 权限（菜单）
 *
 * 一个用户可以有多个角色（通过 sys_user_role 关联）
 * 一个角色可以有多个权限（通过 sys_role_menu 关联）
 *
 * 权限标识（perms字段）的命名规范：
 *   格式：{模块}:{业务}:{操作}
 *   示例：
 *     system:user:list      → 用户管理 - 查看列表
 *     system:user:query     → 用户管理 - 查看详情
 *     system:user:add       → 用户管理 - 新增
 *     system:user:edit      → 用户管理 - 修改
 *     system:user:remove    → 用户管理 - 删除
 *     system:user:resetPwd  → 用户管理 - 重置密码
 *     system:role:list      → 角色管理 - 查看列表
 *     system:role:add       → 角色管理 - 新增
 *
 * 在Controller方法上标注 @RequiresPermissions("system:user:list")
 * 框架切面会从当前用户的权限集合中检查是否包含该标识
 * ============================================================
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 菜单名称（如 "用户管理"、"查看用户列表"） */
    private String menuName;

    /** 父菜单ID，0表示顶级菜单 */
    private Long parentId;

    /** 显示排序 */
    private Integer sortOrder;

    /** 路由地址（前端路由用，如 "/system/user"） */
    private String path;

    /**
     * 权限标识 —— 这是权限系统的核心字段
     *
     * 格式：模块:业务:操作
     * 例如：system:user:list、system:role:add
     *
     * Controller方法上的 @RequiresPermissions("system:user:list")
     * 就是与这个字段进行比对
     */
    private String perms;

    /**
     * 菜单类型
     * M = 目录（如"系统管理"）
     * C = 菜单（如"用户管理"，对应一个页面）
     * F = 按钮（如"新增用户"，对应一个接口权限）
     *
     * 权限校验主要关注 F 类型的记录，它代表一个具体的操作权限
     */
    private String menuType;

    /** 菜单状态：0=隐藏，1=显示 */
    private Integer visible;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 菜单图标 */
    private String icon;

    /** 逻辑删除标志 */
    @TableLogic
    private Integer isDeleted;

    /** 子菜单列表（构建树结构时使用） */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
