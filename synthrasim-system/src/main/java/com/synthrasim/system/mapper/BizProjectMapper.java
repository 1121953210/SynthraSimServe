package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.BizProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目表数据访问层
 */
@Mapper
public interface BizProjectMapper extends BaseMapper<BizProject> {
}

