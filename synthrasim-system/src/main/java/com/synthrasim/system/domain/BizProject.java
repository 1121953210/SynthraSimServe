package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.synthrasim.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目实体类 - 对应数据库表 biz_project
 *
 * 用于存储用户创建的建模仿真项目信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project")
public class BizProject extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String projectName;

    /** 项目简介 */
    private String description;

    /**
     * 项目封面图/缩略图相对路径
     *
     * 约定：文件上传后返回形如 /uploads/project/xxxx.jpg 的相对路径，
     * 前端保存该值到本字段。
     */
    private String coverImage;

    /** 项目状态：0=已完成，1=进行中 */
    private Integer status;

    /** 项目所有者（创建者）用户ID */
    private Long ownerId;

    /** 最后访问时间（用于“最近访问”排序） */
    private Date lastAccessTime;

    /** 逻辑删除标志：0=未删除，1=已删除 */
    @TableLogic
    private Integer isDeleted;
}

