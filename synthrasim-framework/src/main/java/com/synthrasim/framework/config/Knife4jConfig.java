package com.synthrasim.framework.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;
import java.util.List;

/**
 * Knife4j（Swagger增强）接口文档配置
 *
 * 关键注解说明：
 * - @EnableSwagger2WebMvc：启用Springfox Swagger2，这是knife4j 3.x必需的注解
 * - @EnableKnife4j：启用Knife4j增强功能（文档搜索、离线文档等）
 *
 * 配置后访问 http://localhost:8080/doc.html 即可查看API文档。
 */
@Configuration
@EnableSwagger2WebMvc
@EnableKnife4j
public class Knife4jConfig {

    /**
     * 创建API文档分组
     *
     * 使用basePackage扫描所有Controller，比注解扫描更可靠。
     * 凡是com.synthrasim包下的Controller都会被自动收录到文档中。
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 扫描所有com.synthrasim包下的Controller
                .apis(RequestHandlerSelectors.basePackage("com.synthrasim"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /** API文档基本信息 */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("工业仿真平台 API文档")
                .description("SynthraSim Server RESTful API - 接口说明文档")
                .version("1.0.0")
                .contact(new Contact("SynthraSim", "", ""))
                .build();
    }

    /**
     * 配置Token认证方式
     * 在文档页面顶部的"Authorize"按钮中填入Token即可测试需要认证的接口
     * 格式：Bearer {你的JWT Token}
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> list = new ArrayList<>();
        list.add(new ApiKey("Authorization", "Authorization", "header"));
        return list;
    }

    /** 配置哪些接口需要携带Token */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> list = new ArrayList<>();
        list.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/.*"))
                .build());
        return list;
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        List<SecurityReference> list = new ArrayList<>();
        list.add(new SecurityReference("Authorization", authorizationScopes));
        return list;
    }
}
