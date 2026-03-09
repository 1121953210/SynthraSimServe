package com.synthrasim.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * MyBatis Plus配置类
 * 
 * 配置内容：
 * 1. 分页插件 - 实现物理分页（而非内存分页）
 * 2. 自动填充 - 自动填充createTime/updateTime等公共字段
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatisPlus插件配置
     * 
     * 分页插件说明：
     * MyBatisPlus默认不开启分页功能，需要手动配置分页拦截器。
     * 配置后，使用 Page 对象进行分页查询时会自动拼接 LIMIT 语句。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 公共字段自动填充处理器
     * 
     * 当实体类中的字段标注了 @TableField(fill = FieldFill.INSERT) 或
     * @TableField(fill = FieldFill.INSERT_UPDATE) 时，
     * MyBatisPlus在执行INSERT/UPDATE操作前会自动调用此处理器填充字段值。
     * 
     * 避免了在每个Service方法中手动设置createTime/updateTime的重复代码。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /**
             * 插入时自动填充
             * 新增记录时自动设置创建时间和更新时间
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
                this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
            }

            /**
             * 更新时自动填充
             * 修改记录时自动更新更新时间
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        };
    }
}
