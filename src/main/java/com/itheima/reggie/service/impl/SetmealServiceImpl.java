package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Setmeal套餐接口实现类：继承ServiceImpl类，实现SetmealService接口
@Service


@Transactional // 事务注解
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    // 自动注入Service层
    @Autowired
    private SetmealDishService setmealDishService;

    /*
    一、实现保存套餐关联信息与菜品信息的方法

    因为需要同时操作两张表，所以添加事务注解，保证数据一致性，要么全成功/全失败
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 1.保存套餐的基本信息，操作setmeal表，执行insert操作
        this.save(setmealDto);

          // 1.1 获取日志：关联信息在setmealDto中，是集合SetmealDishes，需要用get获取
        log.info("保存套餐和菜品的关联信息:{}",setmealDto.getSetmealDishes());
          // 1.2 拿到关联关系集合setmealDishes
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
          // 1.3 遍历集合，为每个DishFlavor对象设置菜品的Id,使用Stream流
        setmealDishes.stream().map(item -> {
           // 为每个setmealDish对象设置对应的setmealId
             // 先从对应Dto中获取套餐的Id，然后使用set方法设置对应的Id
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 2.保存套餐和菜品的关联信息，操作setmeal_dish表，执行insert操作
          // 相当于已经匹配了信息和id，直接保存到套餐菜品关系表即可，调用Service层保存到数据库
        setmealDishService.saveBatch(setmealDishes);
    }


    /*
    二、实现批量删除套餐功能的方法

    还可以加入状态判断，启售的不能删除

    并且操作了两张表，最好开启事务 @Transactional
     */
    @Transactional
    @Override
    public R<String> deleteBatch(List<Long> ids) {
        // 涉及到两张表，即stemal 和 setmeal_dish

        // 1.先删除stemal表中的数据
        // this.removeByIds(ids);

        // 2.再删除setmeal_dish表中的数据,不可以直接删除了，需要查询关联关系
          // 2.1 新建一个构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
          // 2.2 添加查询条件，按照套餐id查询
        queryWrapper.in(Setmeal::getId,ids);
          // 2.3 添加查询条件：状态为1的不能删除
        queryWrapper.eq(Setmeal::getStatus,1);

        // 3.判断查询到的结果数，大于0就不能删
        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 4.否则就是可以删除套餐，先删除套餐表中的数据
        this.removeByIds(ids);

        // 5.再删除套餐菜品关系表setmeal_dish表的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getSetmealId,ids);

        // 6 调用Service的方法，删除查询到的套餐(要根据套餐id删除)
        setmealDishService.remove(queryWrapper2);

        // 3.删除完成
        return R.success("批量删除套餐成功");
    }


    /*
    三、实现批量起售、停售套餐的方法

    需要用stream流遍历集合，为每个对象设置对应的status属性
     */
    @Override
    public R<String> updateStatus(Integer status, List<Long> ids) {
        // 1.创建一个构造器
        // LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 2.添加查询条件：根据套餐的id查询
        // queryWrapper.in(Setmeal::getId,ids);


        // 1.先批量查询套餐的id
        List<Setmeal> setmeals = this.listByIds(ids);
        // 2.使用stream流遍历，赋值,返回一个新的集合
        setmeals = setmeals.stream().map(item -> {
           // 3.设置对应的status属性
           item.setStatus(status);
           return item;
        }).collect(Collectors.toList());

        // 4.调用Service方法，更新数据库
        this.updateBatchById(setmeals);

        return R.success("批量更新套餐状态成功");
    }


}
