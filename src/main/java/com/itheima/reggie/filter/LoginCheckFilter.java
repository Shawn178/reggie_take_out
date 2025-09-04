package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
*检查登录的过滤器：使用过滤器或者拦截器，在过滤器或者拦截器中判断用户是否已经完成登录，如果没有登录则跳转到登录页面。

实现步骤：
1、创建自定义过滤器LoginCheckFilter
2、在启动类上加入注解@ServletComponentScan
3、完善过滤器的处理逻辑

@WebFilter是过滤器，urlPatterns是过滤的url路径，/*表示所有路径
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
// 过滤器需要实现一个Filter接口，为了实现接口中的doFilter方法
public class LoginCheckFilter implements Filter {

    // 路径匹配器，用于匹配URL路径,支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

   // doFilter方法是过滤的方法
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 对servletRequest类型转换，因为父类没有getUrl方法，子类有
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        /*
        1、获取本次请求的URI
        2、判断本次请求是否需要处理
        3、如果不需要处理，则直接放行
        4、判断登录状态，如果已登录，则直接放行
        5、如果未登录则返回未登录结果,通过输出流方式向客户端页面响应数据
         */

        // 1.获取本次请求的URI，URI是统一资源标识符，包含了URL
        String requestURI = request.getRequestURI();

        // 加日志，方便调试!
        log.info("拦截到请求:{}",requestURI);

          // 罗列不需过滤处理的路径，直接放行
        String[] urls = new String[]{
                "/employee/login", // 用户想登录，直接放行
                "/employee/logout", // 用户想退出登录，直接放行
                "/backend/**", // backend的所有页面全放行
                "/front/**", // front的所有页面全放行
                "/common/**", // 不登录账号也可以使用上传下载网页
                "/user/sendMsg", // 移动端发送验证码
                "/user/login", // 移动端用户登录
                "/doc.html", // swagger文档界面
                "/webjars/**", //
                "/swagger-resources", // swagger静态资源
                "/v2/api-docs"
        };
        
        // 临时调试：打印所有白名单路径
        /*log.info("白名单路径:");
        for (String url : urls) {
            log.info("  - {}", url);
        }*/

        // 2、判断本次请求是否需要处理，封装成一个方法后调用
        boolean check = check(urls, requestURI);

        // 3、如果不需要处理，则直接放行
        if (check){
            // 不需要处理时，加日志
            log.info("本次请求{}不需要处理，直接放行",requestURI);
            // 直接放行session
            filterChain.doFilter(request,response);
            return;
        }


        // 4.1、判断登录状态，如果已登录，则直接放行
          // 获取session中放入的员工信息,getAttribute是获取session中放入的对象
        if(request.getSession().getAttribute("employee") != null){
            // 已处于登录状态时，加日志
            log.info("用户已登录，用户ID为:{}",request.getSession().getAttribute("employee"));

            /*
            在同一请求线程内，Service、DAO、MyBatis-Plus 的 MetaObjectHandler 等无需再依赖 HttpServletRequest，直接通过 BaseContext.getCurrentId() 就能拿到当前用户ID，用于公共字段自动填充（createUser/updateUser）、审计日志、数据权限等。
            解耦：避免层层传参或在非Web层硬取 Request/Session。
             */
            Long empId = (Long)request.getSession().getAttribute("employee");
            // 把这个ID放到 BaseContext（通常是用 ThreadLocal 实现的线程本地变量）里，达到“本次请求内随处可取”的效果。
            BaseContext.setCurrentId(empId);


            filterChain.doFilter(request,response);
            return;
        }

        // 4.2 判断移动端用户登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        /*
        5、如果未登录则返回未登录结果,通过输出流方式向客户端页面响应数据

        response.getWriter()是获取HTTP响应的输出流，用于向客户端发送数据
        R.error("NOTLOGIN") 是调用R类的静态方法，返回一个错误结果
        JSON.toJSONString() 是将R对象转换为JSON字符串
        wirte() 是将JSON字符串写入HTTP响应的输出流中，发送给客户端

        数据流向：
        客户端浏览器 ← HTTP响应 ← response.getWriter().write() ← 服务器

        当未登录用户访问需要登录的页面时，服务器会返回类似这样的JSON数据：
        {
        "code": 0,
        "msg": "NOTLOGIN",
        "data": null
        }
         */
        // 未登录也要加日志
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return; // 这里的 return 不是返回数据给方法调用者，而是结束当前方法的执行。

        // log.info("拦截到请求：{}",request.getRequestURL());
        // session放行
        // filterChain.doFilter(request,response);
    }


    // 路径匹配：判断本次请求是否需要处理/放行，封装成一个方法
    public boolean check(String[] urls,String requestURI){
        // 遍历url数组
        for (String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            log.info("尝试匹配: {} 与 {} = {}", requestURI, url, match);
            if (match){
                log.info("路径匹配成功: {} 匹配 {}", requestURI, url);
                return true;
            }
        }
        
        // 特殊处理：如果请求的是登录页面，直接放行
        /*if (requestURI.equals("/backend/page/login/login.html")) {
            log.info("特殊处理：登录页面直接放行");
            return true;
        }*/
        
        // 循环完毕没匹配到
        log.info("路径匹配失败: {} 未匹配任何白名单路径", requestURI);
        return false;
    }


}
