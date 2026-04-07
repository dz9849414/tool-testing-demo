package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysUserOrganization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户组织关联Mapper接口
 */
@Mapper
public interface SysUserOrganizationMapper extends BaseMapper<SysUserOrganization> {
}