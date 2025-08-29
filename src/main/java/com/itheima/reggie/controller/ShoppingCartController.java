package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
    一、添加购物车

    因为前端返回JSON形式，所以需要加入@RequestBody注解
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        // 加日志
        log.info("购物车数据：{}",shoppingCart);

        // 设置用户id，指定当前是哪个用户的id
          // 通过BaseContext的获取用户id的方法
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 查询当前菜品或者套餐是否在购物车中
          // 添加菜品时，返回dishId;添加套餐时，返回setmealId
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null) {
            // 不为空，说明添加到购物车的是菜品
              // 是菜品要查询菜品信息
            cartQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        } else {
            // 为空，说明添加到购物车的是套餐
            cartQueryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }

        // 调用Service层查询:查询当前菜品或套餐是否在购物车中
        ShoppingCart cartServiceOne = shoppingCartService.getOne(cartQueryWrapper);

        // 如果已经存在，在原来数量基础上 + 1
        if (cartServiceOne != null) {
            // 获取原来的数量
            Integer number = cartServiceOne.getNumber();
            // 在原来基础上，直接number + 1,用set写入数据
            cartServiceOne.setNumber(number + 1);
            // 再调用Service层写入数据库，更新一下
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 不存在就添加到购物车，数量默认为1
            // 先手动设置number为1
            shoppingCart.setNumber(1);
            // 入库的时候设置一个id创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartService.save(shoppingCart);
            // 再把购物车的数据写入数据
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }


    /*
    二、查看购物车列表
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){

        // 加日志查看购物车
        log.info("查看购物车...");

        // 根据UserId查询购物车列表
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        // 调用Service层查询
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }


    /*
    三、购物车减少菜品、套餐数量

    调试期间错误汇总：
    1.点击购物车减号，显示操作超时
    错误原因：
    1前端返回JSON数据，我忘记添加@RequestBody注解
    2数量小于1时，应该按照id删除，忘记获取id
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车减少数量...");
        // 调用Service层的减少方法
        shoppingCartService.sub(shoppingCart);
        return R.success("减菜品/套餐 成功");
    }

    /*
    四、清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车...");

        // 构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件：通过查询用户id，相同就删除全部购物车
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        // 调用Service层的remove方法
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}
