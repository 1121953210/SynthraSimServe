package com.synthrasim.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志记录注解
 * 
 * 标注在Controller方法上，框架会自动记录该接口的调用日志，
 * 包括操作人、操作时间、请求参数、执行结果等信息。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /** 操作模块描述 */
    String title() default "";

    /** 操作类型（增删改查等） */
    String businessType() default "OTHER";
}
