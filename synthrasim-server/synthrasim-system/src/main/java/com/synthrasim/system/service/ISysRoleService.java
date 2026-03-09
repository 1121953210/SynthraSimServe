package com.synthrasim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.synthrasim.system.domain.SysRole;
import java.util.List;
import java.util.Set;

/**
 * 角色业务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /** 根据用户ID查询角色列表 */
    List<SysRole> selectRolesByUserId(Long userId);

    /** 根据用户ID查询角色编码集合（用于权限判断） */
    Set<String> selectRoleCodesByUserId(Long userId);

    /** 查询所有角色列表 */
    List<SysRole> selectRoleAll();
}
