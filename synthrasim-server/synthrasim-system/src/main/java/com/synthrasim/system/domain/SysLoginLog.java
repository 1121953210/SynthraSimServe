package com.synthrasim.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志实体类 - 对应数据库表 sys_login_log
 * 
 * 记录用户每次登录/注销操作，用于安全审计和用户行为追踪。
 * 在个人中心"登录日志"页签中按时间倒序分页展示。
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名（冗余存储，避免每次联表查询） */
    private String username;

    /** 操作类型：1=登录，2=注销登录 */
    private Integer operationType;

    /** 操作IP地址 */
    private String ipAddress;

    /** 浏览器User-Agent */
    private String userAgent;

    /** 操作结果：0=失败，1=成功 */
    private Integer loginStatus;

    /** 失败原因（登录失败时记录） */
    private String failReason;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;
}
