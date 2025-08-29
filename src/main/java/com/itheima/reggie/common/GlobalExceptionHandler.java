package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

// 全局异常处理器：AOP
// @ControllerAdvice(annotations = {RestController.class,Controller.class})
// @ControllerAdvice缺少@ResPonseBody注解，而@RestControllerAdvice = @ControllerAdvice + @ResponseBody
@RestControllerAdvice  // 自动包含全部注解的类，不需要再annotation
@Slf4j
public class GlobalExceptionHandler {

    // 分类捕获异常：捕获到指定异常时，返回指定错误信息

    // 1.SQL异常:SQLException 包含 SQLIntegrityConstraintViolationException，但是Spring捕获异常会采用最具体原则
    // 优先匹配最具体的异常，没有再考虑下一级异常
    @ExceptionHandler(SQLException.class)
    public R<String> exceptionHandler(SQLException ex){
        // 记录SQL异常的日志
        log.error("捕获到SQL异常：{}",ex.getMessage(),ex);
        // 可以对异常信息判断输出
        if (ex.getMessage().contains("Duplicate entry")){
            // 获取重复的列名
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        // 返回错误信息
        return R.error("数据库操作失败");
    }


    // 2.全局处理自定义的异常
    @ExceptionHandler(CustomException.class)
    public R<String> ExceptionHandler(CustomException ex){
        // 加日志
        log.error("捕获到运行时业务异常:{}",ex.getMessage());

        return R.error(ex.getMessage());
    }


    // 兜底处理：捕获异常的方法,加注解@ExceptionHandler,捕获所有异常
    /*@ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception ex){
        // 记录全部异常的日志
        log.error("捕获到未知异常:{}",ex.getMessage(),ex);

        // 返回统一格式的错误信息
        return R.error("系统繁忙，请稍后再试");
    }*/
}
