package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 继承ServiceImpl类，实现CategoryService接口
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
  // 继承自 ServiceImpl，而 ServiceImpl 有 removeById 方法

    // 自动注入菜品和套餐的Service层
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    // 自定义方法：根据id删除分类
    @Override
    public void remove(Long id) {
        // 构造一个查询条件
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件：根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        // 统计当前分类下的菜品数量,相对于查询当前分类是否关联菜品
        int count1 = dishService.count(dishLambdaQueryWrapper);

        // 1.先查询当前分类是否关联了菜品，若已关联，抛出异常；否则，正常删除
        if (count1 > 0) {
            // 表示关联了菜品
        }


        // 2.先查询当前分类是否关联了套餐，若已关联，抛出异常；否则，正常删除
          // 构造查询条件，并根据分类id查询套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
          // 统计当前分类下的套餐数量
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if (count2 > 0){
            // 表示关联了套餐,不可以删除了，抛出自定义的业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }

        // 3.如果没关联菜品/套餐，正常删除分类
          // 使用 super 明确调用父类ServiceImpl的方法
          // 为什么不用this？如果当前类重写了 removeById，会调用重写版本
          // 而 super 明确调用父类的方法，避免潜在的冲突
        super.removeById(id);
    }
}