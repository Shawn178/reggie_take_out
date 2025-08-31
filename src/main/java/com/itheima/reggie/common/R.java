package com.itheima.reggie.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// R：result，返回json数据给前端
@Data
public class R<T> implements Serializable {

    // 状态码在前端login.html中用到，返回1表示登录成功，进入页面；然后返回msg和data
    private Integer code; // 编码：1成功，0和其它数字为失败

    private String msg; // 错误信息

    private T data; // 数据

    private Map map = new HashMap(); // 定义一个哈希表，用于返回动态数据

    // 登录成功的方法
    public static <T> R<T> success(T object){
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    // 登录失败的方法
    public static <T> R<T> error(String msg){
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key,Object value){
        this.map.put(key,value);
        return this;
    }
}
