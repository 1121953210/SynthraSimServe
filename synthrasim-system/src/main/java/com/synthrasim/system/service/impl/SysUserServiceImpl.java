package com.synthrasim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.common.utils.SecurityUtils;
import com.synthrasim.common.utils.StringUtils;
import com.synthrasim.system.domain.SysUser;
import com.synthrasim.system.domain.SysUserRole;
import com.synthrasim.system.mapper.SysUserMapper;
import com.synthrasim.system.mapper.SysUserRoleMapper;
import com.synthrasim.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户业务实现类
 * 
 * ServiceImpl<SysUserMapper, SysUser> 是MyBatisPlus提供的通用Service实现，
 * 自动注入对应的Mapper并实现IService接口中的所有基础CRUD方法。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private static final Logger logger = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public SysUser selectUserByUsername(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public boolean checkUsernameUnique(String username) {
        return userMapper.checkUsernameUnique(username) == 0;
    }

    @Override
    public boolean checkEmailUnique(SysUser user) {
        Long userId = user.getId() == null ? -1L : user.getId();
        if (StringUtils.isNotEmpty(user.getEmail())) {
            return userMapper.checkEmailUnique(user.getEmail(), userId) == 0;
        }
        return true;
    }

    @Override
    public boolean checkPhoneUnique(SysUser user) {
        Long userId = user.getId() == null ? -1L : user.getId();
        if (StringUtils.isNotEmpty(user.getPhone())) {
            return userMapper.checkPhoneUnique(user.getPhone(), userId) == 0;
        }
        return true;
    }

    /**
     * 注册新用户
     * 密码在调用前应已通过SecurityUtils.encryptPassword()加密
     */
    @Override
    public boolean registerUser(SysUser user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public int updateUserProfile(SysUser user) {
        return userMapper.updateById(user);
    }

    @Override
    public int resetUserPwd(String username, String password) {
        SysUser user = userMapper.selectUserByUsername(username);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(password);
        return userMapper.updateById(updateUser);
    }

    @Override
    public boolean updateUserAvatar(String username, String avatar) {
        SysUser user = userMapper.selectUserByUsername(username);
        if (user == null) {
            return false;
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setAvatar(avatar);
        return userMapper.updateById(updateUser) > 0;
    }

    @Override
    public List<SysUser> selectUserList(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(user.getUsername()), SysUser::getUsername, user.getUsername())
               .like(StringUtils.isNotEmpty(user.getRealName()), SysUser::getRealName, user.getRealName())
               .like(StringUtils.isNotEmpty(user.getPhone()), SysUser::getPhone, user.getPhone())
               .eq(user.getStatus() != null, SysUser::getStatus, user.getStatus())
               .eq(user.getOrgId() != null, SysUser::getOrgId, user.getOrgId())
               .orderByDesc(SysUser::getCreateTime);
        return userMapper.selectList(wrapper);
    }

    /**
     * 给用户分配角色
     * 先删除该用户的所有旧角色关联，再批量插入新关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        userRoleMapper.deleteUserRoleByUserId(userId);
        if (roleIds != null && roleIds.length > 0) {
            List<SysUserRole> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            for (SysUserRole ur : list) {
                userRoleMapper.insert(ur);
            }
        }
    }
}
