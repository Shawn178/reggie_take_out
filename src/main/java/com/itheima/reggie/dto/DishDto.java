package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 菜品的数据传输对象类，继承Dish类的全部属性
@Data
public class DishDto extends Dish {

    // 下面是dto新增的属性，相对于给Dish类新增加属性

    // 菜品口味的列表：目的是用来接收前端返回的多个口味的flavors参数
        // 因为前端返回了name和value,直接用DishFlavor类接收，因为其有这俩参数
    private List<DishFlavor> flavors = new ArrayList<>();

    // 菜品分类名称
    private String categoryName;

    //
    private Integer copies;
}
