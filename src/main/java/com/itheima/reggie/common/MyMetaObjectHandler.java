package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 公共字段自动填充：2、按照框架要求编写 元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口

// 这个类需要被Spring发现并管理
// 加入@Component注解，交给Spring管理，这样MyBatisPlus才能找到并使用这个处理器
@Component
@Slf4j
// Spring启动 → 扫描 @Component → 创建 MyMetaObjectHandler 实例 → 注册到Spring容器
public class MyMetaObjectHandler implements MetaObjectHandler {
    // 插入操作，自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        // 加日志
        log.info("开始公共字段自动插入填充，对象:{}",metaObject.toString());

        /*
        不能通过 @Autowired 注入 HttpServletRequest 来获取用户ID!
            1.作用域不匹配：单例Bean vs 请求作用域
            2.生命周期不同步：Bean创建时请求还不存在
            3.线程安全问题：多个请求共享同一个字段

         * @Autowired
            private HttpServletRequest request; // 多个请求会共享这个字段！
            
            @Override
            public void insertFill(MetaObject metaObject) {
                // 这里的request可能是其他请求的，不是当前请求的！
                Long userId = getUserIdFromRequest(request);
            }
         */

        // 自动填充创建时间、更新时间；创建人、更新人员
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        // 自动填充人的时候需要获取id，正常都是用Request获取，但是这个类没有
            // 这个类不是Controller，没有HttpServletRequest,无法直接获取当前登录用户信息
            // 所以先固定写死id为1，表示是管理员
        // metaObject.setValue("createUser",new Long(1));
        // metaObject.setValue("updateUser",new Long(1));

          // 现在定义好了ThreadLocal，就可以直接获取当前用户的id了
        metaObject.setValue("createUser",BaseContext.getCurrentId());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }

    // 更新操作，自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        // 加日志
        log.info("开始公共字段自动更新填充,对象:{}",metaObject.toString());

        // 自动填充
        metaObject.setValue("updateTime",LocalDateTime.now());
        // 同上
        // metaObject.setValue("updateUser",new Long(1));
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
