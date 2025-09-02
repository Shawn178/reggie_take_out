package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 配置类，用于配置MyBatisPlus的分页插件
@Configuration
public class MyBatisPlusConfig {

    @Bean
    // 创建MyBatisPlus的分页插件。这是 MyBatis-Plus 3.5+ 的"插件容器"，用来装多个"内部拦截器"(InnerInterceptor)。
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 创建拦截器容器实例
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 往容器中添加分页拦截器，自动统计总数并分页
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 
        return mybatisPlusInterceptor;
    }
}
