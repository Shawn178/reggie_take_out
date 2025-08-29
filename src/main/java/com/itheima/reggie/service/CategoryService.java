package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

// 继承IService接口，并指定实体类为Category
public interface CategoryService extends IService<Category> {

    // 自己定义一个根据id删除的方法,再去实现类实现此方法
    public void remove(Long id);
}
