package com.synthrasim.common.utils;

import org.apache.commons.lang3.ObjectUtils;

/**
 * 字符串工具类
 * 
 * 扩展Apache Commons Lang3的StringUtils，
 * 提供项目中常用的字符串处理方法。
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 判断对象是否为空
     * 支持String、Collection、Map、Array等类型的空值判断
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /** 判断对象是否非空 */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * 格式化字符串
     * 使用{}作为占位符，按顺序替换参数
     * 示例：format("你好{}，欢迎来到{}", "张三", "平台") → "你好张三，欢迎来到平台"
     */
    public static String format(String template, Object... params) {
        if (isEmpty(template) || ObjectUtils.isEmpty(params)) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template);
        for (Object param : params) {
            int index = sb.indexOf("{}");
            if (index == -1) {
                break;
            }
            sb.replace(index, index + 2, String.valueOf(param));
        }
        return sb.toString();
    }
}
