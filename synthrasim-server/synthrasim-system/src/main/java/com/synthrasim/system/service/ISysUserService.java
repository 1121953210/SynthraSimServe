package com.synthrasim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.synthrasim.system.domain.SysUser;
import java.util.List;

/**
 * 用户业务接口
 * 
 * 继承MyBatisPlus的IService接口，自动获得基础增删改查能力。
 * 此处定义项目特有的业务方法。
 */
public interface ISysUserService extends IService<SysUser> {

    /** 根据用户名查询用户（支持用户名/手机号/邮箱） */
    SysUser selectUserByUsername(String username);

    /** 校验用户名是否唯一 */
    boolean checkUsernameUnique(String username);

    /** 校验邮箱是否唯一 */
    boolean checkEmailUnique(SysUser user);

    /** 校验手机号是否唯一 */
    boolean checkPhoneUnique(SysUser user);

    /** 注册新用户 */
    boolean registerUser(SysUser user);

    /** 修改用户基本信息（个人中心编辑） */
    int updateUserProfile(SysUser user);

    /** 修改用户密码 */
    int resetUserPwd(String username, String password);

    /** 修改用户头像 */
    boolean updateUserAvatar(String username, String avatar);

    /** 查询用户列表（分页） */
    List<SysUser> selectUserList(SysUser user);

    /** 给用户分配角色 */
    void insertUserAuth(Long userId, Long[] roleIds);
}
