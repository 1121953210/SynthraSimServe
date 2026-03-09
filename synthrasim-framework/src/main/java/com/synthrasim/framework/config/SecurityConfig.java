package com.synthrasim.framework.config;

import com.synthrasim.framework.security.filter.JwtAuthenticationFilter;
import com.synthrasim.framework.security.handle.AuthenticationEntryPointImpl;
import com.synthrasim.framework.security.handle.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security安全配置
 * 
 * 核心配置说明：
 * 1. 禁用CSRF（前后端分离项目不需要）
 * 2. 禁用Session（使用JWT无状态认证）
 * 3. 配置接口访问权限（白名单/需认证）
 * 4. 注册JWT过滤器（在UsernamePasswordAuthenticationFilter之前）
 * 5. 配置认证失败/退出成功处理器
 * 6. 启用CORS跨域支持
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    /** 认证失败处理器 */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /** 退出成功处理器 */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /** JWT认证过滤器 */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 认证管理器
     * 用于在LoginService中手动触发认证流程
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 密码编码器
     * BCrypt算法，每次加密结果不同（自带盐值），安全性优于MD5/SHA
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // 禁用CSRF（前后端分离，Token已提供CSRF防护能力）
            .csrf().disable()
            // 认证失败处理器
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
            .and()
            // 禁用Session（无状态JWT认证）
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 接口访问权限配置
            .authorizeRequests()
                // ===== 以下接口无需认证（白名单） =====
                .antMatchers("/login", "/register", "/captchaImage").permitAll()
                // Swagger/Knife4j接口文档
                .antMatchers("/swagger-ui/**", "/swagger-resources/**", "/v2/**", "/v3/**",
                             "/doc.html", "/webjars/**", "/favicon.ico").permitAll()
                // Druid监控页面
                .antMatchers("/druid/**").permitAll()
                // 静态资源
                .antMatchers("/", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                // ===== 其他所有接口都需要认证 =====
                .anyRequest().authenticated()
            .and()
            // 退出登录配置
            .logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler)
            .and()
            // 启用CORS
            .cors().configurationSource(corsConfigurationSource())
            .and()
            // 在UsernamePasswordAuthenticationFilter之前添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 禁用默认的请求头缓存（iframe嵌套等）
            .headers().frameOptions().disable();

        return httpSecurity.build();
    }

    /**
     * CORS跨域配置
     * 允许前端开发服务器（如localhost:5173）跨域访问后端接口
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
