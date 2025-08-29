package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    // 自动注入Service
    @Autowired
    private ShoppingCartService shoppingCartService;

    // 购物车减少数量的实现方法
    @Override
    public void sub(ShoppingCart shoppingCart) {
        // 获取当前用户Id的购物车数据
        Long currentId = BaseContext.getCurrentId();

        // 组装查询条件：同一用户 + 同一菜品/套餐 + 同一口味
        // 1.定义一个构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 2.添加查询条件
          // 2.1 添加用户id
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
          // 2.2 添加菜品id,非空时获取菜品id，然后再看菜品口味，口味非空再把口味添加上
        if (shoppingCart.getDishId() != null){
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
            if (shoppingCart.getDishFlavor() != null) {
                queryWrapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
            }
        } else if (shoppingCart.getSetmealId() != null) {
            // 2.3 否则如果是套餐，则类似上序操作
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        } else {
            // 2.4 如果两个都不是，直接返回
            return;
        }
          
        // 3.调用Service层查询:查询当前菜品或套餐是否在购物车中
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne == null) {
            return;
        }
        // 4.如果存在，且数量大于1，就减去1；否则如果数量为1就删除该条目
        Integer num = cartServiceOne.getNumber();
        if (num != null && num > 1){
            cartServiceOne.setNumber(num - 1);
            // 再调用Service层更新数据库
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 只要购物车数量小于1了，就删除该条目
            shoppingCartService.removeById(cartServiceOne.getId());
        }
    }
}
