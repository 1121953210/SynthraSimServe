package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.synthrasim.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;

/**
 * 组织机构实体类 - 对应数据库表 sys_organization
 * 
 * 存储组织机构的层级关系，通过parent_id自引用实现树结构。
 * 例如：A公司(parent_id=0) → B部门(parent_id=A的id) → C科室(parent_id=B的id)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_organization")
public class SysOrganization extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 机构ID - 主键自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 父级机构ID，0表示顶级机构 */
    private Long parentId;

    /** 机构名称 */
    private String orgName;

    /** 机构编码（唯一） */
    private String orgCode;

    /** 完整路径（如 A公司/B部门/C科室） */
    private String orgPath;

    /** 显示排序 */
    private Integer sortOrder;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 逻辑删除标志 */
    @TableLogic
    private Integer isDeleted;

    /** 子机构列表（构建树结构时填充，不映射到数据库） */
    @TableField(exist = false)
    private List<SysOrganization> children = new ArrayList<>();
}
