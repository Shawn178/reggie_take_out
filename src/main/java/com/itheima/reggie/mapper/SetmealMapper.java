package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;

// 套餐Setmeal接口:也要继承自BaseMapper类
@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {
}
