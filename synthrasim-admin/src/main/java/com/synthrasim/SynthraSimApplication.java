package com.synthrasim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用程序启动入口
 * 
 * @SpringBootApplication 是一个组合注解，等价于：
 * - @Configuration：标识为配置类
 * - @EnableAutoConfiguration：启用Spring Boot自动配置
 * - @ComponentScan：扫描当前包及子包下的所有Bean
 * 
 * 包路径 com.synthrasim 会自动扫描所有子模块中的组件。
 */
@SpringBootApplication
public class SynthraSimApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynthraSimApplication.class, args);
        System.out.println("============================================");
        System.out.println("   SynthraSim 工业仿真平台 启动成功!         ");
        System.out.println("   接口文档: http://localhost:8080/doc.html  ");
        System.out.println("============================================");
    }
}
