package com.synthrasim.generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

/**
 * 代码生成-表字段实体
 * 
 * 存储从数据库中读取的字段信息，包含字段名、类型、注释等。
 * 用于生成Entity类的属性、Mapper的SQL映射等。
 */
@Data
@TableName("gen_table_column")
public class GenTableColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long columnId;

    /** 归属表编号 */
    private Long tableId;

    /** 数据库字段名（如 project_name） */
    private String columnName;

    /** 字段描述/注释 */
    private String columnComment;

    /** 数据库字段类型（如 varchar(128)、bigint） */
    private String columnType;

    /** Java属性类型（如 String、Long、Integer） */
    private String javaType;

    /** Java属性名（驼峰命名，如 projectName） */
    private String javaField;

    /** 是否主键（1是） */
    private String isPk;

    /** 是否自增（1是） */
    private String isIncrement;

    /** 是否必填（1是） */
    private String isRequired;

    /** 是否在列表中显示（1是） */
    private String isList;

    /** 是否可查询（1是） */
    private String isQuery;

    /** 是否可编辑（1是） */
    private String isEdit;

    /** 查询方式（等于EQ、模糊LIKE、范围BETWEEN等） */
    private String queryType;

    /** 显示类型（文本框input、文本域textarea、下拉框select等） */
    private String htmlType;

    /** 排序 */
    private Integer sort;
}
