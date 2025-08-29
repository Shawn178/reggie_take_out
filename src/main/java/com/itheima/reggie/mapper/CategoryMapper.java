package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Category;
import org.apache.ibatis.annotations.Mapper;

// 菜单类：继承BaseMapper接口，并指定实体类为Category
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
