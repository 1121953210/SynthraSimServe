package com.synthrasim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.synthrasim.system.domain.SysLoginLog;
import com.synthrasim.system.mapper.SysLoginLogMapper;
import com.synthrasim.system.service.ISysLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录日志业务实现类
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

    @Autowired
    private SysLoginLogMapper loginLogMapper;

    @Override
    public void insertLoginLog(SysLoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }

    @Override
    public IPage<SysLoginLog> selectLoginLogPage(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, SysLoginLog::getUserId, userId)
               .orderByDesc(SysLoginLog::getOperationTime);
        return loginLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
