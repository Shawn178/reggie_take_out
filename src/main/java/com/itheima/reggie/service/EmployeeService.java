package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Employee;

// 员工类的实现类接口
// 继承IService接口，并指定实体类为employee
// IService是MyBatis-Plus提供的一个通用Service接口，它封装了常见的CRUD操作方法。
public interface EmployeeService extends IService<Employee> {

}
