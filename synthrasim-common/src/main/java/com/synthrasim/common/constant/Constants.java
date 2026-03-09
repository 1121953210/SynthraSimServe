package com.synthrasim.common.constant;

/**
 * 系统常量定义
 * 集中管理项目中使用的所有常量值，避免魔法数字和硬编码字符串
 */
public class Constants {

    /** UTF-8字符集 */
    public static final String UTF8 = "UTF-8";

    /** 登录成功标识 */
    public static final String LOGIN_SUCCESS = "Success";

    /** 登录失败标识 */
    public static final String LOGIN_FAIL = "Error";

    /** 注销标识 */
    public static final String LOGOUT = "Logout";

    /** 注册标识 */
    public static final String REGISTER = "Register";

    /** 验证码Redis前缀 */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /** 验证码有效期（分钟） */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /** 登录用户Token的Redis前缀 */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /** Token令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT中存储用户唯一标识的claim键名 */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /** 令牌在HTTP请求头中的字段名 */
    public static final String TOKEN_HEADER = "Authorization";

    /** 资源映射路径前缀 */
    public static final String RESOURCE_PREFIX = "/profile";

    /** 防重提交Redis前缀 */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /** 自动识别json对象白名单配置（仅允许匹配的包名，防止反序列化漏洞） */
    public static final String[] JSON_WHITELIST_STR = { "org.springframework", "com.synthrasim" };
}
