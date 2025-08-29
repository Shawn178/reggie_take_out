package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private UserService userService;

    // 注入购物车数据
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /*
    一、用户下单的方法

    因为涉及到两张表：订单表和订单明细表，要加入事务控制，所以要加注解@Transactional
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        // 1.获得当前用户id
        Long userId = BaseContext.getCurrentId();

        // 2.查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
          // 调用Service，把购物车查询的数据传输
        List<ShoppingCart> shoppingCart = shoppingCartService.list(queryWrapper);

        if (shoppingCart == null || shoppingCart.size() == 0){
            throw new RuntimeException("当前购物车为空，不能下单");
        }

        // 3.因为下面的订单表需要用户的信息，要把用户信息查出来，以及用户地址表
          // 查询用户数据
        User user = userService.getById(userId);

        // 查询地址数据，因为页面发送订单数据时，调用了地址，所以直接用订单查询即可
        Long addressBookId = orders.getAddressBookId();
          // 调用Service，把地址查询的数据传输
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址为空，不能下单");
        }

        // 4.向订单表插入数据 -> 一条数据 (只是一个订单)
          // 生成订单号，调用IdWorker
        long orderId = IdWorker.getId();
        // 先将订单属性填充完整后保存
          // 定义一个总金额
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCart.stream().map(item -> {
            // 创建一个订单明细对象
            OrderDetail orderDetail = new OrderDetail();
            // 然后将对应属性设置(这些set方法可以用BeanUtils的copy方法，更简便)
            /*orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());*/

            BeanUtils.copyProperties(item,orderDetail,"id","userId","createTime");

            // 设置订单特有的字段
            orderDetail.setOrderId(orderId);

            // 计算总金额
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 向订单表插入数据，一条
        this.save(orders);

        // 5.向订单明细表插入数据 -> 多条数据 (因为一个订单可能有多个商品)
        orderDetailService.saveBatch(orderDetails);
        // 6.清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }


}
