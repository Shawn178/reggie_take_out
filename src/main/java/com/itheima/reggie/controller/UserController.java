package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.EmailUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    // 注入RedisTemplate对象
    @Autowired
    private RedisTemplate redisTemplate;

    /*
    一、发送邮箱验证码
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody Map<String, String> map, HttpSession session){
        String email = map.get("email");              // 用 email 键接收
        String code = ValidateCodeUtils.generateValidateCode(4).toString();                 // 生成验证码
        log.info("生成的验证码为:{}",code);
        EmailUtils.sendAuthCodeEmail(email, code);    // 发送邮件
        // session.setAttribute(email, code);            // 用 email 作为 session key

        // 新方法：用Redis缓存保存验证码，并且设置有效期为5min
        redisTemplate.opsForValue().set(email,code,5, TimeUnit.MINUTES);

        return R.success("验证码已发送");
    }


    /*
    二、登录方法

    前端返回的JSON数据是phone: 12345678901,code: 1234,属于键值对
    使用User类接收少了code，可以写一个dto类，也可以直接使用Map接收，因为Map就是键值对

    为什么泛型返回User？因为登陆成功后返回给前端的数据是user信息
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        log.info(map.toString());

        // 获取邮箱
        String email = String.valueOf(map.get("email"));  // 按 email 取
        // 获取验证码
        String code = String.valueOf(map.get("code"));
        
        // 从Session中获取保存的验证码
        /*
        在登录接口读取验证码，如果发送验证码接口保存时用的不是email作为key，或者接收的键名还叫phone，就会导致Session里根本没有这个key！
        怎么改？ 发送验证码接口必须用map.get("email")获取邮箱,并用 session.setAttribute(email,code)保存  
        ---> 修改的是senMsg方法的内容
         */
        // Object codeInSession = session.getAttribute(email);

        // 新方法：从Redis中获取缓存的验证码！
        Object codeInRedis = redisTemplate.opsForValue().get(email);

        // 进行验证码的比对(页面提交的验证码和Session保存的验证码对比)
        if(codeInRedis != null && codeInRedis.equals(code)){
            // 如果可以对比成功，登录成功
            // 判断当前邮箱对应的用户是否是新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail,email);

            // 调用UserService，与数据库链接
            User user = userService.getOne(queryWrapper);
            // 判断
            if (user == null){
                // 说明是新用户，把他加进去
                user = new User();
                user.setEmail(email);
                user.setPhone(""); // 允许手机号为空
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            // 登录成功后，删除Redis中缓存的验证码
            redisTemplate.delete(email);

            return R.success(user);
        }
        return R.error("邮箱发送失败");
    }
}
