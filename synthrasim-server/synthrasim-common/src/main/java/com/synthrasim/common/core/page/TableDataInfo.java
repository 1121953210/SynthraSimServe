package com.synthrasim.common.core.page;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 分页查询统一响应结构
 * 
 * 所有分页查询接口统一使用此类返回，包含：
 * - total：总记录数
 * - rows：当前页数据列表
 * - code：状态码
 * - msg：提示消息
 */
@Data
@NoArgsConstructor
public class TableDataInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<?> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /**
     * 构建分页结果
     *
     * @param list  当前页数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total) {
        this.rows = list;
        this.total = total;
        this.code = 200;
        this.msg = "查询成功";
    }
}
