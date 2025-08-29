package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

// 日志注解
@Slf4j
// @RestController = @Controller + @ResponseBody，表示返回数据为json格式
@RestController
// @RequestMapping,作用是映射请求路径(restful风格)
@RequestMapping("/employee")
public class EmployeeController {

    // 自动注入EmployeeService接口的实现类
    @Autowired
    private EmployeeService employeeService;

    // 登录方法的接口
    @PostMapping("login") // 发送请求
    // 因为返回的是json数据，所以需要在返回值上添加@RequestBody注解，表示返回的数据为json格式
    // 还需要一个HttpServletRequest对象，用于获取请求参数
    // R<>泛型，指定返回的类型
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /*
    一、登录方法的逻辑：

        1、将页面提交的密码password进行md5加密处理
        2、根据页面提交的用户名username查询数据库
        3、如果没有查询到则返回登录失败结果
        4、密码比对，如果不一致则返回登录失败结果
        5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        6、登录成功，将员工id存入Session并返回登录成功结果
         */

        // 1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
          // 调用DigestUtils的md5DigestAsHex方法对密码进行加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2、根据页面提交的用户名username查询数据库
          // LambdaQueryWrapper是MyBatis-Plus提供的一个强大的查询条件构造器,它允许通过Lambda表达式方式构建SQL查询条件，避免硬编码字段名
          // 在登录验证过程中，需要根据用户名查询员工信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
          /*queryWrapper.eq()方法接收两个参数：
            第一个参数：指定数据库字段（通过实体类的方法引用）
            第二个参数：指定要比较的值

            Employee::getUsername 是Java 8中的方法引用语法，它等价于以下Lambda表达式：
            (Employee emp) -> emp.getUsername()
           */
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        log.info("查询条件：username = {}", employee.getUsername());
          // getOne()方法用于查询单个对象，而且username在数据库定义时是唯一的，所以查询的结果至多一条
        Employee emp = null;
        try {
          emp = employeeService.getOne(queryWrapper, false);
            log.info("查询结果：{}", emp);
        } catch (Exception e) {
            log.error("查询员工信息时发生异常：", e);
            // throw e;
            return R.error("查询异常");
        }

        // 3、如果没有查询到则返回登录失败结果
        if (emp == null){
            return R.error("登录失败");
        }

        // 4、密码比对，如果不一致则返回登录失败结果
            // 获取数据库中的密码和页面提交加密的密码对比
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        // 5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果,0表示已禁用，1表示已启用
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        // 6、登录成功，将员工id存入Session并返回登录成功结果
            // 把员工id存入session作用：保持员工登录状态、授予权限且可以获取员工信息
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }


    /*
    二、退出登录方法:
      后台退出功能开发代码开发
        用户点击页面中退出按钮，发送请求，请求地址为/employee/logout，请求方式为POST。
        我们只需要在Controller中创建对应的处理方法即可，具体的处理逻辑：
        1、清理Session中的用户id(session的生命周期为一次会话，一次会话指的是用户打开浏览器到关闭浏览器的过程)
        2、返回结果
     */
    @PostMapping("/logout")
    // 等下要清理session中的用户id,所以要注入HttpServletRequest对象
    // 而前端退出登录时，并未返回其余数据，所以不需要返回值
    public R<String> logout(HttpServletRequest request){
        // 1、清理Session中的用户id:removeAttribute()作用是移除指定的属性值
        request.getSession().removeAttribute("employee");
        // 2、返回结果
        return R.success("退出成功");
    }



    /*
    三、添加员工的方法
     */
    @PostMapping
    // 只需要传入String即可，前端没返回数据data，只返回了code，
    // 传入的参数是JSON形式，需要加注解@RequestBody
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，其信息是:{}",employee.toString());

        // 因为添加员工没有密码，所以设置一个公共初始密码,要通过md5加密！
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /*// 设置创建时间和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 获取当前登录用户的id，要用request对象获得！所以要注入HttpServletRequest对象
        // 且用户id是Long类型，而session中的id是Object，要强转
        Long empId = (Long)request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        // 以上设置创建时间和更新时间，创建人，更新人的操作，均可通过公共字段自动填充实
        // 公共字段自动填充 统一存放在MyMetaObjectHandler类中

        // 保存员工数据，调用Service层的save方法
        employeeService.save(employee);
        return R.success("新增员工成功");
    }


    /*
    四、员工信息分页查询
    在开发代码之前，需要梳理一下整个程序的执行过程：
    1、页面发送ajax请求，将分页查询参数(page、pageSize、name)提交到服务端
    2、服务端Controller接收页面提交的数据并调用Service查询数据
    3、Service调用Mapper操作数据库，查询分页数据
    4、Controller将查询到的分页数据响应给页面
    5、页面接收到分页数据并通过ElementUl的Table组件展示到页面上
     */
    // 泛型使用page，因为要使用page中的数据
    @GetMapping("page")
    public R<Page> page(int page,int pageSize,String name){
        // 加日志，匹配参数
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 构建分页构造器:主要是设置当前页数和总页数
        Page pageInfo = new Page(page, pageSize);
        // 构建条件构造器：主要是动态封装传过来的过滤条件
            // LambdaQueryWrapper：封装查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
            /*
            添加过滤条件：给查询条件增加"模糊匹配"规则

            StringUtils.isNotEmpty(name)是条件生效开关，只有name不为空，才会执行匹配
            Employee::getName是Java 8中的方法引用语法，它等价于以下Lambda表达式，通过方法引用指明对name做模糊匹配
            最后的name参数就是参与模糊匹配的值
             */ 
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
            // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询:调用page方法中的分页参数与查询条件
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }



    /*
    五、启用/禁用员工账号
    启用、禁用员工账号，本质上就是一个更新操作，也就是管理员对status状态字段进行操作在Controller中创建update方法，
    此方法是一个通用的修改员工信息的方法
     */

    // 只需要String类型，因为前端只判断状态码，不需要返回数据
    // 因为前端返回数据是json格式，所以需要加@RequestBody
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        // 加日志
        log.info(employee.toString());

        // 通过request获取当前用户的id
        Long empId = (Long)request.getSession().getAttribute("employee");
        // 设置修改时间(获取当前时间)以及哪个用户设置的修改
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);

        // 修改员工信息，调用Service层的修改方法
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }


    /*
    六、编辑员工信息(以下8个步骤都是前端完成)
    在员工管理列表页面点击编辑按钮，跳转到编辑页面，在编辑页面回显员工信息并进行修改，最后点击保存按钮完成编辑操作
    1、点击编辑按钮时，页面跳转到add.html，并在url中携带参数[员工id]
    2、在add.html页面获取url中的参数[员工id]
    3、发送ajax请求，请求服务端，同时提交员工id参数
    4、服务端接收请求，根据员工id查询员工信息，将员工信息以json形式响应给页面
    5、页面接收服务端响应的json数据，通过VUE的数据绑定进行员工信息回显
    6、点击保存按钮，发送ajax请求，将页面中的员工信息以json方式提交给服务端
    7、服务端接收员工信息，并进行处理，完成后给页面响应
    8、页面接收到服务端响应信息后进行相应处理
     */
    @GetMapping("/{id}")  // id是通过url地址栏传输
    // @PathVariable注解，将url中的参数id绑定到方法参数id中
    public R<Employee> getById(@PathVariable Long id){
        // 加日志
        log.info("根据id查询员工信息");
        // 调用Service层的根据id查员工方法
        Employee employee = employeeService.getById(id);
        // 加一个判断，确保查询到非空的员工id
        if(employee != null){
            return R.success(employee);
        }
        return R.error("员工不存在");
    }

}
