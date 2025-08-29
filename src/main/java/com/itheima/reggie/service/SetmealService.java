package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

// 套餐Setmeal接口，继承自IService
public interface SetmealService extends IService<Setmeal> {

    // 保存套餐关联信息与菜品信息
    void saveWithDish(SetmealDto setmealDto);

    // 批量删除套餐
    public R<String> deleteBatch(List<Long> ids);

    //  批量修改套餐起售、停售状态
    R<String> updateStatus(Integer status, List<Long> ids);
}
