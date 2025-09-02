package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// @slf4j 引入日志
@Slf4j
@ServletComponentScan // 扫描过滤器
// @EnableTransactionManagement 开启事务管理
@EnableTransactionManagement
@EnableCaching  // 开启Spring Cache注解方式的缓存功能
@MapperScan("com.itheima.reggie.mapper")
// 禁用数据源自动配置
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ReggieApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功...");
    }

}
