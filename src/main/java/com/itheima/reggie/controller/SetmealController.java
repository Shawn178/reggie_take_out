package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


// 套餐菜品关系的控制类,写成SetmealController是因为主表依旧是套餐表
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    // 自动注入Service层
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    // 注入分类Service
    @Autowired
    private CategoryService categoryService;


    /*
    一、添加一个分页查询方法，用于查询套餐列表
     */
    @GetMapping("/page")
    // 定义一个分页查询方法,page：当前页码，pageSize：每页显示的记录数，name：查询条件
    public R<Page> page(int page,int pageSize,String name){
        // 1.创建一个分页对象:Page对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        // 5.1 创建一个新的分页对象：pageDto
        Page<SetmealDto> pageDto = new Page<>(page,pageSize);

        // 2.创建一个查询对象
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 3.添加查询条件
          // 3.1 查询条件1：套餐名称非空
        queryWrapper.like(name != null,Setmeal::getName,name);
          // 3.2 排序条件1：根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        // 4.与数据库交互，调用Service层方法，先注入setmealService层
          // 主要是根据查询条件对套餐分页查询，要用到数据库
        setmealService.page(pageInfo,queryWrapper);


        // 但上述方法获取的分页数据中，只是根据setmeal中属性查询，少了套餐分类名称categoryName,需要单独处理，其在setmealDto类中

        // 5.因此需要创建一个新的分类对象pageDto,将已经分好的pageInfo对象中的数据复制到pageDto中
          // 5.2 创建新的分页对象pageDto后，把pageInfo已经查询到的对象数据复制到pageDto中
            // protected List<T> records;但是Page中有records属性，泛型不同，不可以直接复制，需要手动写规则复制
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
          // 写一个方法，手动复制pageInfo中的records到pageDto中
          // 5.3 先获取pageInfo中的records对象
        List<Setmeal> records = pageInfo.getRecords();
          // 5.4 使用stream流遍历records对象，将其复制到pageDto中,最后写成列表
        List<SetmealDto> list = records.stream().map(item -> {
           // 5.5 每次都是新建一个pageDto对象，然后将item复制到pageDto中
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);

        // 下面的不熟悉！主要功能是根据分类id获取分类名称，然后将分类名称设置到setmealDto中
            // 因为这个steam流主要是为了获取分类名称，将两个表关联！

            // 5.6 要和分类名称进行关联，获取分类名称，需要先获取分类id
            Long categoryId = item.getCategoryId();
            // 5.7 根据分类id获取分类对象(需要注入categoryService)
            Category category = categoryService.getById(categoryId);
            // 5.8 从分类对象获取分类名称(前提是不为空值才可以继续后续)
            if (category != null){
                // 5.9 从分类对象获取分类名称
                String categoryName = category.getName();
                // 5.10 再将分类名称设置到setmealDto中
                setmealDto.setCategoryName(categoryName);
            }
            // 5.11 返回setmealDto,将分类名称查询出来并填充到 DTO（SetmealDto）中，用于前端展示，不修改 Setmeal 实体或数据库
            return setmealDto;
        }).collect(Collectors.toList());
        // 6.设置转换后的记录
        pageDto.setRecords(list);
        // 7.返回结果
        return R.success(pageDto);
    }


    /*
    二、添加一个新增套餐的方法

    前端返回的数据中包含套餐信息与套餐下菜品的信息，所以不能用参数Setmeal接收，应该使用DTO类接收
    因为SetmealDto类中封装了setmeal的全属性以及setmealDish的属性，所以要使用SetmealDto类接收

    并且前端返回的是JSON数据，要使用@RequestBody注解

    方法优化：加入@CacheEvict注解，表示 有数据新增时，清除缓存
    目的是：缓存数据，防止数据不一致，即 因为新增套餐后，需要更新缓存数据，防止缓存数据与数据库数据不一致

     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        // 加日志
        log.info("新增套餐：{}",setmealDto);

        // 套餐菜品关系保存后，在Controller层调用Service层的方法，保存套餐与菜品关系
        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }


    /*
    三、添加一个删除套餐的方法
    需要添加注解@RequestParam

    优化方法：使用@CacheEvict注解，删除缓存数据
     */
    @DeleteMapping
    // 加入allEntries = true，表示删除所有缓存数据(不加则默认是false)
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> deleteBatch(@RequestParam List<Long> ids){
        // 加日志
        log.info("删除套餐:{}",ids);

        // 调用Service层的删除方法
        setmealService.deleteBatch(ids);

        return R.success("删除套餐成功");
    }


    /*
    四、添加一个修改套餐状态的方法
    前端返回数组ids；需要加注释@RequestParam
    前端还需要返回当前套餐的状态status，1表示起售，0表示停售

    调试期间的错误：
    1.返回404错误：因为没添加@PutMapping的page
    2.返回405错误：因为前端要求返回POST请求，而且状态参数需要加入@PathVariable
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        // 加日志
        log.info("修改套餐状态：{}",status);
        // 调用Service层的批量起售、停售方法
        setmealService.updateStatus(status,ids);
        return R.success("更新套餐状态成功");
    }


    /*
    五、根据条件查询对应的套餐数据

优化代码：加入缓存注解
    添加注解@Cacheable,缓存套餐数据
    因为查询条件是分类id和状态status,所以缓存的key需要拼接两者

    调试错误：
    1.点击套餐分类无反应，报500错误
    错误原因：因为R封装类无法序列化，需要在R类中实现序列化接口Serializable
     */
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        // 1.新建一个构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 2.查询条件：先根据分类id查询套餐，然后按照id和时间排序
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        log.info("查询到的套餐分类id:{}",setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        log.info("查询到的套餐状态:{}",setmeal.getStatus());

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        // 3.调用Service方法，查询数据
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
