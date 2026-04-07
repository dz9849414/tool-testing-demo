package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典Mapper接口
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
}