package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-菜单/权限关联实体 - 对应数据库表 sys_role_menu
 *
 * 角色与权限的多对多中间表。
 * 一条记录表示"某个角色拥有某个权限"。
 *
 * 例如：
 *   role_id=2(普通用户), menu_id=101(system:user:list)
 *   → 普通用户角色拥有"查看用户列表"权限
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 菜单/权限ID */
    private Long menuId;
}
