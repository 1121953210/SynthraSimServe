package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.synthrasim.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类 - 对应数据库表 sys_role
 * 
 * 定义系统中的角色，用于RBAC（基于角色的访问控制）权限模型。
 * 一个用户可以拥有多个角色，一个角色可以分配给多个用户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色ID - 主键自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 角色名称（如"管理员"、"普通用户"） */
    private String roleName;

    /** 角色编码（如"admin"、"user"，用于程序内部权限判断） */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 逻辑删除标志 */
    @TableLogic
    private Integer isDeleted;
}
