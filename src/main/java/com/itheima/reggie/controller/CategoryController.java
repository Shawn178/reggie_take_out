package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    // 注入CategoryService
    @Autowired
    private CategoryService categoryService;

    /*
    一、新增分类

     */
    // 因为前端返回值只有状态码，只需String即可；填完数据返回是Json数据，所以需要@RequestBody注解
    @PostMapping
    public R<String> save(@RequestBody Category category){

        // 加日志
        log.info("新增分类category：{}",category);

        // 调用Service层的save方法，将category的数据保存到数据库
        categoryService.save(category);
        return R.success("新增分类成功");
    }


    /*
    二、分页查询方法
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        // 创建Page对象：page是当前页码，pageSize是每页显示的条数
        Page<Category> pageInfo = new Page<>(page,pageSize);
        // 构造条件构造器对象
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);

        // 调用Service层进行分页查询:放入page对象和排序条件
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /*
    三、删除分类方法:根据id删除分类

    因为当前查询参数的URL形态：DELETE /category?ids=123
    只要形参名和查询参数同名（ids）就能绑定，甚至可以不写@RequestParam注解
    
    为什么分类删除不需要 @PathVariable？
    因为前端发的是查询参数 ids（/category?ids=...），而不是路径变量（/category/{id}）。没有路径变量，就不需要 @PathVariable。Spring 会按名称把查询参数 ids 绑定到方法参数 ids 上；建议显式写 @RequestParam("ids") 更清晰。

    而EmployeeController中：URL形态为DELETE /employee/123


    CategoryController 和 EmployeeController 的区别：
    1.Employee 用的是 REST 风格路径变量；Category 当前页面为支持“批量删除/统一参数名”，采用查询参数 ids。
    2.若你希望两者风格统一，可二选一：
        改前端：使用 /category/{id}，后端用 @PathVariable；
        保持前端：/category?ids=...，后端用 @RequestParam，并可支持 List<Long> 以便批量。
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        // 加日志(因为前端传回来的是ids)
        log.info("删除分类，id为：{}",ids);

        // 调用Service层的remove方法，根据id删除分类，因为重写了remove方法
        // 不能调用自带的removeById方法！因为自带的没有关联检查，会直接删除
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }


    /*
    四、根据id修改分类的方法

    因为前端传输是Json数据，所以需要@RequestBody注解
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        // 调用Service层的updateById方法，修改分类信息
        categoryService.updateById(category);

        return R.success("修改分类信息成功");
    }


    /*
    五、根据条件查询分类数据

    返回一个List集合； 使用Category来封装传来的参数
     */
    @GetMapping("list")
    public R<List<Category>> list(Category category){

        // 创建条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件：只需要传输的type进行查询
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        // 添加排序条件: 先根据sort进行排序，再根据updateTime进行排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        // 调用Service层,把结果封装成集合，返回给前端
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
