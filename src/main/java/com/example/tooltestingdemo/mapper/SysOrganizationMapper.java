package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysOrganization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组织Mapper接口
 */
@Mapper
public interface SysOrganizationMapper extends BaseMapper<SysOrganization> {
}