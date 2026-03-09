package com.synthrasim.common.core.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户注册请求体
 * 
 * 继承LoginBody，注册时除了用户名密码外，
 * 还需要额外的注册信息（如邮箱、手机号等），可在此扩展。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterBody extends LoginBody {

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;
}
