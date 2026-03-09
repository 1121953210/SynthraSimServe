package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.synthrasim.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.util.List;

/**
 * 用户实体类 - 对应数据库表 sys_user
 * 
 * 存储平台所有用户的基本信息，包括：
 * - 账号凭证（用户名、密码）
 * - 个人资料（姓名、头像、邮箱、手机等）
 * - 组织归属（org_id关联组织机构表）
 * - 账号状态（启用/禁用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID - 主键自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户名（登录账号，唯一） */
    private String username;

    /**
     * 密码（BCrypt加密存储）
     * @JsonIgnore 防止密码在JSON序列化时泄露到前端
     */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 头像URL */
    private String avatar;

    /** 电子邮箱 */
    private String email;

    /** 手机号码 */
    private String phone;

    /** 办公电话 */
    private String officePhone;

    /** 工作地 */
    private String workLocation;

    /** 所属组织机构ID */
    private Long orgId;

    /** 账号状态：0=禁用，1=启用 */
    private Integer status;

    /** 逻辑删除标志：0=未删除，1=已删除 */
    @TableLogic
    private Integer isDeleted;

    // ===== 以下为非数据库字段，用于业务扩展 =====

    /** 组织机构名称（联表查询时填充，不映射到数据库） */
    @TableField(exist = false)
    private String orgName;

    /** 组织机构路径（如 A公司/B部门/C科室） */
    @TableField(exist = false)
    private String orgPath;

    /** 用户关联的角色列表（多对多关系，通过sys_user_role中间表关联） */
    @TableField(exist = false)
    private List<SysRole> roles;

    /** 角色ID列表（前端分配角色时提交） */
    @TableField(exist = false)
    private Long[] roleIds;
}
