package com.synthrasim.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 实体基类
 * 
 * 所有业务实体类均应继承此类，统一管理公共字段：
 * - 创建时间/更新时间（由MyBatisPlus自动填充）
 * - 创建人/更新人
 * - 备注信息
 * - 请求参数（用于接收前端传递的额外查询条件）
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建者ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 - 插入时自动填充 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新者ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 更新时间 - 插入和更新时自动填充 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 备注 */
    @TableField(exist = false)
    private String remark;

    /**
     * 请求参数映射
     * 用于接收前端传递的动态查询条件，不映射到数据库字段
     * 例如：日期范围查询 params.beginTime / params.endTime
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }
}
