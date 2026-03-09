package com.synthrasim.generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 代码生成-数据表实体
 * 
 * 存储从数据库中读取的表信息，用于代码生成时作为元数据。
 * 包含表名、表描述、对应的Java类名等信息。
 */
@Data
@TableName("gen_table")
public class GenTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long tableId;

    /** 数据库表名（如 biz_project） */
    private String tableName;

    /** 表描述（如"项目表"） */
    private String tableComment;

    /** 生成的Java类名（如 BizProject） */
    private String className;

    /** 生成包路径（如 com.synthrasim.system） */
    private String packageName;

    /** 生成模块名（如 system） */
    private String moduleName;

    /** 生成业务名（如 project） */
    private String businessName;

    /** 生成功能名（中文，如"项目管理"） */
    private String functionName;

    /** 作者 */
    private String functionAuthor;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 备注 */
    private String remark;

    /** 表的列信息（非数据库字段） */
    @TableField(exist = false)
    private List<GenTableColumn> columns;
}
