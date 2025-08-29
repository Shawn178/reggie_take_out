package com.itheima.reggie.common;

// 自定义的业务异常类：继承RuntimeException 运行时异常
public class CustomException extends RuntimeException{

    // 自定义一个方法，传输错误信息
    public CustomException(String message){
        // 调用父类RuntimeException中的有参构造器
        super(message);
    }
}
