package com.synthrasim.common.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet工具类
 * 
 * 提供获取当前HTTP请求/响应对象、向响应写入数据等便捷方法。
 * 基于Spring的RequestContextHolder获取当前请求上下文。
 */
public class ServletUtils {

    /** 获取当前HTTP请求对象 */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /** 获取当前HTTP响应对象 */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /** 获取Spring管理的请求属性 */
    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端（常用于直接输出JSON）
     *
     * @param response HTTP响应对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
