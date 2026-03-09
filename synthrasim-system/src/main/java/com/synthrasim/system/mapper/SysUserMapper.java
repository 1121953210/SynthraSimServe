package com.synthrasim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 * 
 * 继承MyBatisPlus的BaseMapper，自动获得基础CRUD能力：
 * - insert / deleteById / updateById / selectById
 * - selectList / selectPage / selectCount
 * 
 * 复杂SQL（如联表查询）在此接口中自定义。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（登录认证用）
     * 支持用户名、手机号、邮箱三种方式登录
     */
    @Select("SELECT * FROM sys_user WHERE is_deleted = 0 AND (username = #{username} OR phone = #{username} OR email = #{username})")
    SysUser selectUserByUsername(@Param("username") String username);

    /**
     * 检查用户名是否已存在
     * @return 存在返回记录数>0，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    int checkUsernameUnique(@Param("username") String username);

    /**
     * 检查邮箱是否已被使用
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE email = #{email} AND is_deleted = 0 AND id != #{excludeUserId}")
    int checkEmailUnique(@Param("email") String email, @Param("excludeUserId") Long excludeUserId);

    /**
     * 检查手机号是否已被使用
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE phone = #{phone} AND is_deleted = 0 AND id != #{excludeUserId}")
    int checkPhoneUnique(@Param("phone") String phone, @Param("excludeUserId") Long excludeUserId);
}
