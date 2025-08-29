package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 员工类的Mapper接口
// 继承BaseMapper接口，并指定实体类为employee，其也是MyBatis-Plus的接口
// 作用是封装CRUD操作，并提供常用数据库的操作方法
public interface EmployeeMapper extends BaseMapper<Employee> {

}
