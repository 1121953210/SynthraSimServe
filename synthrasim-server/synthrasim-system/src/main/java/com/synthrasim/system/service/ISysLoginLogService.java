package com.synthrasim.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.synthrasim.system.domain.SysLoginLog;

/**
 * 登录日志业务接口
 */
public interface ISysLoginLogService extends IService<SysLoginLog> {

    /** 新增登录日志 */
    void insertLoginLog(SysLoginLog loginLog);

    /** 分页查询指定用户的登录日志（按时间倒序） */
    IPage<SysLoginLog> selectLoginLogPage(Long userId, int pageNum, int pageSize);
}
