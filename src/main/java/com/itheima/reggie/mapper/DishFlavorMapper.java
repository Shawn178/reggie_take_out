package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

// 菜品口味Mapper接口:继承BaseMapper
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor>{
}
