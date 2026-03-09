package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.SysOrganization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组织机构数据访问层
 */
@Mapper
public interface SysOrganizationMapper extends BaseMapper<SysOrganization> {
}
