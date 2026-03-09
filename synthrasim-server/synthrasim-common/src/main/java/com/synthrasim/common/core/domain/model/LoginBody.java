package com.synthrasim.common.core.domain.model;

import lombok.Data;

/**
 * 用户登录请求体
 * 
 * 前端登录表单提交时使用此对象接收参数。
 * 对应登录页面的用户名、密码、验证码等字段。
 */
@Data
public class LoginBody {

    /** 用户名（支持用户名/手机号/邮箱登录） */
    private String username;

    /** 用户密码 */
    private String password;

    /** 验证码 */
    private String code;

    /** 验证码唯一标识（从Redis中匹配验证码时使用） */
    private String uuid;
}
