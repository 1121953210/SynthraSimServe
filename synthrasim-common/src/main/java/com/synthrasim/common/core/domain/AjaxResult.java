package com.synthrasim.common.core.domain;

import com.synthrasim.common.constant.HttpStatus;
import java.util.HashMap;

/**
 * 统一响应结果封装
 * 
 * 所有Controller接口统一使用此类返回数据，确保前后端交互格式一致。
 * 继承HashMap实现，支持灵活地添加自定义字段。
 * 
 * 标准响应格式：
 * {
 *   "code": 200,      // 状态码
 *   "msg": "操作成功", // 提示消息
 *   "data": { ... }   // 业务数据（可选）
 * }
 */
public class AjaxResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    /** 状态码键名 */
    public static final String CODE_TAG = "code";

    /** 返回消息键名 */
    public static final String MSG_TAG = "msg";

    /** 数据对象键名 */
    public static final String DATA_TAG = "data";

    /** 默认构造：返回成功空结果 */
    public AjaxResult() {
        put(CODE_TAG, HttpStatus.SUCCESS);
        put(MSG_TAG, "操作成功");
    }

    /** 指定状态码和消息的构造函数 */
    public AjaxResult(int code, String msg) {
        put(CODE_TAG, code);
        put(MSG_TAG, msg);
    }

    /** 指定状态码、消息和数据的构造函数 */
    public AjaxResult(int code, String msg, Object data) {
        put(CODE_TAG, code);
        put(MSG_TAG, msg);
        if (data != null) {
            put(DATA_TAG, data);
        }
    }

    /** 返回成功结果（无数据） */
    public static AjaxResult success() {
        return new AjaxResult(HttpStatus.SUCCESS, "操作成功");
    }

    /** 返回成功结果（带自定义消息） */
    public static AjaxResult success(String msg) {
        return new AjaxResult(HttpStatus.SUCCESS, msg);
    }

    /** 返回成功结果（带数据） */
    public static AjaxResult success(Object data) {
        return new AjaxResult(HttpStatus.SUCCESS, "操作成功", data);
    }

    /** 返回成功结果（带消息和数据） */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(HttpStatus.SUCCESS, msg, data);
    }

    /** 返回错误结果（默认500错误） */
    public static AjaxResult error() {
        return new AjaxResult(HttpStatus.ERROR, "操作失败");
    }

    /** 返回错误结果（带自定义消息） */
    public static AjaxResult error(String msg) {
        return new AjaxResult(HttpStatus.ERROR, msg);
    }

    /** 返回错误结果（带自定义状态码和消息） */
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg);
    }

    /** 链式调用：向结果中追加键值对 */
    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
