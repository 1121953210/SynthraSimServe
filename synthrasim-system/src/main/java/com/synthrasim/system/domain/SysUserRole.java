package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-角色关联实体 - 对应数据库表 sys_user_role
 * 
 * 用户与角色的多对多中间表。
 * 一条记录表示"某个用户拥有某个角色"。
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}
