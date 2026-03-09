package com.synthrasim.framework.security.filter;

import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 
 * 继承OncePerRequestFilter，确保每个请求只执行一次。
 * 在Spring Security的过滤器链中，此过滤器位于UsernamePasswordAuthenticationFilter之前。
 * 
 * 执行流程：
 * 1. 从请求头中提取JWT Token
 * 2. 通过TokenService解析Token，从Redis获取LoginUser
 * 3. 验证Token有效期（自动续期）
 * 4. 将LoginUser封装为Authentication对象，放入SecurityContext
 * 5. 后续的Controller方法可通过SecurityContextHolder获取当前用户
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 尝试从请求中获取已认证的用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);

        // 2. 如果用户信息存在且当前SecurityContext中没有认证信息
        if (loginUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 3. 验证Token有效期，必要时自动续期
            tokenService.verifyToken(loginUser);

            // 4. 构建Authentication对象并设置到SecurityContext
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 5. 继续执行后续的过滤器链
        filterChain.doFilter(request, response);
    }
}
