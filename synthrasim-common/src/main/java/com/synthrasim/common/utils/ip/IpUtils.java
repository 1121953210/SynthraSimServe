package com.synthrasim.common.utils.ip;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * 
 * 获取客户端真实IP地址，支持Nginx/CDN等反向代理场景。
 * 在代理环境下，客户端真实IP通常存储在X-Forwarded-For等请求头中。
 */
public class IpUtils {

    /**
     * 获取客户端IP地址
     * 
     * 依次从以下请求头中获取（适配不同代理服务器）：
     * 1. X-Forwarded-For（标准代理头）
     * 2. Proxy-Client-IP（Apache代理头）
     * 3. X-Real-IP（Nginx代理头）
     * 4. WL-Proxy-Client-IP（WebLogic代理头）
     * 5. request.getRemoteAddr()（无代理时直接获取）
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For可能包含多个IP（经过多级代理），取第一个（即客户端真实IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    private static boolean isUnknown(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}
