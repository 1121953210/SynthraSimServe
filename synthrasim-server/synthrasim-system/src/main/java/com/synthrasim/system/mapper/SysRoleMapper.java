package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询该用户拥有的所有角色
     * 通过sys_user_role中间表进行关联查询
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0 AND r.status = 1")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
