package com.synthrasim.common.enums;

/**
 * 用户账号状态枚举
 */
public enum UserStatus {

    /** 正常状态 */
    OK(1, "正常"),

    /** 禁用状态 */
    DISABLE(0, "停用"),

    /** 已删除 */
    DELETED(1, "删除");

    private final int code;
    private final String info;

    UserStatus(int code, String info) {
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
