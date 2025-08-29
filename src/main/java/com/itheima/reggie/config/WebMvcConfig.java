package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// 该配置类作用是：配置静态资源映射
// 实现WebMvcConfigurer接口，实现addResourceHandlers方法
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // 设置要映射哪些静态资源文件,即把静态资源映射到指定的Resource路径下
        // 这样就可以打开静态资源了;将backend和front文件夹下所有文件映射到Resource路径下
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    // 重写父类方法：扩展MVC框架的消息转换器
    /*
    提交的id与数据库id显示不一致：
    发现了问题的原因，即js对long型数据进行处理时丢失精度，导致提交的id和数据库中的id不一致。
    如何解决这个问题？
    我们可以在服务端给页面响应json数据时进行处理，将long型数据统一转为String字符串
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 创建一个新的消息转换器对象
          // Jackson库提供的HTTP消息转换器,负责将HTTP请求/响应中的JSON数据与Java对象之间进行转换
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用Jackson将Java对象转为json
          // 告诉Jackson如何将Java对象转换为JSON，以及如何将JSON转换为Java对象
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将上面的消息转换器对象追加到MVC框架的转换器集合中
          // 将新创建的消息转换器添加到列表的第一个位置,参数0就是表示添加在索引的位置(最前面，优先级第一)
        converters.add(0,messageConverter);
    }
}
