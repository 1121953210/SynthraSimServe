package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单/权限数据访问层
 *
 * 核心SQL：根据用户ID查询该用户拥有的所有权限标识（perms字段）
 * 查询链路：用户 → sys_user_role → 角色 → sys_role_menu → 菜单/权限
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询该用户拥有的所有权限标识
     *
     * SQL关联链路（RBAC三表联查）：
     *   sys_role_menu（角色拥有哪些权限）
     *     JOIN sys_user_role（用户拥有哪些角色）
     *     JOIN sys_menu（权限的详细信息，取perms字段）
     *
     * 只查询 menuType='F'（按钮/操作权限）且状态正常的记录
     * 返回的是权限标识字符串列表，如 ["system:user:list", "system:user:add"]
     */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "AND m.menu_type = 'F' " +
            "AND m.status = 1 " +
            "AND m.is_deleted = 0 " +
            "AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有权限标识（admin用户直接赋予全部权限时可用）
     */
    @Select("SELECT DISTINCT perms FROM sys_menu " +
            "WHERE menu_type = 'F' AND status = 1 AND is_deleted = 0 " +
            "AND perms IS NOT NULL AND perms != ''")
    List<String> selectAllPerms();
}
