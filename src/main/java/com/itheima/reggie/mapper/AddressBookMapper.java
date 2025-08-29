package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

// 地址簿AddressBookMapper接口，继承自BaseMapper
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
