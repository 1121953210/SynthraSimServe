package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户-角色关联数据访问层
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /** 根据用户ID删除用户角色关联（重新分配角色前先清除旧关联） */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoleByUserId(@Param("userId") Long userId);
}
