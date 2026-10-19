package com.vblog.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus Configuration
 *
 * @author V-Blog Team
 */
@Configuration
public class MyBatisPlusConfig {
    @org.springframework.context.annotation.Bean
    public com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor mybatisPlusInterceptor() {
        var interceptor = new com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor(
                com.baomidou.mybatisplus.annotation.DbType.MYSQL));
        return interceptor;
    }
}
