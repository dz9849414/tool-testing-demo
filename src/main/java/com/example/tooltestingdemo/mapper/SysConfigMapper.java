package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper接口
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}