package com.synthrasim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.synthrasim.system.domain.SysRole;
import com.synthrasim.system.mapper.SysRoleMapper;
import com.synthrasim.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色业务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    /**
     * 获取用户的角色编码集合
     * 如果用户拥有admin角色，则返回包含"admin"的集合（admin拥有所有权限）
     */
    @Override
    public Set<String> selectRoleCodesByUserId(Long userId) {
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        Set<String> roleCodes = new HashSet<>();
        for (SysRole role : roles) {
            if (role.getRoleCode() != null) {
                roleCodes.add(role.getRoleCode());
            }
        }
        return roleCodes;
    }

    @Override
    public List<SysRole> selectRoleAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getIsDeleted, 0)
               .eq(SysRole::getStatus, 1);
        return roleMapper.selectList(wrapper);
    }
}
