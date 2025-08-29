package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

// 菜品Dish接口，继承自IService
public interface DishService extends IService<Dish> {

    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish和dish_flavor
    // 因为Dto已经封装了dish和dish_flavor的全部属性
    public void saveWithFlavor(DishDto dishDto);


    // 根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 更新/修改菜品信息，同时更新对应的口味数据
    void updateWithFlavor(DishDto dishDto);

    // 批量删除菜品
    public R<String> deleteBatch(List<Long> ids);

    // 批量停售、起售菜品,需要一个状态值：1是起售，0是停售
    public R<String> updateStatus(Integer status,List<Long> ids);
}
