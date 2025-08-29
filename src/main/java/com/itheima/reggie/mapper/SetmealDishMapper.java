package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

// 套餐菜品关联类Mapper接口，继承自BaseMapper类
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
}
