package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
// 员工实体类实现 Serializable接口——将对象转换为字节流，方便在网络中传输或存储到文件中
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber; // 身份证号

    private Integer status;

    /*
    公共字段自动填充:
    MybatisPlus公共字段自动填充，也就是在插入或者更新的时候为指定字段赋予指定的值，使用它的好处就是可以统一对这些字段进行处理，避免了重复代码。
    实现步骤：
        1、在实体类的属性上加入@TableField注解，指定自动填充的策略
        2、按照框架要求编写元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口
     */
    @TableField(fill = FieldFill.INSERT) // 插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时填充字段
    private LocalDateTime updateTime;

    // 创建人，注解表示插入时填充
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    // 修改人，注解表示插入和更新时填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
