package com.itheima.reggie.common;

/*
(重要！！！)基于ThreadLocal封装工具类，用于保存和获取当前登录用户id

什么是ThreadLocal?
ThreadLocal并不是一个Thread，而是Thread的局部变量。

ThreadLocal常用方法：
1.public void set(T value）设置当前线程的线程局部变量的值。
2.public T get()           返回当前线程所对应的线程局部变量的值


实现步骤：
1、编写BaseContext工具类，基于ThreadLocal封装的工具类
2、在LoginCheckFilter的doFilter方法中调用BaseContext来设置当前登录用户的id
3、在MyMetaObjectHandler的方法中调用BaseContext获取登录用户的id
 */
public class BaseContext {
    // 作用范围是某一个线程内，每一次请求都是一个新的线程
    // 1、创建ThreadLocal对象
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 设置值的方法：
    public static void setCurrentId(Long id){
        // 调用threadLocal的set方法，将值设置到当前线程中
        threadLocal.set(id);
    }

    // 获取值的方法：需要返回id，所以是Long类型
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
