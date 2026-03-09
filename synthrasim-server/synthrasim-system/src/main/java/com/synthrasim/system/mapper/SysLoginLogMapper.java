package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志数据访问层
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}
