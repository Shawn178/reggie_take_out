package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

// 菜品Dish的Service实现类,继承自ServiceImpl,实现DishService接口
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>  implements DishService {

    // 想要菜品口味表，需要注入DishFalvorService
    @Autowired
    private DishFlavorService dishFlavorService;


    // 新增菜品，同时保存对应的口味数据
    // 因为涉及到多张表的操作，所以需要加入事务控制@Transactional,想要事务生效，还需在启动类开启事务支持
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto); // this调用的是Dto，但是Dto继承了Dish

        // 保存菜品口味数据到菜品口味表dish_flavor
        // 因为Dto中有口味的列表属性，属性中含有dishId、value、name等
        // 要先获取dishId,和菜品先进行关联，才能把口味信息写入数据库，不然不匹配
        Long dishId = dishDto.getId(); // 获取菜品id
        List<DishFlavor> flavors = dishDto.getFlavors(); // 获取菜品口味列表

        /*
          遍历列表，为每个dishFlavor设置dishId

          flavors是口味列表List<DishFlavor>
          flavors.stream():创建一个可以逐个处理元素的流，stream()就是将List转换为流对象
          map(item -> {}):遍历口味列表中每个DishFlavor对象
          collect(Collectors.toList()):将处理后的元素重新收集到一个列表中
           */ 
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId); // 为每个DishFlavor对象设置dishId
            return item; // 返回修改后的口味对象
        }).collect(Collectors.toList());

        // 保存菜品口味的数据到数据库的菜品口味dish_flavor表中
            /*
        （重要！）为什么保存到数据库需要调用Service层呢？

            职责分离（三层架构）：
            Controller：接收请求、参数绑定、返回结果（不承载业务规则）。
            Service：编排业务流程与规则、聚合多个数据访问操作。
            Mapper/Repository：仅做单表/单语句的数据访问。
            放在 Service 能避免 Controller 直接操作数据库导致的“胖控制器”和强耦合。
             */
        dishFlavorService.saveBatch(flavors);

    }


    // 根据id查询菜品信息和口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        // 查询菜品基本信息，从dish表查询
          // 用this是因为Impl继承了Dish
        Dish dish = this.getById(id);
        // 简化方法：直接传入id

        // 构造一个DishDto对象，把dish对象属性复制给dishDto对象
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        // 查询当前菜品对应的口味信息，从dish_flavor表查询
          // 设置一个查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
          // 根据dishId查询对应口味
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
          // 把查到的口味数据封装到一个List中
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

          // 把数据封装到dishDto中
        dishDto.setFlavors(flavors);

        return dishDto;
    }


    /*
    四、修改菜品
    因为前端返回是JSON数据，所以要@RequestBody注解
    因为要修改菜品的同时修改菜品的口味，所以要@Transactional事务注解
     */
    @Transactional
    public void updateWithFlavor(@RequestBody DishDto dishDto){
        log.info("修改菜品:{}",dishDto);

        /* 涉及到两张表：dish和dish_flavor，不能直接调Service！
        调用Service层，修改数据库参数
        dishService.updateWithFlavor(dishDto);*/

        // 更新菜品表dish基本信息
        this.updateById(dishDto);

        // 更新口味表dish_flavor，比较简便的方式：先删再添
        // 先删除当前菜品对应的口味
          // 创建一个构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
          // 查询条件
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
          // 查询到就删除
        dishFlavorService.remove(queryWrapper);

        // 然后再添加新的菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        /*
        因为前端需要dishId,而dishId封装在dishDto中，不在dishFlavor中

        这里的 dishId 指的是菜品的ID，不是口味表中的 dishId 字段！
          dishDto.id = 菜品的ID（来自 Dish 类）
          dishFlavor.dishId = 口味关联的菜品ID（来自 DishFlavor 类）

        当需要保存口味信息时：
          1.首先保存菜品信息，获得菜品的 id
          2.然后需要将这个菜品的 id 设置到每个口味对象的 dishId 字段中
          3.这样才能建立口味和菜品的关联关系
         */
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId()); // 为每个DishFlavor对象设置菜品的Id
            return item; // 返回修改后的口味对象
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }


    /*
    五、批量删除菜品
     */
    @Transactional // 添加事务注解
    @Override
    public R<String> deleteBatch(List<Long> ids) {
        // 涉及到删除两张表：dish和dish_flavor
        // 1.先删除dish表的数据
        this.removeByIds(ids);
        // 2.再删除dish_flavor表的数据
          // 先创建一个构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
          // 添加查询条件:按照菜品的id查询，查到就删除
          // 使用in表示范围，在里面就删除
        // 此行代码错误：应该是getDishId,不带()
        // queryWrapper.in(DishFlavor::getDishId(),ids);
        queryWrapper.in(DishFlavor::getDishId,ids);

        // 3.执行删除,调用Service层
        dishFlavorService.remove(queryWrapper);
        return R.success("批量删除成功");
        
        // 是否可以直接调用上面的 查询菜品及其口味的方法，然后删除？
        // 不可行，而且效率更低！
          // 查询方法返回的是 DishDto 对象，包含很多不需要的字段
          // 删除操作只需要ID列表，查询会带来额外的数据处理
    }


    /*
    六、菜品停售、起售
     */
    @Override
    public R<String> updateStatus(Integer status, List<Long> ids) {
        // 加日志：
        log.info("菜品状态:{}",status);

        // 1.先进行参数校验：ids列表是否为空，status只能为1/0
        if (ids == null || ids.isEmpty()) return R.error("菜品ID列表为空");
        if (status !=0 && status != 1) return R.error("菜品状态只能为1/0");

        // 2.先批量查询菜品id列表
        List<Dish> dishList = this.listByIds(ids);

        // 3.批量更新菜品状态
          // 3.1 遍历查询到的菜品列表，直接为每个菜品更新status值(stream流)
          dishList = dishList.stream().map(dish -> {
            // 直接修改菜品状态
            dish.setStatus(status);
            // 返回修改后的菜品
            return dish;
          }).collect(Collectors.toList());

        // 4.最后调用了ServiceImpl层的批量更新方法
        this.updateBatchById(dishList);

        return R.success("批量更新菜品状态成功");
    }


}
